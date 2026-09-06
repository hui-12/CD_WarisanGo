package com.warisango.service;

import com.warisango.dto.BadgeDTO;
import com.warisango.dto.BadgeSummaryDTO;
import com.warisango.exception.FirebasePersistenceException;
import com.warisango.model.Badge;
import com.warisango.model.TouristBadge;
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
    private static final int ACTIVITY_TARGET_MAXIMUM = 300;
    private static final int POINTS_TARGET_MAXIMUM = 10_000;
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
            Map<String, Instant> earned = earnedByBadgeId(badgeRepository.findEarnedBadges(touristId));
            List<Badge> definitions = badgeRepository.findAllDefinitions();

            for (Badge definition : definitions) {
                String badgeId = definition.badgeId();
                BadgeMetric metric = metricFor(definition, progress);
                boolean qualifies = metric.progress() >= metric.target();
                if (qualifies && !earned.containsKey(badgeId)) {
                    badgeRepository.awardBadge(touristId, badgeId);
                } else if (!qualifies && earned.containsKey(badgeId)) {
                    badgeRepository.revokeBadge(touristId, badgeId);
                }
            }

            earned = earnedByBadgeId(badgeRepository.findEarnedBadges(touristId));
            List<BadgeDTO> badges = new ArrayList<>();
            for (Badge definition : definitions) {
                String badgeId = definition.badgeId();
                BadgeMetric metric = metricFor(definition, progress);
                badges.add(new BadgeDTO(
                        badgeId,
                        definition.name(), definition.emoji(), definition.description(),
                        definition.unlockCriteria(), definition.criteriaType(),
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
        return badgeRepository.findAllDefinitions().stream().map(this::toMap).toList();
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

    private BadgeMetric metricFor(Badge definition, BadgeProgress progress) {
        String criteriaType = definition.criteriaType();
        int target = definition.target();
        int current = switch (criteriaType) {
            case "checkInCount" -> progress.visits();
            case "reviewCount" -> progress.reviews();
            case "pointsEarned" -> progress.points();
            case "completedChallengeCount" -> progress.challenges();
            default -> 0;
        };
        return new BadgeMetric(current, target);
    }

    private Badge normalize(Map<String, Object> input) {
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
        int maximumTarget = "pointsEarned".equals(criteriaType)
                ? POINTS_TARGET_MAXIMUM : ACTIVITY_TARGET_MAXIMUM;
        if (target > maximumTarget) {
            throw new IllegalArgumentException("Badge target must not exceed " + maximumTarget + ".");
        }

        return new Badge(null, name, emoji, description, unlockCriteria, criteriaType, target);
    }

    private Map<String, Instant> earnedByBadgeId(List<TouristBadge> awards) {
        Map<String, Instant> earned = new HashMap<>();
        awards.forEach(award -> earned.put(award.badgeId(), award.dateEarned()));
        return earned;
    }

    private Map<String, Object> toMap(Badge badge) {
        Map<String, Object> result = new HashMap<>();
        result.put("badgeId", badge.badgeId());
        result.put("name", badge.name());
        result.put("emoji", badge.emoji());
        result.put("description", badge.description());
        result.put("unlockCriteria", badge.unlockCriteria());
        result.put("criteriaType", badge.criteriaType());
        result.put("target", badge.target());
        return result;
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
