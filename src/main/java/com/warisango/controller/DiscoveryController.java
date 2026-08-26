package com.warisango.controller;

import com.warisango.dto.VideoDTO;
import com.warisango.dto.DiscoveryProcessResponse;
import com.warisango.dto.DiscoverySearchRequest;
import com.warisango.dto.VideoProcessingRequest;
import com.warisango.dto.TikTokSearchRequest;
import com.warisango.dto.TikTokVideoDTO;
import com.warisango.dto.DiscoveryJobStartResponse;
import com.warisango.dto.DiscoveryJobStatusResponse;
import com.warisango.model.service.AIWorkflowService;
import com.warisango.model.service.DiscoveryService;
import com.warisango.model.service.DiscoveryJobService;
import com.warisango.model.service.TikTokScraperService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discovery")
public class DiscoveryController {

    private final DiscoveryService discoveryService;
    private final AIWorkflowService aiWorkflowService;
    private final TikTokScraperService tikTokScraperService;
    private final DiscoveryJobService discoveryJobService;

    public DiscoveryController(
            DiscoveryService discoveryService,
            AIWorkflowService aiWorkflowService,
            TikTokScraperService tikTokScraperService,
            DiscoveryJobService discoveryJobService) {

        this.discoveryService =
                discoveryService;
        this.aiWorkflowService = aiWorkflowService;
        this.tikTokScraperService = tikTokScraperService;
        this.discoveryJobService = discoveryJobService;
    }

    @PostMapping("/process/jobs")
    public ResponseEntity<DiscoveryJobStartResponse> startProcessingJob(
            @Valid @RequestBody VideoProcessingRequest request) {

        return ResponseEntity.accepted().body(discoveryJobService.start(request.videoUrl()));
    }

    @GetMapping("/process/jobs/{jobId}")
    public ResponseEntity<DiscoveryJobStatusResponse> getProcessingJob(
            @PathVariable String jobId) {

        return ResponseEntity.ok(discoveryJobService.getStatus(jobId));
    }

    @PostMapping("/search/tiktok")
    public ResponseEntity<List<TikTokVideoDTO>> searchTikTok(
            @Valid @RequestBody TikTokSearchRequest request) {

        return ResponseEntity.ok(tikTokScraperService.search(request));
    }

    @PostMapping("/search")
    public ResponseEntity<List<VideoDTO>> search(
            @Valid @RequestBody DiscoverySearchRequest request) {

        return ResponseEntity.ok(
                discoveryService.discover(request.keyword())
        );
    }

    @PostMapping("/process")
    public ResponseEntity<DiscoveryProcessResponse> process(
            @Valid @RequestBody VideoProcessingRequest request) {

        return ResponseEntity.ok(
                aiWorkflowService.process(request.videoUrl())
        );
    }
}
