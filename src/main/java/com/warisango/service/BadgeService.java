package com.warisango.service;

import com.warisango.dto.BadgeDTO;
import com.warisango.dto.BadgeSummaryDTO;
import com.warisango.exception.FirebasePersistenceException;
import com.warisango.repository.BadgeRepository;
import com.warisango.repository.ChallengeRepository;
import com.warisango.repository.CheckInRepository;
import com.warisango.repository.PointsRepository;
import com.warisango.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

@Service
public class BadgeService {
    private final BadgeRepository badgeRepository;
    private final CheckInRepository checkInRepository;
    private final ReviewRepository reviewRepository;
    private final PointsRepository pointsRepository;
    private final ChallengeRepository challengeRepository;

    public BadgeService(BadgeRepository badgeRepository,
                        CheckInRepository checkInRepository,
                        ReviewRepository reviewRepository,
                        PointsRepository pointsRepository,
                        ChallengeRepository challengeRepository) {
        this.badgeRepository = badgeRepository;
        this.checkInRepository = checkInRepository;
        this.reviewRepository = reviewRepository;
        this.pointsRepository = pointsRepository;
        this.challengeRepository = challengeRepository;
    }

    public List<BadgeDTO> getBadges(String touristId) {
        try {
            BadgeProgress progress = loadProgress(touristId);
            Map<String, Instant> earned = badgeRepository.findEarnedBadges(touristId);
            List<Map<String, Object>> definitions = badgeRepository.findAllDefinitions();

            for (Map<String, Object> definition : definitions) {
                String badgeId = text(definition.get("badgeId"));
                BadgeMetric metric = metricFor(definition, progress);
                if (metric.progress() >= metric.target() && !earned.containsKey(badgeId)) {
                    badgeRepository.awardBadge(touristId, badgeId);
                }
            }

            earned = badgeRepository.findEarnedBadges(touristId);
            List<BadgeDTO> badges = new ArrayList<>();
            for (Map<String, Object> definition : definitions) {
                String badgeId = text(definition.get("badgeId"));
                BadgeMetric metric = metricFor(definition, progress);
                badges.add(new BadgeDTO(
                        badgeId,
                        text(definition.get("name")),
                        text(definition.get("emoji")),
                        text(definition.get("description")),
                        text(definition.get("unlockCriteria")),
                        text(definition.get("criteriaType")),
                        earned.containsKey(badgeId),
                        earned.get(badgeId),
                        Math.min(metric.progress(), metric.target()),
                        metric.target()
                ));
            }
            return badges;
        } catch (Exception exception) {
            throw new FirebasePersistenceException("Unable to load badges.", exception);
        }
    }

    public BadgeSummaryDTO getSummary(String touristId) {
        List<BadgeDTO> badges = getBadges(touristId);
        int earned = (int) badges.stream().filter(BadgeDTO::earned).count();
        BadgeDTO foodExplorer = badges.stream()
                .filter(badge -> "Food Explorer".equalsIgnoreCase(badge.name()))
                .findFirst()
                .orElse(null);
        return new BadgeSummaryDTO(badges.size(), earned, badges.size() - earned, foodExplorer);
    }

    public List<Map<String, Object>> adminList() throws Exception {
        return badgeRepository.findAllDefinitions();
    }

    public String create(Map<String, Object> input) throws Exception {
        return badgeRepository.createDefinition(normalize(input));
    }

    public void update(String badgeId, Map<String, Object> input) throws Exception {
        badgeRepository.updateDefinition(badgeId, normalize(input));
    }

    public void delete(String badgeId) throws Exception {
        badgeRepository.deleteDefinitionAndAwards(badgeId);
    }

    private BadgeProgress loadProgress(String touristId) throws Exception {
        int visits = (int) checkInRepository.findByTouristId(touristId).stream()
                .map(checkIn -> checkIn.getBusinessId())
                .distinct()
                .count();
        int reviews = reviewRepository.countByTouristId(touristId);
        int points = pointsRepository.getCurrentPoints(touristId);
        int challenges = challengeRepository.countCompletedParticipations(touristId);
        return new BadgeProgress(visits, reviews, points, challenges);
    }

    private BadgeMetric metricFor(Map<String, Object> definition, BadgeProgress progress) {
        String criteriaType = text(definition.get("criteriaType"));
        int target = intValue(definition.get("target"), 1);
        int current = switch (criteriaType) {
            case "checkInCount" -> progress.visits();
            case "reviewCount" -> progress.reviews();
            case "pointsEarned" -> progress.points();
            case "completedChallengeCount" -> progress.challenges();
            default -> 0;
        };
        return new BadgeMetric(current, target);
    }

    private Map<String, Object> normalize(Map<String, Object> input) {
        String name = text(input.get("name"));
        String emoji = text(input.get("emoji"));
        String description = text(input.get("description"));
        String unlockCriteria = text(input.get("unlockCriteria"));
        String criteriaType = text(input.get("criteriaType"));
        int target = intValue(input.get("target"), 0);
        Set<String> supportedTypes = Set.of(
                "checkInCount", "reviewCount", "pointsEarned", "completedChallengeCount");

        if (name.isBlank() || emoji.isBlank() || description.isBlank() || unlockCriteria.isBlank()) {
            throw new IllegalArgumentException("All badge text fields and emoji are required.");
        }
        if (!supportedTypes.contains(criteriaType)) {
            throw new IllegalArgumentException("Unsupported badge criteria type.");
        }
        if (target < 1) throw new IllegalArgumentException("Badge target must be positive.");

        Map<String, Object> definition = new HashMap<>();
        definition.put("name", name);
        definition.put("emoji", emoji);
        definition.put("description", description);
        definition.put("unlockCriteria", unlockCriteria);
        definition.put("criteriaType", criteriaType);
        definition.put("target", target);
        return definition;
    }

    private int intValue(Object value, int fallback) {
        if (value instanceof Number number) return number.intValue();
        try {
            return value == null ? fallback : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private record BadgeProgress(int visits, int reviews, int points, int challenges) {
    }

    private record BadgeMetric(int progress, int target) {
    }
}
