package com.warisango.model.service;

import com.warisango.dto.CommentDTO;
import com.warisango.dto.ReportDTO;
import com.warisango.dto.ReviewDTO;
import com.warisango.model.repository.AdminRepository;
import com.warisango.model.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Handles report validation, Admin authorization and moderation actions.
 */
@Service
public class ReportService {

    public static final String REVIEW_TARGET = "REVIEW";
    public static final String COMMENT_TARGET = "COMMENT";

    private static final List<String> ALLOWED_REASONS = List.of(
            "SPAM",
            "ABUSIVE_CONTENT",
            "HATE_SPEECH",
            "FALSE_INFORMATION",
            "OTHER"
    );

    private final ReportRepository reportRepository;
    private final AdminRepository adminRepository;
    private final ReviewService reviewService;
    private final CommentService commentService;
    private final String currentModerationUserId;

    public ReportService(
            ReportRepository reportRepository,
            AdminRepository adminRepository,
            ReviewService reviewService,
            CommentService commentService,
            @Value("${warisango.moderation.current-user-id:user_001}") String currentModerationUserId) {
        this.reportRepository = reportRepository;
        this.adminRepository = adminRepository;
        this.reviewService = reviewService;
        this.commentService = commentService;
        this.currentModerationUserId = currentModerationUserId;
    }

    public ReportDTO createReport(
            String targetType,
            String reviewId,
            String commentId,
            String reason) {

        String normalizedTargetType = normalizeTargetType(targetType);
        String targetId = validateTarget(normalizedTargetType, reviewId, commentId);
        String normalizedReason = normalizeReason(reason);
        String reporterTouristId = reviewService.getCurrentTouristId();

        if (reportRepository.existsByReporterAndTarget(
                reporterTouristId,
                normalizedTargetType,
                targetId)) {
            throw new IllegalArgumentException("You have already reported this content.");
        }

        ReportDTO report = new ReportDTO();
        report.setReportId(reportRepository.generateNextReportId());
        report.setReporterTouristId(reporterTouristId);
        report.setTargetType(normalizedTargetType);
        report.setReason(normalizedReason);
        report.setStatus("PENDING");

        if (REVIEW_TARGET.equals(normalizedTargetType)) {
            report.setReviewId(reviewId);
        } else {
            report.setCommentId(commentId);
            CommentDTO comment = commentService.getCommentIncludingHidden(commentId);
            if (comment != null) {
                report.setReviewId(comment.getReviewId());
            }
        }

        reportRepository.save(report);
        return report;
    }

    public List<ReportDTO> getAllReports() {
        List<ReportDTO> reports = reportRepository.findAll();
        reports.forEach(this::enrichTarget);

        return reports.stream()
                .sorted(Comparator
                        .comparing((ReportDTO report) -> !"PENDING".equalsIgnoreCase(report.getStatus()))
                        .thenComparing(
                                ReportDTO::getCreatedAt,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public ReportDTO getReport(String reportId) {
        ReportDTO report = reportRepository.findByReportId(reportId);
        if (report != null) {
            enrichTarget(report);
        }
        return report;
    }

    public void dismissReport(String reportId) {
        resolveReport(reportId, "DISMISSED");
    }

    public void hideReport(String reportId) {
        requireAdmin();
        ReportDTO report = requireReport(reportId);

        if (!"PENDING".equalsIgnoreCase(report.getStatus())) {
            throw new IllegalArgumentException("Only pending reports can be hidden.");
        }

        if (REVIEW_TARGET.equals(report.getTargetType())) {
            reviewService.hideReview(report.getReviewId());
        } else {
            commentService.hideComment(report.getCommentId());
        }

        resolveReport(report, "HIDDEN");
    }

    public void restoreReportTarget(String reportId) {
        requireAdmin();
        ReportDTO report = requireReport(reportId);

        if (!"HIDDEN".equalsIgnoreCase(report.getStatus())) {
            throw new IllegalArgumentException("Only hidden content can be restored.");
        }

        if (REVIEW_TARGET.equals(report.getTargetType())) {
            reviewService.restoreReview(report.getReviewId());
        } else {
            commentService.restoreComment(report.getCommentId());
        }

        resolveReport(report, "RESTORED");
    }

    public void deleteReportTarget(String reportId) {
        requireAdmin();
        ReportDTO report = requireReport(reportId);

        if (REVIEW_TARGET.equals(report.getTargetType())) {
            reviewService.deleteReviewByAdmin(report.getReviewId());
        } else {
            commentService.deleteCommentByAdmin(report.getCommentId());
        }

        resolveReport(report, "DELETED");
    }

    public void requireAdmin() {
        if (!adminRepository.existsByUserId(currentModerationUserId)) {
            throw new SecurityException("Admin access is required.");
        }
    }

    public String getCurrentModerationUserId() {
        return currentModerationUserId;
    }

    private void resolveReport(String reportId, String status) {
        requireAdmin();
        resolveReport(requireReport(reportId), status);
    }

    private void resolveReport(ReportDTO report, String status) {
        report.setStatus(status);
        report.setResolvedBy(getCurrentModerationAdminId());
        report.setResolvedAt(LocalDateTime.now().toString());
        reportRepository.update(report);
    }

    public String getCurrentModerationAdminId() {
        String adminId = adminRepository.findAdminIdByUserId(currentModerationUserId);
        return adminId == null || adminId.isBlank() ? currentModerationUserId : adminId;
    }

    private ReportDTO requireReport(String reportId) {
        ReportDTO report = reportRepository.findByReportId(reportId);
        if (report == null) {
            throw new IllegalArgumentException("Report was not found.");
        }
        return report;
    }

    private String validateTarget(String targetType, String reviewId, String commentId) {
        if (REVIEW_TARGET.equals(targetType)) {
            if (reviewId == null || reviewId.isBlank()
                    || reviewService.getReviewIncludingHidden(reviewId) == null) {
                throw new IllegalArgumentException("Review was not found.");
            }
            return reviewId;
        }

        if (commentId == null || commentId.isBlank()
                || commentService.getCommentIncludingHidden(commentId) == null) {
            throw new IllegalArgumentException("Comment was not found.");
        }
        return commentId;
    }

    private String normalizeTargetType(String targetType) {
        String normalized = targetType == null
                ? ""
                : targetType.trim().toUpperCase(Locale.ROOT);

        if (!REVIEW_TARGET.equals(normalized) && !COMMENT_TARGET.equals(normalized)) {
            throw new IllegalArgumentException("Invalid report target.");
        }

        return normalized;
    }

    private String normalizeReason(String reason) {
        String normalized = reason == null
                ? ""
                : reason.trim().toUpperCase(Locale.ROOT);

        if (!ALLOWED_REASONS.contains(normalized)) {
            throw new IllegalArgumentException("Please choose a valid report reason.");
        }

        return normalized;
    }

    private void enrichTarget(ReportDTO report) {
        if (REVIEW_TARGET.equals(report.getTargetType())) {
            ReviewDTO review = reviewService.getReviewIncludingHidden(report.getReviewId());
            if (review != null) {
                report.setTargetText(review.getReviewText());
                report.setTargetOwnerId(review.getTouristId());
            }
            return;
        }

        CommentDTO comment = commentService.getCommentIncludingHidden(report.getCommentId());
        if (comment != null) {
            report.setReviewId(comment.getReviewId());
            report.setTargetText(comment.getCommentText());
            report.setTargetOwnerId(comment.getTouristId());
        }
    }
}
