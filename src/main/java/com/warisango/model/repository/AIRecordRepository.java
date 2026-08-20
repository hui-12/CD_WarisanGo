package com.warisango.model.repository;

import com.google.cloud.firestore.Firestore;
import com.warisango.exception.FirebasePersistenceException;
import com.warisango.model.AIRecord;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ExecutionException;

@Repository
public class AIRecordRepository {

    private static final String COLLECTION_NAME = "aiRecords";

    private final Firestore firestore;

    public AIRecordRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public String save(AIRecord record) {
        try {
            return firestore
                    .collection(COLLECTION_NAME)
                    .add(record)
                    .get()
                    .getId();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new FirebasePersistenceException(
                    "Unable to save the AI record to Firestore.",
                    exception
            );
        } catch (ExecutionException exception) {
            throw new FirebasePersistenceException(
                    "Unable to save the AI record to Firestore.",
                    exception
            );
        }
    }
}
