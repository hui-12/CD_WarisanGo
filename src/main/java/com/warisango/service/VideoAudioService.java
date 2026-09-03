package com.warisango.service;

import com.warisango.exception.AIProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Handles audio extraction from supported video platforms.
 */
@Service
public class VideoAudioService {

    private static final Logger logger =
            LoggerFactory.getLogger(VideoAudioService.class);
    private final TikTokMediaDownloadService tikTokMediaDownloadService;

    public VideoAudioService(TikTokMediaDownloadService tikTokMediaDownloadService) {
        this.tikTokMediaDownloadService = tikTokMediaDownloadService;
    }

    /**
     * Downloads the audio track from a supported video URL into a temporary file.
     *
     * @param videoUrl supported video URL
     * @return path of the temporary audio file
     */
    public Path downloadAudio(String videoUrl) {

        validateVideoUrl(videoUrl);

        if (isTikTokUrl(videoUrl)) {
            return tikTokMediaDownloadService.download(videoUrl);
        }

        Path outputDirectory = null;

        try {

            logger.info("Starting audio extraction from supported video URL.");

            outputDirectory =
                    Files.createTempDirectory("warisango-audio-");

            String outputTemplate =
                    outputDirectory.resolve(
                            "audio-" + UUID.randomUUID() + ".%(ext)s"
                    ).toString();

            ProcessBuilder processBuilder = new ProcessBuilder(
                    createDownloadCommand(videoUrl, outputTemplate));

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
                        extractionFailureMessage(processOutput)
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
                    "Unable to start yt-dlp. Install yt-dlp and ensure it is available on PATH.",
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
     * Validates the supplied supported video URL.
     *
     * @param videoUrl supported video URL
     */
    private void validateVideoUrl(String videoUrl) {

        if (videoUrl == null || videoUrl.isBlank()) {

            throw new AIProcessingException(
                    "Video URL cannot be empty."
            );
        }

        try {
            URI uri = new URI(videoUrl);
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            Set<String> supportedHosts = Set.of(
                    "www.youtube.com", "youtube.com", "youtu.be",
                    "www.tiktok.com", "tiktok.com", "m.tiktok.com", "vm.tiktok.com");

            if (!"https".equalsIgnoreCase(uri.getScheme()) || !supportedHosts.contains(host)) {
                throw new AIProcessingException("Only YouTube and TikTok video URLs are supported.");
            }
        } catch (URISyntaxException exception) {
            throw new AIProcessingException("Invalid video URL.", exception);
        }
    }

    public static List<String> createDownloadCommand(String videoUrl, String outputTemplate) {
        List<String> command = new ArrayList<>(List.of("yt-dlp", "--no-playlist"));
        command.addAll(List.of(
                "--js-runtimes", "node",
                "--remote-components", "ejs:github",
                "-f", "bestaudio/best"));

        String cookiesFile = System.getenv("YOUTUBE_COOKIES_FILE");
        if (cookiesFile != null && !cookiesFile.isBlank()) {
            command.addAll(List.of("--cookies", cookiesFile));
        }

        command.addAll(List.of(
                "--extract-audio", "--audio-format", "m4a", "--audio-quality", "0",
                "-o", outputTemplate, videoUrl));
        return command;
    }

    private boolean isTikTokUrl(String videoUrl) {
        try {
            String host = new URI(videoUrl).getHost();
            return host != null && host.toLowerCase(Locale.ROOT).contains("tiktok.com");
        } catch (URISyntaxException exception) {
            return false;
        }
    }

    private String extractionFailureMessage(String processOutput) {
        String lowercaseOutput = processOutput.toLowerCase(Locale.ROOT);
        if (lowercaseOutput.contains("ffmpeg")
                && (lowercaseOutput.contains("not found") || lowercaseOutput.contains("not installed"))) {
            return "FFmpeg is required to convert the downloaded video audio.";
        }
        if (lowercaseOutput.contains("unexpected response from webpage request")) {
            return "TikTok rejected the yt-dlp webpage request. Refresh the TikTok browser session and retry.";
        }
        return "Unable to extract audio from the video.";
    }

    public void deleteAudioFile(Path audioFile) {

        if (audioFile == null) {
                return;
        }

        Path directory = audioFile.getParent();

        cleanupDirectory(directory);
        }
}
