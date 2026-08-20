package com.warisango.model.repository;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ExecutionException;

@Repository
public class CheckInRepository {
    private static final String COLLECTION_NAME = "CheckIns";

    public boolean hasCheckedInBefore(String userId, String businessId)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        QuerySnapshot snapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("businessId", businessId)
                .limit(1)
                .get()
                .get();

        return !snapshot.isEmpty();
    }
}
