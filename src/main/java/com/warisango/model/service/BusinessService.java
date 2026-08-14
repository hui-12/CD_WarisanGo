package com.warisango.model.service;

import com.google.cloud.firestore.ListenerRegistration;
import com.warisango.dto.HeritageBusinessDTO;
import com.warisango.exception.AIProcessingException;
import com.warisango.model.repository.BusinessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class BusinessService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessService.class);
    private final BusinessRepository businessRepository;

    public BusinessService(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    public List<HeritageBusinessDTO> getApprovedBusinesses() {
        try {
            return businessRepository.findApprovedBusinesses();
        } catch (Exception e) {
            logger.error("Error fetching approved heritage businesses", e);
            throw new AIProcessingException("Failed to load map data.");
        }
    }

    public Optional<HeritageBusinessDTO> getBusinessById(String id) {
        try {
            return businessRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error fetching business by id: {}", id, e);
            return Optional.empty();
        }
    }

    // Stream real-time updates via SSE
    public SseEmitter streamApprovedBusinesses() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // Fetch initial state immediately upon connection
        try {
            List<HeritageBusinessDTO> initialList = businessRepository.findApprovedBusinesses();
            emitter.send(SseEmitter.event().name("business-update").data(initialList));
        } catch (Exception e) {
            logger.error("Error sending initial batch for SSE stream", e);
        }

        // Register Firestore real-time listener
        ListenerRegistration registration = businessRepository.addApprovedBusinessesListener(businesses -> {
            try {
                emitter.send(SseEmitter.event().name("business-update").data(businesses));
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
}