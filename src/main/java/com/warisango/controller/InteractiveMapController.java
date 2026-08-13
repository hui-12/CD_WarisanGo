package com.warisango.controller;

import com.warisango.dto.HeritageBusinessDTO;
import com.warisango.model.service.BusinessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/interactive-map")
public class InteractiveMapController {

    private final BusinessService heritageBusinessService;

    public InteractiveMapController(BusinessService heritageBusinessService) {
        this.heritageBusinessService = heritageBusinessService;
    }

    @GetMapping("/api/approved")
    public ResponseEntity<List<HeritageBusinessDTO>> getApprovedBusinesses() {
        List<HeritageBusinessDTO> businesses = heritageBusinessService.getApprovedBusinesses();
        return ResponseEntity.ok(businesses);
    }

    // SSE Endpoint for real-time updates
    @GetMapping("/api/stream")
    public SseEmitter streamApprovedBusinesses() {
        return heritageBusinessService.streamApprovedBusinesses();
    }
}