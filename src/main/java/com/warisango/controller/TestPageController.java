package com.warisango.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Profile("dev")
public class TestPageController {

    @GetMapping("/test/audio-test")
    public String audioText() {
        return "test/audio-test";
    }

    @GetMapping("/test/transcription-test")
    public String transcriptionText() {
        return "test/transcription-test";
    }

    @GetMapping("/test/ai-extraction-test")
    public String aiExtractionText() {
        return "test/ai-extraction-test";
    }

}
