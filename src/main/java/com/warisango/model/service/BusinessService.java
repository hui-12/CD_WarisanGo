package com.warisango.model.service;

import com.google.cloud.firestore.ListenerRegistration;
import com.warisango.dto.HeritageBusinessDTO;
import com.warisango.exception.AIProcessingException;
import com.warisango.model.repository.BusinessRepository;
import com.warisango.model.repository.HeritageBusinessImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@Service
public class BusinessService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessService.class);
    private final BusinessRepository businessRepository;
    private final HeritageBusinessImageRepository heritageBusinessImageRepository;

    public BusinessService(BusinessRepository businessRepository,
                           HeritageBusinessImageRepository heritageBusinessImageRepository) {
        this.businessRepository = businessRepository;
        this.heritageBusinessImageRepository = heritageBusinessImageRepository;
    }

    public List<HeritageBusinessDTO> getApprovedBusinesses() {
        try {
            return attachImages(businessRepository.findApprovedBusinesses());
        } catch (Exception e) {
            logger.error("Error fetching approved heritage businesses", e);
            throw new AIProcessingException("Failed to load map data.");
        }
    }

    public Optional<HeritageBusinessDTO> getBusinessById(String id) {
        try {
            Optional<HeritageBusinessDTO> business = businessRepository.findById(id);
            business.ifPresent(this::attachImages);
            return business;
        } catch (Exception e) {
            logger.error("Error fetching business by id: {}", id, e);
            return Optional.empty();
        }
    }

    public void updateAverageRating(String businessId, Double averageRating) {
        try {
            businessRepository.updateAverageRating(businessId, averageRating);
        } catch (Exception exception) {
            logger.error("Failed to update average rating for business {}.", businessId, exception);
            throw new IllegalStateException("Failed to update the business rating.", exception);
        }
    }

    public void updateReportedDetails(String businessId, Map<String, Object> corrections) {
        try {
            businessRepository.updateReportedDetails(businessId, corrections);
        } catch (Exception exception) {
            logger.error("Failed to apply reported corrections for business {}.", businessId, exception);
            throw new IllegalStateException("Failed to update the reported business.", exception);
        }
    }

    /**
     * Loads an approved business for the review pages, accepting either a Firestore document ID
     * or the businessId stored in the document.
     */
    public HeritageBusinessDTO getApprovedBusinessForReview(String businessId) {
        try {
            HeritageBusinessDTO business = businessRepository.findByBusinessId(businessId);
            if (business != null) {
                attachImages(business);
            }
            return business;
        } catch (Exception e) {
            logger.warn("Could not load heritage business {} for Review page.", businessId, e);
            return null;
        }
    }

    // Stream real-time updates via SSE
    public SseEmitter streamApprovedBusinesses() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // Fetch initial state immediately upon connection
        try {
            List<HeritageBusinessDTO> initialList = attachImages(businessRepository.findApprovedBusinesses());
            emitter.send(SseEmitter.event().name("business-update").data(initialList));
        } catch (Exception e) {
            logger.error("Error sending initial batch for SSE stream", e);
        }

        // Register Firestore real-time listener
        ListenerRegistration registration = businessRepository.addApprovedBusinessesListener(businesses -> {
            try {
                emitter.send(SseEmitter.event().name("business-update").data(attachImages(businesses)));
            } catch (IOException e) {
                logger.error("Error pushing SSE update to client", e);
                emitter.completeWithError(e);
            }
        });

        // Clean up listener when client disconnects
        emitter.onCompletion(registration::remove);
        emitter.onTimeout(registration::remove);
        emitter.onError((ex) -> registration.remove());

        return emitter;
    }

    /**
     * Image records are stored separately in Firestore so business data remains lightweight.
     * This method joins them with a directory or detail result before it reaches the controller.
     */
    private List<HeritageBusinessDTO> attachImages(List<HeritageBusinessDTO> businesses) {
        for (HeritageBusinessDTO business : businesses) {
            attachImages(business);
        }
        return businesses;
    }

    private void attachImages(HeritageBusinessDTO business) {
        List<String> imageUrls = heritageBusinessImageRepository.findImageUrlsByBusinessId(business.getBusinessId());
        business.setImageUrls(imageUrls);
        business.setPhotos(imageUrls);
    }
}
