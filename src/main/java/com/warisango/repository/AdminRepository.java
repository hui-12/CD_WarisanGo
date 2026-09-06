package com.warisango.repository;

import com.warisango.exception.FirebasePersistenceException;
import com.warisango.model.Admin;

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
            return findByUserId(userId) != null;
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to verify Admin membership.", e);
        }
    }

    private Admin findByUserId(String userId) throws Exception {
        for (QueryDocumentSnapshot document : firestore
                    .collection(COLLECTION)
                    .whereEqualTo("userId", userId)
                    .limit(1)
                    .get()
                    .get()
                    .getDocuments()) {
            String adminId = document.getString("adminId");
            return new Admin(adminId == null || adminId.isBlank() ? document.getId() : adminId,
                    document.getString("userId"));
        }
        return null;
    }

    public String findAdminIdByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return "";
        }

        try {
            Admin admin = findByUserId(userId);
            return admin == null ? "" : admin.adminId();
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to load Admin ID.", e);
        }
    }
}
