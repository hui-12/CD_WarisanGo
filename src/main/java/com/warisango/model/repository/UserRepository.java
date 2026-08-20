package com.warisango.model.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class UserRepository {
    private static final String COLLECTION_NAME = "Users";

    public DocumentSnapshot getUser(String userId) throws ExecutionException, InterruptedException {
        return getUserRef(userId).get().get();
    }

    public DocumentReference getUserRef(String userId) {
        Firestore db = FirestoreClient.getFirestore();
        return db.collection(COLLECTION_NAME).document(userId);
    }

    public int getOrCreateCurrentPoints(String userId) throws ExecutionException, InterruptedException {
        DocumentReference ref = getUserRef(userId);
        DocumentSnapshot snapshot = ref.get().get();

        if (!snapshot.exists()) {
            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("currentPoints", 0L);
            ref.set(data).get();
            return 0;
        }

        Long points = snapshot.getLong("currentPoints");
        return points == null ? 0 : points.intValue();
    }
}
