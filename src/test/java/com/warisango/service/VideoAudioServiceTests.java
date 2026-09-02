package com.warisango.service;

import com.warisango.model.service.TikTokMediaDownloadService;
import com.warisango.model.service.VideoAudioService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VideoAudioServiceTests {

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
    void preservesYouTubeDownloadArguments() {
        String videoUrl = "https://www.youtube.com/watch?v=video123";

        List<String> command = VideoAudioService.createDownloadCommand(videoUrl, "audio.%(ext)s");

        assertTrue(command.contains("youtube:player_client=android"));
        assertTrue(command.contains("18"));
        assertEquals(videoUrl, command.getLast());
    }
}
