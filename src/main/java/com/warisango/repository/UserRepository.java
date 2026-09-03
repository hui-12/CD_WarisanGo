package com.warisango.repository;

import com.warisango.exception.FirebasePersistenceException;

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
 * Reviews and comments retain their existing touristId field for Firestore
 * compatibility, but its value is now the authenticated Firebase user UID.
 */
@Repository
public class UserRepository {

    private static final String USERS_COLLECTION = "users";

    private final Firestore firestore;

    public UserRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Optional<User> findById(String uid) {
        try {
            DocumentSnapshot document = firestore
                    .collection(USERS_COLLECTION)
                    .document(uid)
                    .get()
                    .get();
            if (!document.exists()) {
                return Optional.empty();
            }

            User user = document.toObject(User.class);
            if (user != null && isBlank(user.getUid())) {
                user.setUid(uid);
            }
            return Optional.ofNullable(user);
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to retrieve user: " + uid, e);
        }
    }

    public void save(User user) {
        try {
            firestore
                    .collection(USERS_COLLECTION)
                    .document(user.getUid())
                    .set(user)
                    .get();
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to save user: " + user.getUid(), e);
        }
    }

    public User initializeMissingProfileFields(User user) {
        try {
            DocumentReference reference = getUserRef(user.getUid());
            DocumentSnapshot document = reference.get().get();
            Map<String, Object> missingFields = new HashMap<>();
            putIfMissing(document, missingFields, "uid", user.getUid());
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
                User initializedUser = reference.get().get().toObject(User.class);
                if (initializedUser != null && isBlank(initializedUser.getUid())) {
                    initializedUser.setUid(user.getUid());
                }
                return initializedUser;
            }
            if (isBlank(user.getUid())) {
                user.setUid(reference.getId());
            }
            return user;
        } catch (Exception exception) {
            throw new FirebasePersistenceException("Failed to initialize profile fields: " + user.getUid(), exception);
        }
    }

    public void updateProfile(String uid, String name, String gender, String aboutMe) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("gender", gender);
            updates.put("aboutMe", aboutMe);
            getUserRef(uid).update(updates).get();
        } catch (Exception exception) {
            throw new FirebasePersistenceException("Failed to update profile: " + uid, exception);
        }
    }

    private void putIfMissing(DocumentSnapshot document, Map<String, Object> updates,
                              String field, Object value) {
        if (!document.contains(field)) {
            updates.put(field, value);
        }
    }

    public DocumentSnapshot getUser(String userId)
            throws ExecutionException, InterruptedException {

        return getUserRef(userId).get().get();
    }

    public DocumentReference getUserRef(String userId) {
        return firestore.collection(USERS_COLLECTION).document(userId);
    }

    public int getOrCreateCurrentPoints(String userId)
            throws ExecutionException, InterruptedException {

        DocumentReference userReference = getUserRef(userId);
        DocumentSnapshot userDocument = userReference.get().get();

        if (!userDocument.exists()) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("uid", userId);
            userData.put("totalPoints", 0L);
            userReference.set(userData).get();
            return 0;
        }

        Long totalPoints = userDocument.getLong("totalPoints");
        return totalPoints == null ? 0 : totalPoints.intValue();
    }

    public String findDisplayNameByUserId(String userId) {
        if (isBlank(userId)) {
            return "Visitor";
        }

        try {
            String displayName = findNameByUserId(userId);
            return isBlank(displayName) ? userId : displayName;
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to resolve display name for user: " + userId, e);
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
