package com.warisango.controller;

import com.warisango.dto.VideoDTO;
import com.warisango.dto.DiscoveryProcessResponse;
import com.warisango.dto.DiscoverySearchRequest;
import com.warisango.dto.VideoProcessingRequest;
import com.warisango.service.AIWorkflowService;
import com.warisango.service.DiscoveryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discovery")
public class DiscoveryController {

    private final DiscoveryService discoveryService;
    private final AIWorkflowService aiWorkflowService;

    public DiscoveryController(
            DiscoveryService discoveryService,
            AIWorkflowService aiWorkflowService) {

        this.discoveryService =
                discoveryService;
        this.aiWorkflowService = aiWorkflowService;
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
