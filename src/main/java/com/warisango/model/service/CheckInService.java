package com.warisango.model.service;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;
import com.google.firebase.cloud.FirestoreClient;
import com.warisango.dto.CheckInRequest;
import com.warisango.dto.CheckInResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class CheckInService {
    private static final String BUSINESS_COLLECTION = "HeritageBusinesses";
    private static final String USER_COLLECTION = "Users";
    private static final String CHECKIN_COLLECTION = "CheckIns";
    private static final double MAX_DISTANCE_METERS = 50.0;

    public CheckInResponse processCheckIn(CheckInRequest request) {
        try {
            validateRequest(request);

            Firestore db = FirestoreClient.getFirestore();
            DocumentReference businessRef = db.collection(BUSINESS_COLLECTION)
                    .document(request.getBusinessId());
            DocumentReference userRef = db.collection(USER_COLLECTION)
                    .document(request.getUserId());

            DocumentSnapshot business = businessRef.get().get();
            if (!business.exists()) {
                return failure("Business not found.", 0, request.getDistanceMeters());
            }

            String status = business.getString("status");
            if (status != null && !"APPROVED".equalsIgnoreCase(status)) {
                return failure("This business is not available for check-in.", 0, request.getDistanceMeters());
            }

            GeoPoint location = business.getGeoPoint("location");
            if (location == null) {
                return failure("This business does not have a valid GPS location.", 0, request.getDistanceMeters());
            }

            // Recalculate the distance on the server. The value sent by the browser is not trusted.
            double serverDistance = GeoUtils.distanceMeters(
                    request.getUserLatitude(),
                    request.getUserLongitude(),
                    location.getLatitude(),
                    location.getLongitude()
            );

            if (serverDistance > MAX_DISTANCE_METERS) {
                int currentPoints = getCurrentPoints(userRef);
                return failure(
                        String.format("You are %.1f metres away. You must be within 50 metres to check in.", serverDistance),
                        currentPoints,
                        serverDistance
                );
            }

            int checkInPoints = getCheckInPoints(business);

            // A simple duplicate protection: one successful check-in per user/business.
            // This can later be changed to a time-based cooldown if required.
            boolean alreadyCheckedIn = !db.collection(CHECKIN_COLLECTION)
                    .whereEqualTo("userId", request.getUserId())
                    .whereEqualTo("businessId", request.getBusinessId())
                    .limit(1)
                    .get()
                    .get()
                    .isEmpty();

            if (alreadyCheckedIn) {
                int currentPoints = getCurrentPoints(userRef);
                return failure("You have already checked in at this business.", currentPoints, serverDistance);
            }

            int newPoints = db.runTransaction(transaction -> {
                DocumentSnapshot userSnapshot = transaction.get(userRef).get();
                int currentPoints = 0;

                if (userSnapshot.exists()) {
                    Long value = userSnapshot.getLong("currentPoints");
                    if (value != null) {
                        currentPoints = value.intValue();
                    }
                }

                int updatedPoints = currentPoints + checkInPoints;

                Map<String, Object> userData = new HashMap<>();
                userData.put("userId", request.getUserId());
                userData.put("currentPoints", updatedPoints);
                transaction.set(userRef, userData, com.google.cloud.firestore.SetOptions.merge());

                DocumentReference checkInRef = db.collection(CHECKIN_COLLECTION).document();
                Map<String, Object> checkInData = new HashMap<>();
                checkInData.put("userId", request.getUserId());
                checkInData.put("businessId", request.getBusinessId());
                checkInData.put("businessName", business.getString("name"));
                checkInData.put("pointsEarned", checkInPoints);
                checkInData.put("distanceMeters", serverDistance);
                checkInData.put("userLocation", new GeoPoint(
                        request.getUserLatitude(), request.getUserLongitude()));
                checkInData.put("businessLocation", location);
                checkInData.put("timestamp", FieldValue.serverTimestamp());
                transaction.set(checkInRef, checkInData);

                return updatedPoints;
            }).get();

            return new CheckInResponse(
                    true,
                    "Check-in successful.",
                    checkInPoints,
                    newPoints,
                    serverDistance
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return failure("The check-in operation was interrupted.", 0, request.getDistanceMeters());
        } catch (ExecutionException | RuntimeException e) {
            return failure("Unable to complete check-in. Please try again.", 0, request.getDistanceMeters());
        }
    }

    private int getCurrentPoints(DocumentReference userRef)
            throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = userRef.get().get();
        if (!snapshot.exists()) {
            return 0;
        }
        Long points = snapshot.getLong("currentPoints");
        return points == null ? 0 : points.intValue();
    }

    private int getCheckInPoints(DocumentSnapshot business) {
        Long points = business.getLong("checkInPoints");
        return points != null && points > 0 ? points.intValue() : 50;
    }

    private void validateRequest(CheckInRequest request) {
        if (request == null || request.getUserId() == null || request.getUserId().isBlank()) {
            throw new IllegalArgumentException("User ID is required.");
        }
        if (request.getBusinessId() == null || request.getBusinessId().isBlank()) {
            throw new IllegalArgumentException("Business ID is required.");
        }
        if (!validCoordinate(request.getUserLatitude(), request.getUserLongitude())) {
            throw new IllegalArgumentException("Invalid user GPS coordinates.");
        }
    }

    private boolean validCoordinate(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90
                && longitude >= -180 && longitude <= 180;
    }

    private CheckInResponse failure(String message, int currentPoints, double distanceMeters) {
        return new CheckInResponse(false, message, 0, currentPoints, distanceMeters);
    }
}
