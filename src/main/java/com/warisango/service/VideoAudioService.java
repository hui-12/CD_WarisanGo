package com.warisango.service;

import com.warisango.exception.AIProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Handles audio extraction from YouTube videos.
 */
@Service
public class VideoAudioService {

    private static final Logger logger =
            LoggerFactory.getLogger(VideoAudioService.class);

    /**
     * Downloads the audio track from a YouTube video into a temporary file.
     *
     * @param videoUrl YouTube video URL
     * @return path of the temporary audio file
     */
    public Path downloadAudio(String videoUrl) {

        validateVideoUrl(videoUrl);

        Path outputDirectory = null;

        try {

            logger.info("Starting audio extraction from YouTube.");

            outputDirectory =
                    Files.createTempDirectory("warisango-audio-");

            String outputTemplate =
                    outputDirectory.resolve(
                            "audio-" + UUID.randomUUID() + ".%(ext)s"
                    ).toString();

            ProcessBuilder processBuilder =
                new ProcessBuilder(
                        "yt-dlp",
                        "--no-playlist",
                        "--extractor-args",
                        "youtube:player_client=android",
                        "-f",
                        "18",
                        "--extract-audio",
                        "--audio-format",
                        "m4a",
                        "--audio-quality",
                        "0",
                        "-o",
                        outputTemplate,
                        videoUrl
                );

            processBuilder.redirectErrorStream(true);

            Process process =
                    processBuilder.start();

            String processOutput =
                    new String(
                            process.getInputStream().readAllBytes()
                    );

            int exitCode =
                    process.waitFor();

            if (exitCode != 0) {

                logger.error(
                        "yt-dlp failed. Exit code: {}. Output: {}",
                        exitCode,
                        processOutput
                );

                cleanupDirectory(outputDirectory);

                throw new AIProcessingException(
                        "Unable to extract audio from YouTube."
                );
            }

            Path audioFile =
                    findAudioFile(outputDirectory);

            if (audioFile == null || !Files.exists(audioFile)) {

                logger.error(
                        "yt-dlp completed but no audio file was found."
                );

                cleanupDirectory(outputDirectory);

                throw new AIProcessingException(
                        "Audio file was not generated."
                );
            }

            logger.info(
                    "Audio extracted successfully: {}",
                    audioFile
            );

            return audioFile;

        } catch (IOException exception) {

            if (outputDirectory != null) {
                cleanupDirectory(outputDirectory);
            }

            logger.error(
                    "Unable to execute yt-dlp.",
                    exception
            );

            throw new AIProcessingException(
                    "Unable to extract audio from YouTube.",
                    exception
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            if (outputDirectory != null) {
                cleanupDirectory(outputDirectory);
            }

            logger.error(
                    "yt-dlp process was interrupted.",
                    exception
            );

            throw new AIProcessingException(
                    "Audio extraction was interrupted.",
                    exception
            );
        }
    }

    /**
     * Finds the generated audio file inside the temporary directory.
     *
     * @param directory temporary audio directory
     * @return generated audio file
     * @throws IOException if the directory cannot be read
     */
    private Path findAudioFile(Path directory)
            throws IOException {

        try (var files = Files.list(directory)) {

            return files
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.toString()
                                    .toLowerCase()
                                    .endsWith(".m4a"))
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * Deletes temporary audio files.
     *
     * @param directory temporary directory
     */
    private void cleanupDirectory(Path directory) {

        if (directory == null) {
            return;
        }

        try {

            if (Files.exists(directory)) {

                try (var files = Files.walk(directory)) {

                    files.sorted(
                            (first, second) ->
                                    second.compareTo(first)
                    ).forEach(path -> {

                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException exception) {

                            logger.warn(
                                    "Unable to delete temporary file: {}",
                                    path,
                                    exception
                            );
                        }
                    });
                }
            }

        } catch (IOException exception) {

            logger.warn(
                    "Unable to clean temporary audio directory.",
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

    public void deleteAudioFile(Path audioFile) {

        if (audioFile == null) {
                return;
        }

        Path directory = audioFile.getParent();

        cleanupDirectory(directory);
        }
}