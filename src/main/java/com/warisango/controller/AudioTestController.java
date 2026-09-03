package com.warisango.controller;

import com.warisango.dto.AudioExtractionRequest;
import com.warisango.service.VideoAudioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.Map;

@RestController
@Profile("dev")
@RequestMapping("/api/audio")
public class AudioTestController {

    private final VideoAudioService videoAudioService;

    public AudioTestController(
            VideoAudioService videoAudioService) {

        this.videoAudioService = videoAudioService;
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> testAudioExtraction(
            @Valid @RequestBody AudioExtractionRequest request) {

        Path audioFile =
                videoAudioService.downloadAudio(request.videoUrl());

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Audio extracted successfully.",
                        "audioFile",
                        audioFile.toString()
                )
        );
    }
}
