package com.warisango.model.repository;

/**
 * Repository contract for the root-level admins collection.
 */
public interface AdminRepository {

    boolean existsByUserId(String userId);

    String findAdminIdByUserId(String userId);
}
