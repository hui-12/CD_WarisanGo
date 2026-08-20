package com.warisango.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import com.google.firebase.cloud.FirestoreClient;
import com.warisango.exception.FirebasePersistenceException;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class HeritageBusinessRepository {

    private static final String COLLECTION_NAME = "HeritageBusinesses";

    public synchronized void saveAll(List<Map<String, Object>> businesses) {
        Firestore firestore = FirestoreClient.getFirestore();

        try {
            int currentCount = firestore
                    .collection(COLLECTION_NAME)
                    .get()
                    .get()
                    .size();

            WriteBatch batch = firestore.batch();

            for (int index = 0; index < businesses.size(); index++) {
                String businessId = String.format(
                        "hb_%03d",
                        currentCount + index + 1
                );

                DocumentReference document = firestore
                        .collection(COLLECTION_NAME)
                        .document(businessId);

                Map<String, Object> storedBusiness =
                        new LinkedHashMap<>(businesses.get(index));
                storedBusiness.put("businessId", businessId);
                storedBusiness.put("createdAt", FieldValue.serverTimestamp());
                batch.set(document, storedBusiness);
            }

            batch.commit().get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new FirebasePersistenceException(
                    "Saving heritage businesses was interrupted.",
                    exception
            );
        } catch (Exception exception) {
            throw new FirebasePersistenceException(
                    "Unable to save heritage businesses.",
                    exception
            );
        }
    }
}
