package com.warisango.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.warisango.model.PointsHistory;
import com.warisango.model.User;
import com.warisango.util.TierCalculator;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PointsRepository {
    private static final String USERS = "users";
    private static final String POINTS_HISTORIES = "pointsHistories";

    private final Firestore firestore;

    public PointsRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public int getCurrentPoints(String userId) throws Exception {
        DocumentSnapshot user = firestore.collection(USERS).document(userId).get().get();
        Long points = user.exists() ? user.getLong("totalPoints") : null;
        return points == null ? 0 : points.intValue();
    }

    public int addPoints(String userId, int points, String activityType, String relatedChallengeId)
            throws Exception {
        DocumentReference user = firestore.collection(USERS).document(userId);
        DocumentReference history = firestore.collection(POINTS_HISTORIES).document();

        return firestore.runTransaction(transaction -> {
            DocumentSnapshot userSnapshot = transaction.get(user).get();
            Long storedPoints = userSnapshot.exists() ? userSnapshot.getLong("totalPoints") : null;
            int updatedPoints = (storedPoints == null ? 0 : storedPoints.intValue()) + points;

            Map<String, Object> userUpdate = new HashMap<>();
            userUpdate.put("totalPoints", updatedPoints);
            userUpdate.put("tierStatus", TierCalculator.tierFor(updatedPoints));
            transaction.set(user, userUpdate, SetOptions.merge());

            Map<String, Object> historyData = new HashMap<>();
            historyData.put("activityDate", FieldValue.serverTimestamp());
            historyData.put("activityType", activityType);
            historyData.put("pointsEarned", points);
            historyData.put("relatedChallengeId", relatedChallengeId);
            historyData.put("touristId", userId);
            historyData.put("transactionId", history.getId());
            transaction.set(history, historyData);
            return updatedPoints;
        }).get();
    }

    public List<PointsHistory> findHistory(String touristId) throws Exception {
        List<PointsHistory> history = new ArrayList<>();
        for (DocumentSnapshot document : firestore.collection(POINTS_HISTORIES)
                .whereEqualTo("touristId", touristId).get().get().getDocuments()) {
            Long points = document.getLong("pointsEarned");
            var timestamp = document.getTimestamp("activityDate");
            history.add(new PointsHistory(
                    valueOrFallback(document.getString("transactionId"), document.getId()),
                    document.getString("activityType"),
                    points == null ? 0 : points.intValue(),
                    timestamp == null ? null : timestamp.toDate().toInstant(),
                    document.getString("relatedChallengeId"),
                    touristId
            ));
        }
        return history;
    }

    public List<User> findTouristsForLeaderboard() throws Exception {
        List<User> entries = new ArrayList<>();
        for (DocumentSnapshot document : firestore.collection(USERS).get().get().getDocuments()) {
            if (!"tourist".equalsIgnoreCase(document.getString("role"))) continue;
            User user = document.toObject(User.class);
            if (user != null) {
                if (user.getUserId() == null || user.getUserId().isBlank()) {
                    user.setUserId(document.getId());
                }
                entries.add(user);
            }
        }
        return entries;
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
