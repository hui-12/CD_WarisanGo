package com.warisango.controller;

import com.warisango.dto.AIExtractionRequest;
import com.warisango.dto.AIExtractionResult;
import com.warisango.service.AIExtractionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-extraction")
public class AIExtractionController {

    private final AIExtractionService aiExtractionService;

    public AIExtractionController(
            AIExtractionService aiExtractionService) {

        this.aiExtractionService =
                aiExtractionService;
    }

    /**
     * Receives a transcript from SpeechToTextService
     * and sends it to Gemini for heritage information extraction.
     *
     * @param request request containing transcript
     * @return structured AI extraction result
     */
    @PostMapping("/extract")
    public ResponseEntity<AIExtractionResult> extract(
            @Valid @RequestBody AIExtractionRequest request) {

        return ResponseEntity.ok(
                aiExtractionService.extract(request.transcript())
        );
    }
}
