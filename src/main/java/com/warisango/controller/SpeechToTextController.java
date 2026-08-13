package com.warisango.controller;

import com.warisango.service.SpeechToTextService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Provides endpoints for testing AssemblyAI speech-to-text processing.
 */
@RestController
@RequestMapping("/api/transcription")
public class SpeechToTextController {

    private final SpeechToTextService speechToTextService;

    public SpeechToTextController(
            SpeechToTextService speechToTextService) {

        this.speechToTextService = speechToTextService;
    }

    /**
     * Tests transcription using a direct audio URL.
     *
     * @param request request containing the audio URL
     * @return transcription result
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> testTranscription(
            @RequestBody Map<String, String> request) {

        String audioUrl =
                request.get("audioUrl");

        String transcript =
                speechToTextService.transcribe(audioUrl);

        return ResponseEntity.ok(
                Map.of("transcript", transcript)
        );
    }

    @GetMapping("/test")
    public String testPage() {
        return "Transcription API is working";
    }
}