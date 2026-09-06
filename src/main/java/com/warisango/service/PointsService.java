package com.warisango.service;

import com.warisango.dto.LeaderboardEntryDTO;
import com.warisango.dto.PointHistoryDTO;
import com.warisango.repository.PointsRepository;
import com.warisango.util.TierCalculator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class PointsService {
    private final PointsRepository pointsRepository;

    public PointsService(PointsRepository pointsRepository) {
        this.pointsRepository = pointsRepository;
    }

    public int getCurrentPoints(String userId) throws Exception {
        return pointsRepository.getCurrentPoints(userId);
    }

    public int addPoints(String userId, int points, String type, String description, String referenceId)
            throws Exception {
        String challengeId = "challenge".equalsIgnoreCase(type) ? referenceId : null;
        return pointsRepository.addPoints(userId, points, type, challengeId);
    }

    public List<PointHistoryDTO> getHistory(String userId) throws Exception {
        return pointsRepository.findHistory(userId).stream()
                .map(history -> new PointHistoryDTO(
                        history.transactionId(),
                        history.activityType(),
                        null,
                        history.pointsEarned(),
                        history.activityDate(),
                        history.relatedChallengeId()))
                .map(this::withDescription)
                .sorted(Comparator.comparing(
                        PointHistoryDTO::timestamp,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List<LeaderboardEntryDTO> getLeaderboard() throws Exception {
        List<LeaderboardEntryDTO> sorted = pointsRepository.findTouristsForLeaderboard().stream()
                .map(user -> new LeaderboardEntryDTO(
                        0,
                        user.getUid(),
                        user.getName() == null || user.getName().isBlank() ? "WarisanGo Tourist" : user.getName(),
                        user.getTotalPoints(),
                        TierCalculator.tierFor(user.getTotalPoints())))
                .sorted(Comparator.comparingInt(LeaderboardEntryDTO::points).reversed()
                        .thenComparing(LeaderboardEntryDTO::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        List<LeaderboardEntryDTO> ranked = new ArrayList<>();
        for (int index = 0; index < sorted.size(); index++) {
            ranked.add(sorted.get(index).withRank(index + 1));
        }
        return ranked;
    }

    public Optional<LeaderboardEntryDTO> getLeaderboardEntry(String userId) throws Exception {
        return getLeaderboard().stream()
                .filter(entry -> entry.userId().equals(userId))
                .findFirst();
    }

    private PointHistoryDTO withDescription(PointHistoryDTO history) {
        String type = history.type() == null ? "points" : history.type();
        String label = switch (type.toLowerCase()) {
            case "checkin" -> "Heritage business check-in";
            case "challenge" -> "Challenge reward";
            case "review" -> "Review reward";
            case "badge" -> "Badge milestone reward";
            default -> "Points activity";
        };
        return new PointHistoryDTO(history.transactionId(), type, label, history.points(), history.timestamp(),
                history.relatedChallengeId());
    }
}
