package com.warisango.service;

import com.warisango.exception.OperationConflictException;
import com.warisango.model.Challenge;
import com.warisango.model.ChallengeParticipation;
import com.warisango.repository.ChallengeRepository;
import com.google.cloud.Timestamp;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ChallengeService {
    private static final ZoneId CHALLENGE_TIME_ZONE = ZoneId.of("Asia/Kuala_Lumpur");
    private static final int CHECK_IN_TARGET_MAXIMUM = 300;
    private static final int BONUS_POINTS_MAXIMUM = 10_000;
    private final ChallengeRepository challengeRepository;

    public ChallengeService(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    public List<Map<String, Object>> getChallenges(String touristId) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> checkIns = challengeRepository.findCheckIns(touristId);

        for (Challenge challenge : challengeRepository.findAllChallenges()) {
            if (!isVisibleToTourists(challenge)) continue;

            String challengeId = challenge.challengeId();
            int target = challenge.target();
            int progress = (int) checkIns.stream().filter(checkIn -> isEligible(checkIn, challenge)).count();
            ChallengeParticipation participation = challengeRepository.findParticipation(touristId, challengeId);
            boolean joined = participation != null;
            boolean completed = joined && "completed".equalsIgnoreCase(participation.status());
            boolean expired = isExpired(challenge);

            if (joined && !completed && !expired) {
                challengeRepository.updateParticipationProgress(
                        participation.participationId(), Math.min(progress, target) + "/" + target);
            }

            Map<String, Object> challengeResponse = toMap(challenge);
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
        for (Challenge challenge : challengeRepository.findAllChallenges()) {
            if (!isVisibleToTourists(challenge)) {
                continue;
            }
            Map<String, Object> guestChallenge = toMap(challenge);
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
        Challenge challenge = requireChallenge(challengeId);
        ensureJoinable(challenge);
        ChallengeParticipation existing = challengeRepository.findParticipation(touristId, challengeId);
        if (existing != null && "completed".equalsIgnoreCase(existing.status())) {
            throw new OperationConflictException("This challenge has already been completed.");
        }

        int target = challenge.target();
        ChallengeParticipation participation = new ChallengeParticipation(
                existing == null ? null : existing.participationId(), touristId, challengeId,
                "0/" + target, "in_progress", null);
        challengeRepository.saveParticipation(existing == null ? null : existing.participationId(), participation);
    }

    public Map<String, Object> claim(String touristId, String challengeId) throws Exception {
        Challenge challenge = requireChallenge(challengeId);
        ensureJoinable(challenge);
        int target = challenge.target();
        int progress = (int) challengeRepository.findCheckIns(touristId).stream()
                .filter(checkIn -> isEligible(checkIn, challenge))
                .count();
        if (progress < target) {
            throw new OperationConflictException("Challenge requirements are not completed.");
        }

        ChallengeParticipation participation = challengeRepository.findParticipation(touristId, challengeId);
        if (participation == null) {
            throw new OperationConflictException("Join the challenge first.");
        }

        int reward = challenge.rewardPoints();
        int currentPoints = challengeRepository.claimReward(
                touristId, challengeId, participation.participationId(), reward, target);
        return Map.of("success", true, "pointsEarned", reward, "currentPoints", currentPoints);
    }

    public List<Map<String, Object>> adminList() throws Exception {
        return challengeRepository.findAllChallenges().stream()
                .map(challenge -> {
                    Map<String, Object> response = toMap(challenge);
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

    private Challenge requireChallenge(String challengeId) throws Exception {
        Challenge challenge = challengeRepository.findChallenge(challengeId);
        if (challenge == null) throw new IllegalArgumentException("Challenge not found.");
        return challenge;
    }

    private boolean isVisibleToTourists(Challenge challenge) {
        return "ACTIVE".equalsIgnoreCase(challenge.status() == null ? "ACTIVE" : challenge.status());
    }

    /**
     * An expiry date remains valid until the end of that local calendar day.
     */
    private boolean isExpired(Challenge challenge) {
        try {
            return LocalDate.parse(challenge.expiry()).isBefore(LocalDate.now(CHALLENGE_TIME_ZONE));
        } catch (Exception exception) {
            return true;
        }
    }

    private void ensureJoinable(Challenge challenge) {
        if (!isVisibleToTourists(challenge)) {
            throw new OperationConflictException("This challenge is not active.");
        }
        if (isExpired(challenge)) {
            throw new OperationConflictException("This challenge has expired.");
        }
    }

    private boolean isEligible(Map<String, Object> checkIn, Challenge challenge) {
        if (checkIn.get("businessId") == null) {
            return false;
        }

        Instant checkInTime = timestampAsInstant(checkIn.get("checkInTimestamp"));
        Instant createdTime = challenge.createdDate();
        if (checkInTime == null || createdTime == null || checkInTime.isBefore(createdTime)) {
            return false;
        }

        try {
            Instant expiryEnd = LocalDate.parse(challenge.expiry())
                    .plusDays(1)
                    .atStartOfDay(CHALLENGE_TIME_ZONE)
                    .toInstant();
            return checkInTime.isBefore(expiryEnd);
        } catch (Exception exception) {
            return false;
        }
    }

    private Instant timestampAsInstant(Object value) {
        if (value instanceof Timestamp timestamp) {
            return timestamp.toDate().toInstant();
        }
        if (value instanceof java.util.Date date) {
            return date.toInstant();
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        return null;
    }

    private Challenge normalize(Map<String, Object> input) {
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
        if (target < 1 || target > CHECK_IN_TARGET_MAXIMUM) {
            throw new IllegalArgumentException("Target check-ins must be between 1 and 300.");
        }
        if (rewardPoints < 1 || rewardPoints > BONUS_POINTS_MAXIMUM) {
            throw new IllegalArgumentException("Bonus points must be between 1 and 10000.");
        }
        try {
            LocalDate.parse(expiry);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Expiry must use YYYY-MM-DD format.");
        }
        if (!Set.of("ACTIVE", "INACTIVE").contains(status)) {
            throw new IllegalArgumentException("Status must be ACTIVE or INACTIVE.");
        }

        return new Challenge(null, title, description, requirement, target, rewardPoints,
                badge, expiry, status, null);
    }

    private Map<String, Object> toMap(Challenge challenge) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", challenge.challengeId());
        data.put("title", challenge.title());
        data.put("description", challenge.description());
        data.put("requirement", challenge.requirement());
        data.put("target", challenge.target());
        data.put("rewardPoints", challenge.rewardPoints());
        data.put("badge", challenge.badge());
        data.put("expiry", challenge.expiry());
        data.put("status", challenge.status());
        data.put("createdDate", challenge.createdDate());
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
