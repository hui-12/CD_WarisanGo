package com.warisango.model.service;

import com.google.cloud.firestore.GeoPoint;
import com.warisango.dto.AIExtractionResponse;
import com.warisango.dto.AIExtractionResult;
import com.warisango.exception.AIProcessingException;
import com.warisango.model.repository.HeritageBusinessRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HeritageBusinessPersistenceService {

    private static final String PENDING_STATUS = "Pending";
    private static final int DEFAULT_CHECK_IN_POINTS = 50;
    private static final Pattern COORDINATE_PATTERN = Pattern.compile(
            "([+-]?\\d+(?:\\.\\d+)?)\\s*°?\\s*([NS])?\\s*,\\s*"
                    + "([+-]?\\d+(?:\\.\\d+)?)\\s*°?\\s*([EW])?",
            Pattern.CASE_INSENSITIVE
    );

    private final HeritageBusinessRepository heritageBusinessRepository;

    public HeritageBusinessPersistenceService(
            HeritageBusinessRepository heritageBusinessRepository) {

        this.heritageBusinessRepository = heritageBusinessRepository;
    }

    public void save(AIExtractionResult extraction, String sourceVideoLink) {
        if (extraction == null || extraction.getBusinesses() == null
                || extraction.getBusinesses().isEmpty()) {
            throw new AIProcessingException(
                    "Gemini extraction did not contain any businesses."
            );
        }

        List<Map<String, Object>> businesses = extraction.getBusinesses()
                .stream()
                .map(business -> toDocument(business, sourceVideoLink))
                .toList();

        heritageBusinessRepository.saveAll(businesses);
    }

    private Map<String, Object> toDocument(
            AIExtractionResponse business,
            String sourceVideoLink) {

        Map<String, Object> document = new LinkedHashMap<>();
        document.put("address", nullIfBlank(business.address()));
        document.put("averageRating", validRating(business.averageRating()));
        document.put("checkInPoints", validCheckInPoints(business.checkInPoints()));
        document.put("city", nullIfBlank(business.city()));
        document.put("description", nullIfBlank(business.description()));
        document.put("location", parseLocation(business.location()));
        document.put("name", nullIfBlank(business.name()));
        document.put("operatingHour", nullIfBlank(business.operatingHour()));
        document.put("sourceVideoLink", nullIfBlank(sourceVideoLink));
        document.put("state", nullIfBlank(business.state()));
        document.put("status", PENDING_STATUS);
        document.put("approveAt", null);
        document.put("rejectAt", null);
        return document;
    }

    private GeoPoint parseLocation(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }

        String normalizedLocation = location.trim()
                .replace("[", "")
                .replace("]", "")
                .replace("°", "")
                .replace("Â", "");
        Matcher matcher = COORDINATE_PATTERN.matcher(normalizedLocation);
        if (!matcher.matches()) {
            return null;
        }

        try {
            double latitude = applyDirection(
                    Double.parseDouble(matcher.group(1)),
                    matcher.group(2)
            );
            double longitude = applyDirection(
                    Double.parseDouble(matcher.group(3)),
                    matcher.group(4)
            );

            if (latitude < -90 || latitude > 90
                    || longitude < -180 || longitude > 180) {
                return null;
            }

            return new GeoPoint(latitude, longitude);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private double applyDirection(double coordinate, String direction) {
        if (direction == null) {
            return coordinate;
        }
        return "S".equalsIgnoreCase(direction) || "W".equalsIgnoreCase(direction)
                ? -Math.abs(coordinate)
                : Math.abs(coordinate);
    }

    private Double validRating(Double averageRating) {
        return averageRating != null && averageRating >= 0 && averageRating <= 5
                ? averageRating
                : null;
    }

    private int validCheckInPoints(Integer checkInPoints) {
        return checkInPoints != null && checkInPoints > 0
                ? checkInPoints
                : DEFAULT_CHECK_IN_POINTS;
    }

    private String nullIfBlank(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
