package com.warisango.service;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitUntilState;
import com.warisango.dto.TikTokSearchRequest;
import com.warisango.dto.TikTokVideoDTO;
import com.warisango.exception.TikTokScrapingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class TikTokScraperService {

    private static final Logger logger = LoggerFactory.getLogger(TikTokScraperService.class);
    static final Path PROFILE_PATH = Path.of(".tiktok-profile").toAbsolutePath().normalize();
    static final Object PROFILE_LOCK = new Object();
    private static final long VERIFICATION_TIMEOUT_MILLISECONDS = 120_000;
    private static final String VIDEO_LINK_SELECTOR = "a[href*='/video/']";
    private static final String CHALLENGE_SELECTOR = String.join(", ",
            "iframe[src*='captcha']",
            "[class*='captcha']",
            "[id*='captcha']");
    private static final Pattern CHALLENGE_TEXT = Pattern.compile(
            "verify to continue|log in to continue",
            Pattern.CASE_INSENSITIVE);

    /**
     * Runs one visible, persistent TikTok search at a time because Chromium does not permit concurrent use of the
     * same user-data directory.
     */
    public List<TikTokVideoDTO> search(TikTokSearchRequest request) {
        synchronized (PROFILE_LOCK) {
            return searchWithBrowser(request);
        }
    }

    private List<TikTokVideoDTO> searchWithBrowser(TikTokSearchRequest request) {
        String encodedKeyword = URLEncoder.encode(request.keyword().trim(), StandardCharsets.UTF_8);
        String searchUrl = "https://www.tiktok.com/search/video?q=" + encodedKeyword;

        logger.info("Starting TikTok search with profile {}.", PROFILE_PATH);

        try (Playwright playwright = Playwright.create();
             BrowserContext context = playwright.chromium().launchPersistentContext(
                     PROFILE_PATH,
                     new BrowserType.LaunchPersistentContextOptions().setHeadless(isHeadless()))) {

            Page page = context.pages().isEmpty() ? context.newPage() : context.pages().getFirst();
            page.navigate(searchUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            page.waitForTimeout(request.waitTime());
            waitForManualVerification(page);

            Map<String, TikTokVideoDTO> videos = new LinkedHashMap<>();
            collectVideos(page, videos, request.maximumVideos());

            for (int scroll = 0;
                 scroll < request.scrollCount() && videos.size() < request.maximumVideos();
                 scroll++) {
                page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
                page.waitForTimeout(request.waitTime());
                waitForManualVerification(page);
                collectVideos(page, videos, request.maximumVideos());
            }

            return List.copyOf(videos.values());
        } catch (TikTokScrapingException exception) {
            throw exception;
        } catch (PlaywrightException exception) {
            logger.warn("TikTok browser search failed.", exception);
            throw new TikTokScrapingException(
                    "Unable to complete the TikTok browser search. Check the server log for details.", exception);
        }
    }

    private void waitForManualVerification(Page page) {
        if (!hasChallenge(page)) {
            return;
        }

        logger.info("TikTok verification detected. Waiting for manual completion in the visible browser.");
        long deadline = System.currentTimeMillis() + VERIFICATION_TIMEOUT_MILLISECONDS;

        while (hasChallenge(page) && System.currentTimeMillis() < deadline) {
            page.waitForTimeout(1_000);
        }

        if (hasChallenge(page)) {
            throw new TikTokScrapingException(
                    "TikTok verification was not completed within two minutes. Complete it manually and retry.");
        }
    }

    private boolean hasChallenge(Page page) {
        return page.locator(CHALLENGE_SELECTOR).count() > 0
                || page.getByText(CHALLENGE_TEXT).count() > 0;
    }

    private void collectVideos(Page page, Map<String, TikTokVideoDTO> videos, int maximumVideos) {
        Object evaluatedVideos = page.locator(VIDEO_LINK_SELECTOR).evaluateAll(
                "links => links.map(link => {"
                        + " const image = link.querySelector('img');"
                        + " return { videoUrl: link.href, title: image?.alt || link.ariaLabel || 'TikTok video',"
                        + " thumbnail: image?.currentSrc || image?.src || '' };"
                        + "})");

        if (!(evaluatedVideos instanceof Collection<?> videoData)) {
            return;
        }

        for (Object value : videoData) {
            if (value instanceof Map<?, ?> data) {
                addVideo(data, videos);
            }

            if (videos.size() >= maximumVideos) {
                return;
            }
        }
    }

    private void addVideo(Map<?, ?> data, Map<String, TikTokVideoDTO> videos) {
        Object urlValue = data.get("videoUrl");
        if (!(urlValue instanceof String url)
                || !url.startsWith("https://www.tiktok.com/")
                || !url.contains("/video/")) {
            return;
        }

        String normalizedUrl = normalizeVideoUrl(url);
        String title = stringValue(data.get("title"), "TikTok video");
        String thumbnail = stringValue(data.get("thumbnail"), "");
        videos.putIfAbsent(normalizedUrl, new TikTokVideoDTO(normalizedUrl, title, thumbnail));
    }

    private String stringValue(Object value, String fallback) {
        return value instanceof String text && !text.isBlank() ? text : fallback;
    }

    private boolean isHeadless() {
        return Boolean.parseBoolean(System.getenv().getOrDefault("TIKTOK_HEADLESS", "false"));
    }

    public static String normalizeVideoUrl(String url) {
        int queryIndex = url.indexOf('?');
        int fragmentIndex = url.indexOf('#');
        int endIndex = url.length();

        if (queryIndex >= 0) {
            endIndex = queryIndex;
        }
        if (fragmentIndex >= 0) {
            endIndex = Math.min(endIndex, fragmentIndex);
        }

        return url.substring(0, endIndex);
    }
}
