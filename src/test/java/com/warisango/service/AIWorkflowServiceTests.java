package com.warisango.service;

import com.warisango.dto.AIExtractionResult;
import com.warisango.dto.DiscoveryProcessResponse;
import com.warisango.model.service.AIExtractionService;
import com.warisango.model.service.AIRecordService;
import com.warisango.model.service.AIWorkflowService;
import com.warisango.model.service.SpeechToTextService;
import com.warisango.model.service.VideoAudioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIWorkflowServiceTests {

    @Mock
    private VideoAudioService videoAudioService;

    @Mock
    private SpeechToTextService speechToTextService;

    @Mock
    private AIExtractionService aiExtractionService;

    @Mock
    private AIRecordService aiRecordService;

    @InjectMocks
    private AIWorkflowService aiWorkflowService;

    @Test
    void processCoordinatesTheCompleteWorkflow() {
        String videoUrl = "https://www.youtube.com/watch?v=video123";
        Path audioFile = Path.of("audio.m4a");
        String transcript = "Heritage restaurant transcript";
        AIExtractionResult extraction = new AIExtractionResult();

        when(videoAudioService.downloadAudio(videoUrl)).thenReturn(audioFile);
        when(speechToTextService.transcribe(audioFile)).thenReturn(transcript);
        when(aiExtractionService.extract(transcript)).thenReturn(extraction);
        when(aiRecordService.save(extraction, transcript, videoUrl))
                .thenReturn("record-123");

        DiscoveryProcessResponse response = aiWorkflowService.process(videoUrl);

        assertThat(response.transcript()).isEqualTo(transcript);
        assertThat(response.extraction()).isSameAs(extraction);
        assertThat(response.recordId()).isEqualTo("record-123");
        verify(aiRecordService).save(extraction, transcript, videoUrl);
    }
}
