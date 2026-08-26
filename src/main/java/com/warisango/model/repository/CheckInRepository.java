package com.warisango.model.repository;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.Timestamp;
import com.warisango.exception.FirebasePersistenceException;
import com.warisango.model.CheckInRecord;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class CheckInRepository {
    private static final String COLLECTION_NAME = "CheckIns";
    private final Firestore firestore;

    public CheckInRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public boolean hasCheckedInBefore(String userId, String businessId)
            throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("businessId", businessId)
                .limit(1)
                .get()
                .get();

        return !snapshot.isEmpty();
    }

    public List<CheckInRecord> findByUserId(String userId) {
        try {
            QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .get()
                    .get();

            List<CheckInRecord> records = new ArrayList<>();
            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                Timestamp timestamp = document.getTimestamp("timestamp");
                Long points = document.getLong("pointsEarned");
                records.add(new CheckInRecord(
                        document.getString("businessName"),
                        points == null ? 0 : points.intValue(),
                        timestamp == null ? null : timestamp.toDate().toInstant()));
            }
            return records;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new FirebasePersistenceException("Unable to load recent visits.", exception);
        } catch (ExecutionException exception) {
            throw new FirebasePersistenceException("Unable to load recent visits.", exception);
        }
    }
}
