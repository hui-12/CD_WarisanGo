package com.warisango.controller;

import com.warisango.dto.HeritageBusinessDTO;
import com.warisango.model.service.HeritageBusinessService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Controller
@RequestMapping("/interactive-map")
public class HeritageBusinessController {

    private final HeritageBusinessService heritageBusinessService;

    public HeritageBusinessController(HeritageBusinessService heritageBusinessService) {
        this.heritageBusinessService = heritageBusinessService;
    }

    @GetMapping
    public String showMapPage() {
        return "InteractiveMapPage";
    }

    @GetMapping("/api/approved")
    @ResponseBody
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