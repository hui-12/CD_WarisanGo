package com.warisango.service;

import com.warisango.dto.AIExtractionResult;
import com.warisango.dto.DiscoveryProcessResponse;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

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
        Path audioFile = videoAudioService.downloadAudio(videoUrl);
        String transcript = speechToTextService.transcribe(audioFile);
        AIExtractionResult extraction = aiExtractionService.extract(transcript);
        persistenceService.save(extraction, videoUrl);

        return new DiscoveryProcessResponse(
                transcript,
                extraction
        );
    }
}
