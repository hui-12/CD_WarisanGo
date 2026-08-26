package com.warisango.model.repository;

import com.warisango.model.User;

import java.util.Optional;

/**
 * Repository contract for user persistence and display-name lookup.
 */
public interface UserRepository {

    Optional<User> findById(String uid);

    void save(User user);

    String findDisplayNameByTouristId(String touristId);
}
