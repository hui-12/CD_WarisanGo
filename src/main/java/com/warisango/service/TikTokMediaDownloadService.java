package com.warisango.service;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.WaitUntilState;
import com.microsoft.playwright.options.RequestOptions;
import com.warisango.exception.AIProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class TikTokMediaDownloadService {

    private static final Logger logger = LoggerFactory.getLogger(TikTokMediaDownloadService.class);
    private static final long MEDIA_TIMEOUT_MILLISECONDS = 60_000;

    /**
     * Downloads the media response loaded by TikTok's authenticated browser session.
     */
    public Path download(String videoUrl) {
        synchronized (TikTokScraperService.PROFILE_LOCK) {
            return downloadWithBrowser(videoUrl);
        }
    }

    private Path downloadWithBrowser(String videoUrl) {
        Path outputDirectory = null;

        try (Playwright playwright = Playwright.create();
             BrowserContext context = playwright.chromium().launchPersistentContext(
                     TikTokScraperService.PROFILE_PATH,
                     new BrowserType.LaunchPersistentContextOptions().setHeadless(false))) {
            outputDirectory = Files.createTempDirectory("warisango-audio-");
            Path mediaFile = outputDirectory.resolve("video-" + UUID.randomUUID() + ".mp4");
            Page page = context.pages().isEmpty() ? context.newPage() : context.pages().getFirst();
            AtomicReference<Response> mediaResponse = new AtomicReference<>();

            page.onResponse(response -> captureMediaResponse(response, mediaResponse));
            page.navigate(videoUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            page.locator("video").first().waitFor();
            page.locator("video").first().evaluate("video => video.play().catch(() => {})");

            Response response = waitForMediaResponse(page, mediaResponse);
            response.finished();
            APIResponse downloadResponse = context.request().get(
                    response.url(),
                    RequestOptions.create().setHeader("Referer", videoUrl));
            if (!downloadResponse.ok()) {
                throw new AIProcessingException(
                        "TikTok rejected the authenticated video download. HTTP status: "
                                + downloadResponse.status());
            }
            byte[] mediaBytes = downloadResponse.body();
            if (mediaBytes.length == 0) {
                throw new AIProcessingException("TikTok returned an empty video file.");
            }

            Files.write(mediaFile, mediaBytes);
            logger.info("TikTok media downloaded for transcription: {} bytes", mediaBytes.length);
            return mediaFile;
        } catch (AIProcessingException exception) {
            cleanup(outputDirectory);
            throw exception;
        } catch (IOException | PlaywrightException exception) {
            cleanup(outputDirectory);
            logger.warn("Unable to download TikTok media through Chromium.", exception);
            throw new AIProcessingException(
                    "Unable to download the TikTok video. Complete any verification in Chromium and retry.",
                    exception);
        }
    }

    private void captureMediaResponse(Response response, AtomicReference<Response> mediaResponse) {
        String contentType = response.headers().getOrDefault("content-type", "").toLowerCase(Locale.ROOT);
        if (mediaResponse.get() == null && contentType.startsWith("video/") && response.status() < 400) {
            mediaResponse.compareAndSet(null, response);
        }
    }

    private Response waitForMediaResponse(Page page, AtomicReference<Response> mediaResponse) {
        long deadline = System.currentTimeMillis() + MEDIA_TIMEOUT_MILLISECONDS;
        while (mediaResponse.get() == null && System.currentTimeMillis() < deadline) {
            page.waitForTimeout(500);
        }

        Response response = mediaResponse.get();
        if (response == null) {
            throw new AIProcessingException(
                    "TikTok did not load a downloadable video. Complete any verification in Chromium and retry.");
        }
        return response;
    }

    private void cleanup(Path directory) {
        if (directory == null) {
            return;
        }
        try (var paths = Files.walk(directory)) {
            paths.sorted((first, second) -> second.compareTo(first)).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    logger.warn("Unable to delete temporary TikTok media path: {}", path, exception);
                }
            });
        } catch (IOException exception) {
            logger.warn("Unable to clean the temporary TikTok media directory.", exception);
        }
    }
}
