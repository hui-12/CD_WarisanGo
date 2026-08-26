package com.warisango.service;

import com.warisango.dto.AIExtractionResult;
import com.warisango.dto.DiscoveryProcessResponse;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.function.BiConsumer;

/**
 * Coordinates the complete server-side heritage discovery workflow.
 */
@Service
public class AIWorkflowService {

    private final VideoAudioService videoAudioService;
    private final SpeechToTextService speechToTextService;
    private final AIExtractionService aiExtractionService;
    private final HeritageBusinessPersistenceService persistenceService;

    public AIWorkflowService(
            VideoAudioService videoAudioService,
            SpeechToTextService speechToTextService,
            AIExtractionService aiExtractionService,
            HeritageBusinessPersistenceService persistenceService) {

        this.videoAudioService = videoAudioService;
        this.speechToTextService = speechToTextService;
        this.aiExtractionService = aiExtractionService;
        this.persistenceService = persistenceService;
    }

    public DiscoveryProcessResponse process(String videoUrl) {
        return process(videoUrl, (progress, message) -> { });
    }

    public DiscoveryProcessResponse process(
            String videoUrl,
            BiConsumer<Integer, String> progressListener) {

        Path audioFile = videoAudioService.downloadAudio(videoUrl);
        progressListener.accept(35, "Audio extraction completed.");
        String transcript = speechToTextService.transcribe(audioFile);
        progressListener.accept(65, "Transcription completed.");
        AIExtractionResult extraction = aiExtractionService.extract(transcript);
        progressListener.accept(90, "AI extraction completed. Saving result...");
        persistenceService.save(extraction, videoUrl);

        return new DiscoveryProcessResponse(
                transcript,
                extraction
        );
    }
}
