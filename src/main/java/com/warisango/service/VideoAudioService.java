package com.warisango.service;

import com.warisango.exception.AIProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Handles audio stream URL extraction from YouTube videos.
 */
@Service
public class VideoAudioService {

    private static final Logger logger =
            LoggerFactory.getLogger(VideoAudioService.class);

    /**
     * Retrieves the direct audio stream URL from a YouTube video.
     *
     * @param videoUrl YouTube video URL
     * @return direct audio stream URL
     */
    public String getAudioUrl(String videoUrl) {

        validateVideoUrl(videoUrl);

        try {

            logger.info("Starting audio URL extraction.");

            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            "yt-dlp",
                            "-f",
                            "bestaudio",
                            "-g",
                            videoUrl
                    );

            processBuilder.redirectErrorStream(true);

            Process process =
                    processBuilder.start();

            String audioUrl =
                    new String(
                            process.getInputStream().readAllBytes()
                    ).trim();

            int exitCode =
                    process.waitFor();

            if (exitCode != 0 || audioUrl.isBlank()) {

                logger.error(
                        "yt-dlp failed to retrieve audio URL. Exit code: {}",
                        exitCode
                );

                throw new AIProcessingException(
                        "Unable to retrieve audio URL from YouTube."
                );
            }

            logger.info(
                    "Audio URL retrieved successfully."
            );

            return audioUrl;

        } catch (IOException exception) {

            logger.error(
                    "Unable to execute yt-dlp.",
                    exception
            );

            throw new AIProcessingException(
                    "Unable to execute yt-dlp.",
                    exception
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            logger.error(
                    "yt-dlp process was interrupted.",
                    exception
            );

            throw new AIProcessingException(
                    "Audio URL extraction was interrupted.",
                    exception
            );
        }
    }

    /**
     * Validates the supplied YouTube URL.
     *
     * @param videoUrl YouTube video URL
     */
    private void validateVideoUrl(String videoUrl) {

        if (videoUrl == null || videoUrl.isBlank()) {

            throw new AIProcessingException(
                    "YouTube video URL cannot be empty."
            );
        }

        if (!videoUrl.startsWith("https://www.youtube.com/")
                && !videoUrl.startsWith("https://youtube.com/")
                && !videoUrl.startsWith("https://youtu.be/")) {

            throw new AIProcessingException(
                    "Invalid YouTube URL."
            );
        }
    }
}