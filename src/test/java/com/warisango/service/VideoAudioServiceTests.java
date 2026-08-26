package com.warisango.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VideoAudioServiceTests {

    @Test
    void createsTikTokDownloadCommandWithoutYouTubeArguments() {
        String videoUrl = "https://www.tiktok.com/@warisango/video/123456";

        List<String> command = VideoAudioService.createDownloadCommand(videoUrl, "audio.%(ext)s");

        assertTrue(command.contains("bestaudio/best"));
        assertTrue(command.contains("--cookies-from-browser"));
        assertTrue(command.stream().anyMatch(argument -> argument.startsWith("chromium:")));
        assertFalse(command.contains("youtube:player_client=android"));
        assertEquals(videoUrl, command.getLast());
    }

    @Test
    void preservesYouTubeDownloadArguments() {
        String videoUrl = "https://www.youtube.com/watch?v=video123";

        List<String> command = VideoAudioService.createDownloadCommand(videoUrl, "audio.%(ext)s");

        assertTrue(command.contains("youtube:player_client=android"));
        assertTrue(command.contains("18"));
        assertEquals(videoUrl, command.getLast());
    }
}
