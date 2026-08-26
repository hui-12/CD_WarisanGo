package com.warisango.model.repository;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.warisango.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class UserRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    private static final String COLLECTION_NAME = "users";

    public Optional<User> findById(String uid) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentSnapshot document = db.collection(COLLECTION_NAME).document(uid).get().get();
            if (document.exists()) {
                return Optional.ofNullable(document.toObject(User.class));
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error retrieving user with ID: {}", uid, e);
            Thread.currentThread().interrupt();
        }
        return Optional.empty();
    }

    public void save(User user) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            db.collection(COLLECTION_NAME).document(user.getUid()).set(user).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error saving user with ID: {}", user.getUid(), e);
            Thread.currentThread().interrupt();
        }
    }
}
