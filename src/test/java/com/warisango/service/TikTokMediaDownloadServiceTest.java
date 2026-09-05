package com.warisango.service;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.warisango.exception.AIProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TikTokMediaDownloadServiceTest {
    private final Page page = mock(Page.class);
    private final Locator errorHeading = mock(Locator.class);
    private final Locator errorDetails = mock(Locator.class);
    private final Locator video = mock(Locator.class);
    private final TikTokMediaDownloadService service = new TikTokMediaDownloadService();

    @BeforeEach
    void setUp() {
        when(page.getByText("Oops! Something went wrong")).thenReturn(errorHeading);
        when(errorHeading.first()).thenReturn(errorHeading);
        when(page.getByText("Please contact your administrator with the error code:")).thenReturn(errorDetails);
        when(errorDetails.first()).thenReturn(errorDetails);
        when(page.locator("video")).thenReturn(video);
        when(video.first()).thenReturn(video);
    }

    @Test
    void refreshesTemporaryErrorAndDiscardsOldMediaResponse() {
        when(errorHeading.isVisible()).thenReturn(true, false);
        when(errorDetails.isVisible()).thenReturn(true);
        when(video.isVisible()).thenReturn(true);
        AtomicReference<Response> response = new AtomicReference<>(mock(Response.class));

        service.waitForVideoPage(page, response);

        verify(page).reload(any(Page.ReloadOptions.class));
        assertNull(response.get());
    }

    @Test
    void stopsAfterTwoRefreshesWhenErrorPersists() {
        when(errorHeading.isVisible()).thenReturn(true);
        when(errorDetails.isVisible()).thenReturn(true);

        assertThrows(AIProcessingException.class,
                () -> service.waitForVideoPage(page, new AtomicReference<>()));

        verify(page, times(2)).reload(any(Page.ReloadOptions.class));
    }

    @Test
    void doesNotRefreshHealthyVideoPage() {
        when(video.isVisible()).thenReturn(true);

        service.waitForVideoPage(page, new AtomicReference<>());

        verify(page, never()).reload(any(Page.ReloadOptions.class));
    }

    @Test
    void doesNotRefreshOtherPagesIncludingVerificationChallenges() {
        when(errorHeading.isVisible()).thenReturn(true);
        when(errorDetails.isVisible()).thenReturn(false);

        assertThrows(AIProcessingException.class,
                () -> service.waitForVideoPage(page, new AtomicReference<>()));

        verify(page, never()).reload(any(Page.ReloadOptions.class));
    }

    @Test
    void detectsErrorThatAppearsAfterInitialPageLoad() {
        when(errorHeading.isVisible()).thenReturn(false, true, false);
        when(errorDetails.isVisible()).thenReturn(true);
        when(video.isVisible()).thenReturn(false, true);

        service.waitForVideoPage(page, new AtomicReference<>());

        verify(page).reload(any(Page.ReloadOptions.class));
    }
}
