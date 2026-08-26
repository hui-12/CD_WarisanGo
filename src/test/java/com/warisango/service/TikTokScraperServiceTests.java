package com.warisango.model.service;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TikTokScraperServiceTests {

    @Test
    void normalizeVideoUrlRemovesTrackingQuery() {
        String result = TikTokScraperService.normalizeVideoUrl(
                "https://www.tiktok.com/@warisango/video/123456?is_from_webapp=1");

        assertEquals("https://www.tiktok.com/@warisango/video/123456", result);
    }

    @Test
    void normalizeVideoUrlRemovesFragment() {
        String result = TikTokScraperService.normalizeVideoUrl(
                "https://www.tiktok.com/@warisango/video/123456#comments");

        assertEquals("https://www.tiktok.com/@warisango/video/123456", result);
    }
}
