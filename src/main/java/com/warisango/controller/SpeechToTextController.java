package com.warisango.controller;

import com.warisango.service.SpeechToTextService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/transcription")
public class SpeechToTextController {

    private final SpeechToTextService speechToTextService;

    public SpeechToTextController(
            SpeechToTextService speechToTextService) {

        this.speechToTextService = speechToTextService;
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> testTranscription(
            @RequestBody Map<String, String> request) {

        String audioFile =
                request.get("audioFile");

        if (audioFile == null || audioFile.isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Audio file path cannot be empty."
                    )
            );
        }

        String transcript =
                speechToTextService.transcribe(
                        Path.of(audioFile)
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