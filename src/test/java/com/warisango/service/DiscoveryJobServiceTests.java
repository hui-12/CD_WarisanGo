package com.warisango.service;

import com.warisango.dto.DiscoveryJobStartResponse;
import com.warisango.dto.DiscoveryJobStatusResponse;
import com.warisango.dto.DiscoveryProcessResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DiscoveryJobServiceTests {

    private final AIWorkflowService aiWorkflowService = mock(AIWorkflowService.class);
    private final DiscoveryJobService discoveryJobService = new DiscoveryJobService(aiWorkflowService);

    @AfterEach
    void shutdownExecutor() {
        discoveryJobService.shutdown();
    }

    @Test
    @SuppressWarnings("unchecked")
    void completedJobRemainsAvailableById() throws InterruptedException {
        String videoUrl = "https://www.youtube.com/watch?v=video123";
        DiscoveryProcessResponse expectedResult = mock(DiscoveryProcessResponse.class);

        when(aiWorkflowService.process(eq(videoUrl), any(BiConsumer.class)))
                .thenReturn(expectedResult);

        DiscoveryJobStartResponse startedJob = discoveryJobService.start(videoUrl);
        DiscoveryJobStatusResponse job = waitForTerminalState(startedJob.jobId());

        assertEquals("COMPLETED", job.status());
        assertEquals(100, job.progress());
        assertSame(expectedResult, job.result());
    }

    private DiscoveryJobStatusResponse waitForTerminalState(String jobId) throws InterruptedException {
        for (int attempt = 0; attempt < 50; attempt++) {
            DiscoveryJobStatusResponse job = discoveryJobService.getStatus(jobId);
            if (job.status().equals("COMPLETED") || job.status().equals("FAILED")) {
                return job;
            }
            Thread.sleep(20);
        }

        return discoveryJobService.getStatus(jobId);
    }
}
