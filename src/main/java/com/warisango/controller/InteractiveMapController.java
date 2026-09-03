package com.warisango.controller;

import com.warisango.dto.HeritageBusinessDTO;
import com.warisango.service.BusinessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
public class InteractiveMapController {

    private final BusinessService heritageBusinessService;
    private final BusinessStreamManager businessStreamManager;

    public InteractiveMapController(
            BusinessService heritageBusinessService,
            BusinessStreamManager businessStreamManager) {
        this.heritageBusinessService = heritageBusinessService;
        this.businessStreamManager = businessStreamManager;
    }

    @GetMapping({"/api/map/businesses", "/interactive-map/api/approved"})
    public ResponseEntity<List<HeritageBusinessDTO>> getApprovedBusinesses() {
        List<HeritageBusinessDTO> businesses = heritageBusinessService.getApprovedBusinesses();
        return ResponseEntity.ok(businesses);
    }

    // SSE Endpoint for real-time updates
    @GetMapping({"/api/map/stream", "/interactive-map/api/stream"})
    public SseEmitter streamApprovedBusinesses() {
        return businessStreamManager.openStream();
    }
}
