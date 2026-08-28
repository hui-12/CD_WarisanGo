package com.warisango.model.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.Timestamp;
import com.warisango.model.User;
import com.warisango.util.TierCalculator;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Reads and writes user data from the root-level users collection.
 *
 * Reviews and Comments intentionally keep touristId as their relationship key.
 * This repository only resolves the name needed by the UI.
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private static final String USERS_COLLECTION = "users";
    private static final String TOURISTS_COLLECTION = "Tourists";

    private final Firestore firestore;

    public UserRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public Optional<User> findById(String uid) {
        try {
            DocumentSnapshot document = firestore
                    .collection(USERS_COLLECTION)
                    .document(uid)
                    .get()
                    .get();
            return document.exists()
                    ? Optional.ofNullable(document.toObject(User.class))
                    : Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve user: " + uid, e);
        }
    }

    @Override
    public void save(User user) {
        try {
            firestore
                    .collection(USERS_COLLECTION)
                    .document(user.getUid())
                    .set(user)
                    .get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save user: " + user.getUid(), e);
        }
    }

    @Override
    public User initializeMissingProfileFields(User user) {
        try {
            DocumentReference reference = getUserRef(user.getUid());
            DocumentSnapshot document = reference.get().get();
            Map<String, Object> missingFields = new HashMap<>();
            putIfMissing(document, missingFields, "email", user.getEmail());
            putIfMissing(document, missingFields, "name", user.getName());
            putIfMissing(document, missingFields, "avatar", user.getAvatar());
            putIfMissing(document, missingFields, "role", user.getRole());
            putIfMissing(document, missingFields, "totalPoints", 0L);
            Long storedPoints = document.getLong("totalPoints");
            int totalPoints = storedPoints == null ? 0 : storedPoints.intValue();
            String calculatedTier = TierCalculator.tierFor(totalPoints);
            if (!calculatedTier.equalsIgnoreCase(String.valueOf(document.get("tierStatus")))) {
                missingFields.put("tierStatus", calculatedTier);
            }
            putIfMissing(document, missingFields, "gender", null);
            putIfMissing(document, missingFields, "aboutMe", null);
            putIfMissing(document, missingFields, "createdAt", Timestamp.now());
            if (!missingFields.isEmpty()) {
                reference.update(missingFields).get();
                return reference.get().get().toObject(User.class);
            }
            return user;
        } catch (Exception exception) {
            throw new RuntimeException("Failed to initialize profile fields: " + user.getUid(), exception);
        }
    }

    @Override
    public void updateProfile(String uid, String name, String gender, String aboutMe) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("gender", gender);
            updates.put("aboutMe", aboutMe);
            getUserRef(uid).update(updates).get();
        } catch (Exception exception) {
            throw new RuntimeException("Failed to update profile: " + uid, exception);
        }
    }

    private void putIfMissing(DocumentSnapshot document, Map<String, Object> updates,
                              String field, Object value) {
        if (!document.contains(field)) {
            updates.put(field, value);
        }
    }

    @Override
    public DocumentSnapshot getUser(String userId)
            throws ExecutionException, InterruptedException {

        return getUserRef(userId).get().get();
    }

    @Override
    public DocumentReference getUserRef(String userId) {
        return firestore.collection(USERS_COLLECTION).document(userId);
    }

    @Override
    public int getOrCreateCurrentPoints(String userId)
            throws ExecutionException, InterruptedException {

        DocumentReference userReference = getUserRef(userId);
        DocumentSnapshot userDocument = userReference.get().get();

        if (!userDocument.exists()) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", userId);
            userData.put("totalPoints", 0L);
            userReference.set(userData).get();
            return 0;
        }

        Long totalPoints = userDocument.getLong("totalPoints");
        return totalPoints == null ? 0 : totalPoints.intValue();
    }

    @Override
    public String findDisplayNameByTouristId(String touristId) {
        if (isBlank(touristId)) {
            return "Visitor";
        }

        try {
            DocumentSnapshot tourist = firestore
                    .collection(TOURISTS_COLLECTION)
                    .document(touristId)
                    .get()
                    .get();

            if (tourist.exists()) {
                String directName = firstText(tourist, "name", "displayName");
                if (!isBlank(directName)) {
                    return directName;
                }

                String userId = firstText(tourist, "userId", "userID");
                String userName = findNameByUserId(userId);
                if (!isBlank(userName)) {
                    return userName;
                }
            }

            // This fallback supports the development data where the user id and
            // tourist id use the same numeric suffix (user_001/tourist_001).
            String derivedUserId = deriveUserId(touristId);
            String derivedName = findNameByUserId(derivedUserId);
            if (!isBlank(derivedName)) {
                return derivedName;
            }

            return touristId;
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve display name for tourist: " + touristId, e);
        }
    }

    private String findNameByUserId(String userId) throws Exception {
        if (isBlank(userId)) {
            return "";
        }

        DocumentSnapshot userDocument = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .get();

        String name = userDocument.exists()
                ? firstText(userDocument, "name", "displayName")
                : "";
        if (!isBlank(name)) {
            return name;
        }

        // Also support data whose document id is different but userId is stored
        // as a field, without changing the existing users collection design.
        ApiFuture<QuerySnapshot> query = firestore
                .collection(USERS_COLLECTION)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get();

        List<QueryDocumentSnapshot> documents = query.get().getDocuments();
        return documents.isEmpty()
                ? ""
                : firstText(documents.get(0), "name", "displayName");
    }

    private String deriveUserId(String touristId) {
        return touristId.startsWith("tourist_")
                ? "user_" + touristId.substring("tourist_".length())
                : "";
    }

    private String firstText(DocumentSnapshot document, String... fields) {
        for (String field : fields) {
            String value = document.getString(field);
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
