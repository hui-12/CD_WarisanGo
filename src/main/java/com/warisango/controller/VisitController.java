package com.warisango.controller;

import com.warisango.dto.VisitDTO;
import com.warisango.model.service.VisitService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/visits")
public class VisitController {
    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @GetMapping
    public ResponseEntity<List<VisitDTO>> getVisits(Authentication authentication) {
        return ResponseEntity.ok(visitService.getVisits(authentication.getName()));
    }

    @GetMapping("/this-week")
    public ResponseEntity<List<VisitDTO>> getThisWeeksVisits(Authentication authentication) {
        return ResponseEntity.ok(visitService.getThisWeeksVisits(authentication.getName()));
    }

    @GetMapping("/business-ids")
    public ResponseEntity<List<String>> getVisitedBusinessIds(Authentication authentication) {
        return ResponseEntity.ok(visitService.getVisitedBusinessIds(authentication.getName()));
    }
}
