package com.warisango.controller;

import org.springframework.web.bind.annotation.*;
import com.warisango.service.VideoAudioService;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@RestController
@RequestMapping("/api/audio")
public class AudioTestController {

    private final VideoAudioService videoAudioService;

    public AudioTestController(VideoAudioService videoAudioService) {
        this.videoAudioService = videoAudioService;
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> testAudioExtraction(
            @RequestBody Map<String, String> request) {

        String videoUrl = request.get("videoUrl");

        String audioUrl =
                videoAudioService.getAudioUrl(videoUrl);

        return ResponseEntity.ok(
                Map.of("audioUrl", audioUrl)
        );
    }
}