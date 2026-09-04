package com.warisango.service;

import com.warisango.exception.OperationConflictException;
import com.warisango.repository.ChallengeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ChallengeService {
    private final ChallengeRepository challengeRepository;

    public ChallengeService(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    public List<Map<String, Object>> getChallenges(String touristId) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> checkIns = challengeRepository.findCheckIns(touristId);

        for (Map<String, Object> challenge : challengeRepository.findAllChallenges()) {
            if (!isVisibleToTourists(challenge)) continue;

            String challengeId = textValue(challenge.get("id"));
            int target = intValue(challenge.get("target"), 1);
            int progress = (int) checkIns.stream().filter(checkIn -> isEligible(checkIn, challenge)).count();
            Map<String, Object> participation = challengeRepository.findParticipation(touristId, challengeId);
            boolean joined = participation != null;
            boolean completed = joined && "completed".equalsIgnoreCase(textValue(participation.get("status")));
            boolean expired = isExpired(challenge);

            if (joined && !completed && !expired) {
                challengeRepository.updateParticipationProgress(
                        textValue(participation.get("id")), Math.min(progress, target) + "/" + target);
            }

            Map<String, Object> challengeResponse = new HashMap<>(challenge);
            challengeResponse.put("target", target);
            challengeResponse.put("progress", Math.min(progress, target));
            challengeResponse.put("joined", joined);
            challengeResponse.put("done", completed);
            challengeResponse.put("expired", expired);
            challengeResponse.put("eligibleToClaim", joined && !completed && !expired && progress >= target);
            result.add(challengeResponse);
        }
        return result;
    }

    /**
     * Returns active challenge information without creating or reading visitor participation.
     */
    public List<Map<String, Object>> getGuestChallenges() throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> challenge : challengeRepository.findAllChallenges()) {
            if (!isVisibleToTourists(challenge)) {
                continue;
            }
            Map<String, Object> guestChallenge = new HashMap<>(challenge);
            guestChallenge.put("progress", 0);
            guestChallenge.put("joined", false);
            guestChallenge.put("done", false);
            guestChallenge.put("expired", isExpired(challenge));
            guestChallenge.put("eligibleToClaim", false);
            result.add(guestChallenge);
        }
        return result;
    }

    public void join(String touristId, String challengeId) throws Exception {
        Map<String, Object> challenge = requireChallenge(challengeId);
        ensureJoinable(challenge);
        Map<String, Object> existing = challengeRepository.findParticipation(touristId, challengeId);
        if (existing != null && "completed".equalsIgnoreCase(textValue(existing.get("status")))) {
            throw new OperationConflictException("This challenge has already been completed.");
        }

        int target = intValue(challenge.get("target"), 1);
        Map<String, Object> participation = new HashMap<>();
        participation.put("touristId", touristId);
        participation.put("challengeId", challengeId);
        participation.put("progress", "0/" + target);
        participation.put("status", "in_progress");
        participation.put("completedDate", null);
        challengeRepository.saveParticipation(existing == null ? null : textValue(existing.get("id")), participation);
    }

    public Map<String, Object> claim(String touristId, String challengeId) throws Exception {
        Map<String, Object> challenge = requireChallenge(challengeId);
        ensureJoinable(challenge);
        int target = intValue(challenge.get("target"), 1);
        int progress = (int) challengeRepository.findCheckIns(touristId).stream()
                .filter(checkIn -> isEligible(checkIn, challenge))
                .count();
        if (progress < target) {
            throw new OperationConflictException("Challenge requirements are not completed.");
        }

        Map<String, Object> participation = challengeRepository.findParticipation(touristId, challengeId);
        if (participation == null) {
            throw new OperationConflictException("Join the challenge first.");
        }

        int reward = intValue(challenge.get("rewardPoints"), 0);
        int currentPoints = challengeRepository.claimReward(
                touristId, challengeId, textValue(participation.get("id")), reward, target);
        return Map.of("success", true, "pointsEarned", reward, "currentPoints", currentPoints);
    }

    public List<Map<String, Object>> adminList() throws Exception {
        return challengeRepository.findAllChallenges().stream()
                .map(challenge -> {
                    Map<String, Object> response = new HashMap<>(challenge);
                    boolean expired = isExpired(challenge);
                    response.put("expired", expired);
                    if (expired) {
                        response.put("status", "EXPIRED");
                    }
                    return response;
                })
                .toList();
    }

    public String create(Map<String, Object> input) throws Exception {
        return challengeRepository.createChallenge(normalize(input));
    }

    public void update(String id, Map<String, Object> input) throws Exception {
        challengeRepository.updateChallenge(id, normalize(input));
    }

    public void delete(String id) throws Exception {
        challengeRepository.deleteChallenge(id);
    }

    private Map<String, Object> requireChallenge(String challengeId) throws Exception {
        Map<String, Object> challenge = challengeRepository.findChallenge(challengeId);
        if (challenge == null) throw new IllegalArgumentException("Challenge not found.");
        return challenge;
    }

    private boolean isVisibleToTourists(Map<String, Object> challenge) {
        return "ACTIVE".equalsIgnoreCase(textValue(challenge.getOrDefault("status", "ACTIVE")));
    }

    /**
     * An expiry date remains valid until the end of that local calendar day.
     */
    private boolean isExpired(Map<String, Object> challenge) {
        try {
            return LocalDate.parse(textValue(challenge.get("expiry"))).isBefore(LocalDate.now());
        } catch (Exception exception) {
            return true;
        }
    }

    private void ensureJoinable(Map<String, Object> challenge) {
        if (!isVisibleToTourists(challenge)) {
            throw new OperationConflictException("This challenge is not active.");
        }
        if (isExpired(challenge)) {
            throw new OperationConflictException("This challenge has expired.");
        }
    }

    private boolean isEligible(Map<String, Object> checkIn, Map<String, Object> challenge) {
        return checkIn.get("businessId") != null;
    }

    private Map<String, Object> normalize(Map<String, Object> input) {
        String title = textValue(input.get("title"));
        String description = textValue(input.get("description"));
        String requirement = textValue(input.get("requirement"));
        String badge = textValue(input.get("badge"));
        String expiry = textValue(input.get("expiry"));
        int target = intValue(input.get("target"), 0);
        int rewardPoints = intValue(input.get("rewardPoints"), 0);
        String status = textValue(input.getOrDefault("status", "ACTIVE")).toUpperCase(Locale.ROOT);

        if (title.isBlank() || description.isBlank() || requirement.isBlank() || badge.isBlank()) {
            throw new IllegalArgumentException("All challenge text fields are required.");
        }
        if (target < 1 || rewardPoints < 1) {
            throw new IllegalArgumentException("Target and reward points must be positive.");
        }
        try {
            LocalDate.parse(expiry);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Expiry must use YYYY-MM-DD format.");
        }
        if (!Set.of("ACTIVE", "INACTIVE").contains(status)) {
            throw new IllegalArgumentException("Status must be ACTIVE or INACTIVE.");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("description", description);
        data.put("requirement", requirement);
        data.put("target", target);
        data.put("rewardPoints", rewardPoints);
        data.put("badge", badge);
        data.put("expiry", expiry);
        data.put("status", status);
        return data;
    }

    private int intValue(Object value, int fallback) {
        if (value instanceof Number number) return number.intValue();
        try {
            return value == null ? fallback : Integer.parseInt(String.valueOf(value));
        } catch (Exception exception) {
            return fallback;
        }
    }

    private String textValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
