package com.warisango.controller;

import com.warisango.dto.TranscriptionRequest;
import com.warisango.service.SpeechToTextService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.Map;

@RestController
@Profile("dev")
@RequestMapping("/api/transcription")
public class SpeechToTextController {

    private final SpeechToTextService speechToTextService;

    public SpeechToTextController(
            SpeechToTextService speechToTextService) {

        this.speechToTextService = speechToTextService;
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> testTranscription(
            @Valid @RequestBody TranscriptionRequest request) {

        String transcript =
                speechToTextService.transcribe(
                        Path.of(request.audioFile())
                );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Transcription completed successfully.",
                        "transcript",
                        transcript
                )
        );
    }
}
