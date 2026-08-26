package com.warisango.model.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.warisango.model.User;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Repository contract for user persistence and display-name lookup.
 */
public interface UserRepository {

    Optional<User> findById(String uid);

    void save(User user);

    String findDisplayNameByTouristId(String touristId);

    DocumentSnapshot getUser(String userId) throws ExecutionException, InterruptedException;

    DocumentReference getUserRef(String userId);

    int getOrCreateCurrentPoints(String userId) throws ExecutionException, InterruptedException;
}
