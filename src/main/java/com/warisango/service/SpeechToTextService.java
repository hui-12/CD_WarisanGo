package com.warisango.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warisango.exception.AIProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Handles speech-to-text transcription using AssemblyAI.
 *
 * Flow:
 * 1. Download audio from the provided audio URL.
 * 2. Store the audio temporarily on the server.
 * 3. Upload the temporary audio file to AssemblyAI.
 * 4. Submit the AssemblyAI upload URL for transcription.
 * 5. Poll until transcription is completed.
 * 6. Delete the temporary audio file.
 */
@Service
public class SpeechToTextService {

    private static final Logger logger =
            LoggerFactory.getLogger(SpeechToTextService.class);

    private static final String ASSEMBLYAI_UPLOAD_URL =
            "https://api.assemblyai.com/v2/upload";

    private static final String ASSEMBLYAI_TRANSCRIPT_URL =
            "https://api.assemblyai.com/v2/transcript";

    private static final long POLLING_INTERVAL_MS = 3000;

    private static final int MAX_POLLING_ATTEMPTS = 100;

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    private final String apiKey;

    /**
     * Creates the speech-to-text service.
     *
     * @param apiKey AssemblyAI API key
     * @param objectMapper Jackson object mapper
     */
    public SpeechToTextService(
            @Value("${assemblyai.api.key}") String apiKey,
            ObjectMapper objectMapper) {

        this.apiKey = apiKey;
        this.objectMapper = objectMapper;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Downloads audio from the provided URL, uploads it to AssemblyAI,
     * and returns the completed transcript.
     *
     * @param audioUrl temporary/direct audio URL
     * @return completed transcript text
     */
    public String transcribe(String audioUrl) {

        validateAudioUrl(audioUrl);

        Path audioFile = downloadAudio(audioUrl);

        try {

            String uploadUrl =
                    uploadAudio(audioFile);

            String transcriptId =
                    submitTranscription(uploadUrl);

            return waitForTranscript(transcriptId);

        } finally {

            deleteTemporaryFile(audioFile);
        }
    }

    /**
     * Downloads the audio URL to a temporary file.
     *
     * @param audioUrl direct audio URL
     * @return path of the temporary audio file
     */
    private Path downloadAudio(String audioUrl) {

        try {

            Path tempFile =
                    Files.createTempFile(
                            "warisango-audio-",
                            ".webm"
                    );

            logger.info(
                    "Downloading audio to temporary file."
            );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(audioUrl))
                            .timeout(Duration.ofMinutes(2))
                            .GET()
                            .build();

            HttpResponse<Path> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofFile(tempFile)
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                Files.deleteIfExists(tempFile);

                logger.error(
                        "Audio download failed. HTTP status: {}",
                        response.statusCode()
                );

                throw new AIProcessingException(
                        "Failed to download audio. HTTP status: "
                                + response.statusCode()
                );
            }

            logger.info(
                    "Audio downloaded successfully."
            );

            return tempFile;

        } catch (IOException exception) {

            logger.error(
                    "Failed to download audio.",
                    exception
            );

            throw new AIProcessingException(
                    "Failed to download audio file.",
                    exception
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new AIProcessingException(
                    "Audio download was interrupted.",
                    exception
            );
        }
    }

    /**
     * Uploads the temporary audio file to AssemblyAI.
     *
     * @param audioFile temporary audio file
     * @return AssemblyAI upload URL
     */
    private String uploadAudio(Path audioFile) {

        try {

            logger.info(
                    "Uploading audio file to AssemblyAI."
            );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            ASSEMBLYAI_UPLOAD_URL
                                    )
                            )
                            .timeout(Duration.ofMinutes(2))
                            .header(
                                    "Authorization",
                                    apiKey
                            )
                            .header(
                                    "Content-Type",
                                    "application/octet-stream"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofFile(audioFile)
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                logger.error(
                        "AssemblyAI upload failed. HTTP status: {}",
                        response.statusCode()
                );

                throw new AIProcessingException(
                        "Failed to upload audio to AssemblyAI."
                );
            }

            JsonNode responseJson =
                    objectMapper.readTree(
                            response.body()
                    );

            String uploadUrl =
                    responseJson
                            .path("upload_url")
                            .asText();

            if (uploadUrl.isBlank()) {

                throw new AIProcessingException(
                        "AssemblyAI did not return an upload URL."
                );
            }

            logger.info(
                    "Audio uploaded to AssemblyAI successfully."
            );

            return uploadUrl;

        } catch (IOException exception) {

            logger.error(
                    "Failed to upload audio to AssemblyAI.",
                    exception
            );

            throw new AIProcessingException(
                    "Failed to upload audio to AssemblyAI.",
                    exception
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new AIProcessingException(
                    "AssemblyAI upload was interrupted.",
                    exception
            );
        }
    }

    /**
     * Submits an AssemblyAI upload URL for transcription.
     *
     * @param uploadUrl AssemblyAI uploaded audio URL
     * @return AssemblyAI transcript ID
     */
    private String submitTranscription(String uploadUrl) {

        try {

            String requestBody =
                    objectMapper.writeValueAsString(
                            new TranscriptRequest(uploadUrl)
                    );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            ASSEMBLYAI_TRANSCRIPT_URL
                                    )
                            )
                            .timeout(Duration.ofSeconds(30))
                            .header(
                                    "Authorization",
                                    apiKey
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(requestBody)
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                logger.error(
                        "AssemblyAI transcription submission failed. "
                                + "HTTP status: {}",
                        response.statusCode()
                );

                throw new AIProcessingException(
                        "AssemblyAI transcription request failed."
                );
            }

            JsonNode responseJson =
                    objectMapper.readTree(
                            response.body()
                    );

            JsonNode idNode =
                    responseJson.get("id");

            if (idNode == null
                    || idNode.asText().isBlank()) {

                throw new AIProcessingException(
                        "AssemblyAI did not return a transcript ID."
                );
            }

            String transcriptId =
                    idNode.asText();

            logger.info(
                    "AssemblyAI transcription submitted successfully."
            );

            return transcriptId;

        } catch (IOException exception) {

            logger.error(
                    "Failed to communicate with AssemblyAI.",
                    exception
            );

            throw new AIProcessingException(
                    "Failed to submit transcription request.",
                    exception
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new AIProcessingException(
                    "Transcription request was interrupted.",
                    exception
            );
        }
    }

    /**
     * Polls AssemblyAI until transcription is completed or fails.
     *
     * @param transcriptId AssemblyAI transcript ID
     * @return completed transcript text
     */
    private String waitForTranscript(String transcriptId) {

        for (int attempt = 0;
             attempt < MAX_POLLING_ATTEMPTS;
             attempt++) {

            JsonNode transcript =
                    getTranscript(transcriptId);

            String status =
                    transcript
                            .path("status")
                            .asText();

            logger.info(
                    "AssemblyAI transcription status: {}",
                    status
            );

            if ("completed".equalsIgnoreCase(status)) {

                return transcript
                        .path("text")
                        .asText("");
            }

            if ("error".equalsIgnoreCase(status)) {

                String error =
                        transcript
                                .path("error")
                                .asText(
                                        "Unknown transcription error."
                                );

                throw new AIProcessingException(
                        "AssemblyAI transcription failed: "
                                + error
                );
            }

            sleepBeforeNextPoll();
        }

        throw new AIProcessingException(
                "AssemblyAI transcription timed out."
        );
    }

    /**
     * Retrieves the current transcript status from AssemblyAI.
     *
     * @param transcriptId AssemblyAI transcript ID
     * @return transcript response
     */
    private JsonNode getTranscript(String transcriptId) {

        try {

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            ASSEMBLYAI_TRANSCRIPT_URL
                                                    + "/"
                                                    + transcriptId
                                    )
                            )
                            .timeout(Duration.ofSeconds(30))
                            .header(
                                    "Authorization",
                                    apiKey
                            )
                            .GET()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new AIProcessingException(
                        "Failed to retrieve transcription status."
                );
            }

            return objectMapper.readTree(
                    response.body()
            );

        } catch (IOException exception) {

            throw new AIProcessingException(
                    "Failed to retrieve AssemblyAI transcript.",
                    exception
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new AIProcessingException(
                    "Transcript polling was interrupted.",
                    exception
            );
        }
    }

    /**
     * Waits before polling AssemblyAI again.
     */
    private void sleepBeforeNextPoll() {

        try {

            Thread.sleep(POLLING_INTERVAL_MS);

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new AIProcessingException(
                    "Transcript polling was interrupted.",
                    exception
            );
        }
    }

    /**
     * Validates the audio URL.
     *
     * @param audioUrl direct audio URL
     */
    private void validateAudioUrl(String audioUrl) {

        if (audioUrl == null
                || audioUrl.isBlank()) {

            throw new AIProcessingException(
                    "Audio URL cannot be empty."
            );
        }
    }

    /**
     * Deletes the temporary audio file.
     *
     * @param audioFile temporary audio file
     */
    private void deleteTemporaryFile(Path audioFile) {

        try {

            Files.deleteIfExists(audioFile);

            logger.info(
                    "Temporary audio file deleted."
            );

        } catch (IOException exception) {

            logger.warn(
                    "Failed to delete temporary audio file: {}",
                    audioFile,
                    exception
            );
        }
    }

    /**
     * Request body sent to AssemblyAI.
     *
     * @param audio_url AssemblyAI uploaded audio URL
     */
    private record TranscriptRequest(String audio_url) {
    }
}