package com.warisango.model.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Reads display names from the root-level Tourists and Users collections.
 *
 * Reviews and Comments intentionally keep touristId as their relationship key.
 * This repository only resolves the name needed by the UI.
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private static final String TOURISTS_COLLECTION = "Tourists";
    private static final String USERS_COLLECTION = "Users";

    private final Firestore firestore;

    public UserRepositoryImpl() {
        this.firestore = FirestoreClient.getFirestore();
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
        // as a field, without changing the existing Users collection design.
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
