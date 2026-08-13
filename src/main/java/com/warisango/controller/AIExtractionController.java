package com.warisango.controller;

import com.warisango.dto.AIExtractionResponse;
import com.warisango.service.AIExtractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Provides endpoints for testing AI heritage information extraction.
 */
@RestController
@RequestMapping("/api/ai")
public class AIExtractionController {

    private final AIExtractionService aiExtractionService;

    /**
     * Creates the AI extraction test controller.
     *
     * @param aiExtractionService AI extraction service
     */
    public AIExtractionController(
            AIExtractionService aiExtractionService) {

        this.aiExtractionService = aiExtractionService;
    }

    /**
     * Extracts heritage information from a transcript.
     *
     * @param request request containing transcript text
     * @return extracted heritage information
     */
    @PostMapping("/extract")
    public ResponseEntity<AIExtractionResponse> extract(
            @RequestBody Map<String, String> request) {

        String transcript =
                request.get("transcript");

        AIExtractionResponse result =
                aiExtractionService.extract(transcript);

        return ResponseEntity.ok(result);
    }
}