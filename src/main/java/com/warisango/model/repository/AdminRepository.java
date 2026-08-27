package com.warisango.model.repository;

import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

@Repository
public class AdminRepository {
    public boolean existsByUserId(String userId) {
        try {
            return !FirestoreClient.getFirestore().collection("Admins")
                    .whereEqualTo("userId", userId).limit(1).get().get().isEmpty();
        } catch (Exception exception) {
            return false;
        }
    }
}
