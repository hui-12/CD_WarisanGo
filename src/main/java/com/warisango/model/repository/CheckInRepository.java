package com.warisango.model.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.SetOptions;
import com.warisango.model.CheckIn;
import com.warisango.util.TierCalculator;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class CheckInRepository {
    private static final String CHECK_INS = "checkIns";
    private static final String POINTS_HISTORIES = "pointsHistories";
    private static final String USERS = "users";

    private final Firestore firestore;

    public CheckInRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public boolean hasCheckedInToday(String touristId, String businessId, ZoneId zoneId)
            throws ExecutionException, InterruptedException {
        return firestore.collection(CHECK_INS)
                .whereEqualTo("touristId", touristId)
                .get()
                .get()
                .getDocuments()
                .stream()
                .filter(document -> businessId.equals(document.getString("businessId")))
                .map(document -> document.getTimestamp("checkInTimestamp"))
                .filter(timestamp -> timestamp != null)
                .map(timestamp -> timestamp.toDate().toInstant().atZone(zoneId).toLocalDate())
                .anyMatch(LocalDate.now(zoneId)::equals);
    }

    public int getCurrentPoints(String touristId) throws ExecutionException, InterruptedException {
        DocumentSnapshot user = firestore.collection(USERS).document(touristId).get().get();
        Long points = user.exists() ? user.getLong("totalPoints") : null;
        return points == null ? 0 : points.intValue();
    }

    public List<CheckIn> findByTouristId(String touristId)
            throws ExecutionException, InterruptedException {
        return firestore.collection(CHECK_INS)
                .whereEqualTo("touristId", touristId)
                .get()
                .get()
                .getDocuments()
                .stream()
                .map(document -> document.toObject(CheckIn.class))
                .filter(checkIn -> checkIn.getCheckInTimestamp() != null)
                .sorted((left, right) -> right.getCheckInTimestamp().compareTo(left.getCheckInTimestamp()))
                .toList();
    }

    public int saveAndAwardPoints(String touristId, String businessId, GeoPoint gpsLocation,
                                  int pointsAwarded) throws ExecutionException, InterruptedException {
        DocumentReference userReference = firestore.collection(USERS).document(touristId);
        DocumentReference checkInReference = firestore.collection(CHECK_INS).document();
        DocumentReference historyReference = firestore.collection(POINTS_HISTORIES).document();

        return firestore.runTransaction(transaction -> {
            DocumentSnapshot user = transaction.get(userReference).get();
            Long storedPoints = user.exists() ? user.getLong("totalPoints") : null;
            int updatedPoints = (storedPoints == null ? 0 : storedPoints.intValue()) + pointsAwarded;

            Map<String, Object> userUpdate = new HashMap<>();
            userUpdate.put("totalPoints", updatedPoints);
            userUpdate.put("tierStatus", TierCalculator.tierFor(updatedPoints));
            transaction.set(userReference, userUpdate, SetOptions.merge());

            Map<String, Object> checkIn = new HashMap<>();
            checkIn.put("businessId", businessId);
            checkIn.put("checkInId", checkInReference.getId());
            checkIn.put("checkInTimestamp", FieldValue.serverTimestamp());
            checkIn.put("gpsLocation", gpsLocation);
            checkIn.put("pointsAwarded", pointsAwarded);
            checkIn.put("touristId", touristId);
            transaction.set(checkInReference, checkIn);

            Map<String, Object> history = new HashMap<>();
            history.put("activityDate", FieldValue.serverTimestamp());
            history.put("activityType", "checkIn");
            history.put("pointsEarned", pointsAwarded);
            history.put("relatedChallengeId", null);
            history.put("touristId", touristId);
            history.put("transactionId", historyReference.getId());
            transaction.set(historyReference, history);

            return updatedPoints;
        }).get();
    }
}
