package com.warisango.controller;

import com.warisango.dto.AIExtractionResult;
import com.warisango.exception.AIProcessingException;
import com.warisango.service.AIExtractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public ResponseEntity<?> extract(
            @RequestBody Map<String, String> request) {

        String transcript =
                request.get("transcript");

        if (transcript == null || transcript.isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Transcript cannot be empty."
                    )
            );
        }

        try {

            AIExtractionResult result =
                    aiExtractionService.extract(transcript);

            return ResponseEntity.ok(result);

        } catch (AIProcessingException exception) {

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "message",
                            exception.getMessage()
                    )
            );
        }
    }
}