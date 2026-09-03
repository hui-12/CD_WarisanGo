package com.warisango.controller;

import com.google.cloud.firestore.ListenerRegistration;
import com.warisango.dto.HeritageBusinessDTO;
import com.warisango.service.BusinessService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Component
public class BusinessStreamManager {

    private final BusinessService businessService;

    public BusinessStreamManager(BusinessService businessService) {
        this.businessService = businessService;
    }

    public SseEmitter openStream() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        send(emitter, businessService.getApprovedBusinesses());
        ListenerRegistration registration = businessService.subscribeToApprovedBusinesses(
                businesses -> send(emitter, businesses));
        emitter.onCompletion(registration::remove);
        emitter.onTimeout(registration::remove);
        emitter.onError(exception -> registration.remove());
        return emitter;
    }

    private void send(SseEmitter emitter, List<HeritageBusinessDTO> businesses) {
        try {
            emitter.send(SseEmitter.event().name("business-update").data(businesses));
        } catch (IOException exception) {
            emitter.completeWithError(exception);
        }
    }
}
