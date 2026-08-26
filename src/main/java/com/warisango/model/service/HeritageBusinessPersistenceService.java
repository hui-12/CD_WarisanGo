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

@Service
public class HeritageBusinessPersistenceService {

    private static final String PENDING_STATUS = "Pending";

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
        document.put("averageRating", null);
        document.put("city", nullIfBlank(business.city()));
        document.put("description", nullIfBlank(business.description()));
        document.put("location", parseLocation(business.location()));
        document.put("name", nullIfBlank(business.name()));
        document.put("sourceVideoLink", nullIfBlank(sourceVideoLink));
        document.put("status", PENDING_STATUS);
        document.put("approveAt", null);
        document.put("rejectAt", null);
        return document;
    }

    private GeoPoint parseLocation(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }

        String[] coordinates = location.split(",");
        if (coordinates.length != 2) {
            return null;
        }

        try {
            double latitude = Double.parseDouble(coordinates[0].trim());
            double longitude = Double.parseDouble(coordinates[1].trim());

            if (latitude < -90 || latitude > 90
                    || longitude < -180 || longitude > 180) {
                return null;
            }

            return new GeoPoint(latitude, longitude);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String nullIfBlank(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
