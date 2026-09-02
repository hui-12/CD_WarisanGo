package com.warisango.dto;

/**
 * Contains heritage business information extracted from a transcript.
 *
 * @param name business name
 * @param address business address
 * @param state state where the business is located
 * @param city city where the business is located
 * @param location geographical location
 * @param description heritage/business description
 * @param operatingHour business operating hours
 * @param averageRating known average rating, or null when unavailable
 * @param checkInPoints points awarded for a verified check-in
 */
public record AIExtractionResponse(
        String name,
        String address,
        String state,
        String city,
        String location,
        String description,
        String operatingHour,
        Double averageRating,
        Integer checkInPoints
) {
}
