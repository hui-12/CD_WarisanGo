package com.warisango.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VideoAudioServiceTest {

    @Test
    void delegatesTikTokDownloadToBrowserService() {
        String videoUrl = "https://www.tiktok.com/@warisango/video/123456";
        Path mediaFile = Path.of("video.mp4");
        TikTokMediaDownloadService tikTokService = mock(TikTokMediaDownloadService.class);
        when(tikTokService.download(videoUrl)).thenReturn(mediaFile);
        VideoAudioService service = new VideoAudioService(tikTokService);

        Path result = service.downloadAudio(videoUrl);

        assertEquals(mediaFile, result);
        verify(tikTokService).download(videoUrl);
    }

    @Test
    void usesCompatibleYouTubeDownloadArguments() {
        String videoUrl = "https://www.youtube.com/watch?v=video123";

        List<String> command = VideoAudioService.createDownloadCommand(videoUrl, "audio.%(ext)s");

        assertTrue(command.contains("bestaudio/best"));
        assertTrue(command.contains("--js-runtimes"));
        assertTrue(command.contains("node:/usr/bin/node"));
        assertEquals(videoUrl, command.getLast());
    }
}
