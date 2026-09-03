package com.warisango.controller;

import com.warisango.service.ContentModerationService;
import com.warisango.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ReviewReportApiController {

    private final ReportService reportService;
    private final ContentModerationService contentModerationService;

    public ReviewReportApiController(
            ReportService reportService,
            ContentModerationService contentModerationService) {
        this.reportService = reportService;
        this.contentModerationService = contentModerationService;
    }

    @PostMapping("/reviews/reports")
    public ResponseEntity<Map<String, String>> createReport(
            @RequestParam String targetType,
            @RequestParam(required = false) String reviewId,
            @RequestParam(required = false) String commentId,
            @RequestParam String reason,
            Authentication authentication) {
        reportService.createReport(targetType, reviewId, commentId, reason, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "Thank you. Your report has been submitted."));
    }

    @PostMapping("/reviews/moderation/check")
    public ResponseEntity<Map<String, Object>> checkText(@RequestParam String text) {
        boolean prohibited = contentModerationService.containsProhibitedContent(text);
        return ResponseEntity.ok(Map.of(
                "allowed", !prohibited,
                "message", prohibited ? contentModerationService.getProhibitedMessage() : ""
        ));
    }
}
