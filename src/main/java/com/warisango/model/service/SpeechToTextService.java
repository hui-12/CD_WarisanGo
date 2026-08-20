package com.warisango.model.service;

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
 *
 * Local .m4a file
 *      ↓
 * AssemblyAI Upload API
 *      ↓
 * AssemblyAI audio URL
 *      ↓
 * AssemblyAI Transcript API
 *      ↓
 * Poll transcript status
 *      ↓
 * Transcript text
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

        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(30))
                        .build();
    }

    /**
     * Transcribes a local audio file.
     *
     * @param audioFile path to the temporary .m4a file
     * @return completed transcript
     */
    public String transcribe(Path audioFile) {

        validateAudioFile(audioFile);

        try {

            logger.info(
                    "Starting AssemblyAI transcription for: {}",
                    audioFile
            );

            /*
             * Step 1:
             * Upload the local .m4a file to AssemblyAI.
             */
            String assemblyAudioUrl =
                    uploadAudio(audioFile);

            logger.info(
                    "Audio uploaded successfully to AssemblyAI."
            );

            /*
             * Step 2:
             * Submit the AssemblyAI audio URL
             * for transcription.
             */
            String transcriptId =
                    submitTranscription(assemblyAudioUrl);

            logger.info(
                    "Transcription submitted. ID: {}",
                    transcriptId
            );

            /*
             * Step 3:
             * Wait until AssemblyAI finishes.
             */
            String transcript =
                    waitForTranscript(transcriptId);

            logger.info(
                    "Transcription completed successfully."
            );

            return transcript;

        } finally {

            /*
             * Step 4:
             * Delete the temporary audio file after
             * transcription is finished.
             */
            deleteTemporaryAudio(audioFile);
        }
    }

    /**
     * Uploads the local audio file to AssemblyAI.
     *
     * AssemblyAI returns a URL such as:
     *
     * https://cdn.assemblyai.com/upload/xxxxx
     *
     * That URL is then used when creating
     * the transcription request.
     *
     * @param audioFile local .m4a file
     * @return AssemblyAI audio URL
     */
    private String uploadAudio(Path audioFile) {

        try {

            byte[] audioBytes =
                    Files.readAllBytes(audioFile);

            logger.info(
                    "Uploading audio file to AssemblyAI: {} bytes",
                    audioBytes.length
            );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            ASSEMBLYAI_UPLOAD_URL
                                    )
                            )
                            .timeout(
                                    Duration.ofMinutes(5)
                            )
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
                                            .ofByteArray(audioBytes)
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

                logger.error(
                        "AssemblyAI response: {}",
                        response.body()
                );

                throw new AIProcessingException(
                        "AssemblyAI audio upload failed. HTTP status: "
                                + response.statusCode()
                );
            }

            JsonNode responseJson =
                    objectMapper.readTree(
                            response.body()
                    );

            JsonNode uploadUrlNode =
                    responseJson.get("upload_url");

            if (uploadUrlNode == null
                    || uploadUrlNode.asText().isBlank()) {

                throw new AIProcessingException(
                        "AssemblyAI did not return an upload URL."
                );
            }

            return uploadUrlNode.asText();

        } catch (IOException exception) {

            logger.error(
                    "Failed to read or upload audio file.",
                    exception
            );

            throw new AIProcessingException(
                    "Failed to upload audio file to AssemblyAI.",
                    exception
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new AIProcessingException(
                    "AssemblyAI audio upload was interrupted.",
                    exception
            );
        }
    }

    /**
     * Submits the AssemblyAI audio URL for transcription.
     *
     * @param audioUrl AssemblyAI-hosted audio URL
     * @return transcript ID
     */
    private String submitTranscription(String audioUrl) {

        try {

            String requestBody =
                    objectMapper.writeValueAsString(
                            new TranscriptRequest(audioUrl)
                    );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            ASSEMBLYAI_TRANSCRIPT_URL
                                    )
                            )
                            .timeout(
                                    Duration.ofSeconds(30)
                            )
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
                        "AssemblyAI transcription submission failed. HTTP status: {}",
                        response.statusCode()
                );

                logger.error(
                        "AssemblyAI response: {}",
                        response.body()
                );

                throw new AIProcessingException(
                        "AssemblyAI transcription request failed. HTTP status: "
                                + response.statusCode()
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

            return idNode.asText();

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
     * Polls AssemblyAI until transcription is completed.
     *
     * @param transcriptId AssemblyAI transcript ID
     * @return transcript text
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
     * Retrieves the current transcription status.
     *
     * @param transcriptId AssemblyAI transcript ID
     * @return AssemblyAI transcript response
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
                            .timeout(
                                    Duration.ofSeconds(30)
                            )
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

                logger.error(
                        "Failed to retrieve transcript. HTTP status: {}",
                        response.statusCode()
                );

                logger.error(
                        "AssemblyAI response: {}",
                        response.body()
                );

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

            Thread.sleep(
                    POLLING_INTERVAL_MS
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
     * Validates the local audio file.
     *
     * @param audioFile temporary .m4a file
     */
    private void validateAudioFile(Path audioFile) {

        if (audioFile == null) {

            throw new AIProcessingException(
                    "Audio file path cannot be null."
            );
        }

        if (!Files.exists(audioFile)) {

            throw new AIProcessingException(
                    "Audio file does not exist: "
                            + audioFile
            );
        }

        if (!Files.isRegularFile(audioFile)) {

            throw new AIProcessingException(
                    "Audio path is not a file: "
                            + audioFile
            );
        }

        if (!Files.isReadable(audioFile)) {

            throw new AIProcessingException(
                    "Audio file cannot be read: "
                            + audioFile
            );
        }

        String fileName =
                audioFile
                        .getFileName()
                        .toString()
                        .toLowerCase();

        if (!fileName.endsWith(".m4a")) {

            throw new AIProcessingException(
                    "Unsupported audio format. "
                            + "Expected .m4a file."
            );
        }

        try {
            Path realAudioFile = audioFile.toRealPath();
            Path temporaryRoot = Path.of(
                    System.getProperty("java.io.tmpdir")
            ).toRealPath();
            Path parentDirectory = realAudioFile.getParent();

            boolean isManagedTemporaryFile = parentDirectory != null
                    && temporaryRoot.equals(parentDirectory.getParent())
                    && parentDirectory.getFileName()
                            .toString()
                            .startsWith("warisango-audio-");

            if (!isManagedTemporaryFile) {
                throw new AIProcessingException(
                        "Audio file is outside the managed temporary directory."
                );
            }
        } catch (IOException exception) {
            throw new AIProcessingException(
                    "Unable to validate the audio file path.",
                    exception
            );
        }
    }

    /**
     * Deletes the temporary audio file.
     *
     * The parent directory created by VideoAudioService
     * is also deleted when it becomes empty.
     *
     * @param audioFile temporary audio file
     */
    private void deleteTemporaryAudio(Path audioFile) {

        try {

            if (audioFile != null
                    && Files.exists(audioFile)) {

                Files.deleteIfExists(audioFile);

                logger.info(
                        "Temporary audio file deleted: {}",
                        audioFile
                );
            }

            if (audioFile != null) {

                Path parentDirectory =
                        audioFile.getParent();

                if (parentDirectory != null
                        && Files.exists(parentDirectory)) {

                    try (var files =
                                 Files.list(parentDirectory)) {

                        if (files.findAny().isEmpty()) {

                            Files.deleteIfExists(
                                    parentDirectory
                            );

                            logger.info(
                                    "Temporary audio directory deleted: {}",
                                    parentDirectory
                            );
                        }
                    }
                }

            }

        } catch (IOException exception) {

            /*
             * Failure to delete a temporary file should
             * not cause an otherwise successful transcription
             * to fail.
             */
            logger.warn(
                    "Unable to delete temporary audio file: {}",
                    audioFile,
                    exception
            );
        }
    }

    /**
     * Request body sent to AssemblyAI.
     *
     * @param audio_url AssemblyAI-hosted audio URL
     */
    private record TranscriptRequest(
            String audio_url
    ) {
    }
}
