package com.warisango.repository;

import com.warisango.exception.FirebasePersistenceException;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.stereotype.Repository;

/**
 * Reads admin membership from the root-level admins collection.
 */
@Repository
public class AdminRepository {

    private static final String COLLECTION = "admins";
    private final Firestore firestore;

    public AdminRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public boolean existsByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return false;
        }

        try {
            return !firestore
                    .collection(COLLECTION)
                    .whereEqualTo("userId", userId)
                    .limit(1)
                    .get()
                    .get()
                    .isEmpty();
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to verify Admin membership.", e);
        }
    }

    public String findAdminIdByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return "";
        }

        try {
            for (QueryDocumentSnapshot document : firestore
                    .collection(COLLECTION)
                    .whereEqualTo("userId", userId)
                    .limit(1)
                    .get()
                    .get()
                    .getDocuments()) {
                String adminId = document.getString("adminId");
                return adminId == null || adminId.isBlank()
                        ? document.getId()
                        : adminId;
            }
            return "";
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to load Admin ID.", e);
        }
    }
}
