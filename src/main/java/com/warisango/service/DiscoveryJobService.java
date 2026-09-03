package com.warisango.service;

import com.warisango.dto.DiscoveryJobStartResponse;
import com.warisango.dto.DiscoveryJobStatusResponse;
import com.warisango.dto.DiscoveryProcessResponse;
import com.warisango.exception.DiscoveryJobNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Service
public class DiscoveryJobService {

    private static final Logger logger = LoggerFactory.getLogger(DiscoveryJobService.class);
    private static final Duration JOB_RETENTION = Duration.ofHours(24);

    private final AIWorkflowService aiWorkflowService;
    private final ExecutorService executorService;
    private final Map<String, JobState> jobs = new ConcurrentHashMap<>();

    public DiscoveryJobService(
            AIWorkflowService aiWorkflowService,
            @Qualifier("discoveryExecutor") ExecutorService executorService) {
        this.aiWorkflowService = aiWorkflowService;
        this.executorService = executorService;
    }

    public DiscoveryJobStartResponse start(String videoUrl) {
        removeExpiredJobs();

        String jobId = UUID.randomUUID().toString();
        JobState job = new JobState(jobId);
        jobs.put(jobId, job);
        executorService.submit(() -> process(job, videoUrl));

        return new DiscoveryJobStartResponse(jobId);
    }

    public DiscoveryJobStatusResponse getStatus(String jobId) {
        JobState job = jobs.get(jobId);
        if (job == null) {
            throw new DiscoveryJobNotFoundException(jobId);
        }
        return job.toResponse();
    }

    private void process(JobState job, String videoUrl) {
        try {
            job.update("PROCESSING", 10, "Downloading video audio...");
            DiscoveryProcessResponse result = aiWorkflowService.process(
                    videoUrl,
                    (progress, message) -> job.update("PROCESSING", progress, message));
            job.complete(result);
        } catch (Exception exception) {
            logger.warn("Discovery processing job {} failed: {}", job.jobId, exception.getMessage());
            job.fail(exception.getMessage() == null ? "Video processing failed." : exception.getMessage());
        }
    }

    private void removeExpiredJobs() {
        Instant cutoff = Instant.now().minus(JOB_RETENTION);
        jobs.entrySet().removeIf(entry -> entry.getValue().updatedAt.isBefore(cutoff));
    }

    private static final class JobState {
        private final String jobId;
        private volatile String status = "QUEUED";
        private volatile int progress = 0;
        private volatile String message = "Waiting to start...";
        private volatile DiscoveryProcessResponse result;
        private volatile Instant updatedAt = Instant.now();

        private JobState(String jobId) {
            this.jobId = jobId;
        }

        private void update(String status, int progress, String message) {
            this.status = status;
            this.progress = progress;
            this.message = message;
            this.updatedAt = Instant.now();
        }

        private void complete(DiscoveryProcessResponse result) {
            this.result = result;
            update("COMPLETED", 100, "AI Heritage Discovery completed successfully.");
        }

        private void fail(String message) {
            update("FAILED", progress, message);
        }

        private DiscoveryJobStatusResponse toResponse() {
            return new DiscoveryJobStatusResponse(jobId, status, progress, message, result);
        }
    }
}
