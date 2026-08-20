package com.warisango.model.repository;

/**
 * Repository contract for user display information used by community content.
 */
public interface UserRepository {

    /**
     * Resolves a tourist id such as tourist_001 to the user's display name.
     *
     * @param touristId the id stored on Reviews and Comments
     * @return the user's name, or the tourist id when no name is available
     */
    String findDisplayNameByTouristId(String touristId);
}
