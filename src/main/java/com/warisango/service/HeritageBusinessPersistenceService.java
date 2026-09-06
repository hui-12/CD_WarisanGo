package com.warisango.service;

import com.warisango.dto.AIExtractionResponse;
import com.warisango.dto.AIExtractionResult;
import com.warisango.exception.AIProcessingException;
import com.warisango.model.GeoCoordinates;
import com.warisango.model.HeritageBusiness;
import com.warisango.repository.BusinessRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HeritageBusinessPersistenceService {

    private static final String PENDING_STATUS = "Pending";
    private static final int DEFAULT_CHECK_IN_POINTS = 50;
    private static final Pattern COORDINATE_PATTERN = Pattern.compile(
            "([+-]?\\d+(?:\\.\\d+)?)\\s*\\u00B0?\\s*([NS])?\\s*,\\s*"
                    + "([+-]?\\d+(?:\\.\\d+)?)\\s*\\u00B0?\\s*([EW])?",
            Pattern.CASE_INSENSITIVE
    );

    private final BusinessRepository heritageBusinessRepository;

    public HeritageBusinessPersistenceService(
            BusinessRepository heritageBusinessRepository) {

        this.heritageBusinessRepository = heritageBusinessRepository;
    }

    public void save(AIExtractionResult extraction, String sourceVideoLink) {
        if (extraction == null || extraction.getBusinesses() == null
                || extraction.getBusinesses().isEmpty()) {
            throw new AIProcessingException(
                    "Gemini extraction did not contain any businesses."
            );
        }

        List<HeritageBusiness> businesses = extraction.getBusinesses()
                .stream()
                .map(business -> toDocument(business, sourceVideoLink))
                .toList();

        heritageBusinessRepository.saveAll(businesses);
    }

    private HeritageBusiness toDocument(
            AIExtractionResponse business,
            String sourceVideoLink) {
        GeoCoordinates location = parseLocation(business.location());
        return new HeritageBusiness(
                null,
                nullIfBlank(business.name()),
                nullIfBlank(business.address()),
                nullIfBlank(business.state()),
                nullIfBlank(business.city()),
                nullIfBlank(business.description()),
                location == null ? null : location.latitude(),
                location == null ? null : location.longitude(),
                nullIfBlank(business.operatingHour()),
                nullIfBlank(sourceVideoLink),
                PENDING_STATUS,
                validRating(business.averageRating()),
                validCheckInPoints(business.checkInPoints()),
                null,
                null,
                null);
    }

    private GeoCoordinates parseLocation(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }

        String normalizedLocation = location.trim()
                .replace("[", "")
                .replace("]", "")
                .replace("\u00B0", "");
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

            return new GeoCoordinates(latitude, longitude);
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
