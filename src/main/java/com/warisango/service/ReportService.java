package com.warisango.service;

import com.warisango.dto.CommentDTO;
import com.warisango.dto.ReportDTO;
import com.warisango.dto.ReviewDTO;
import com.warisango.repository.AdminRepository;
import com.warisango.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

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
    private final UserService userService;

    public ReportService(
            ReportRepository reportRepository,
            AdminRepository adminRepository,
            ReviewService reviewService,
            CommentService commentService,
            UserService userService) {
        this.reportRepository = reportRepository;
        this.adminRepository = adminRepository;
        this.reviewService = reviewService;
        this.commentService = commentService;
        this.userService = userService;
    }

    public ReportDTO createReport(
            String targetType,
            String reviewId,
            String commentId,
            String reason,
            String reporterUserId) {

        if (reporterUserId == null || reporterUserId.isBlank()) {
            throw new IllegalArgumentException("Authenticated user ID is required.");
        }

        String normalizedTargetType = normalizeTargetType(targetType);
        String targetId = validateTarget(normalizedTargetType, reviewId, commentId);
        String normalizedReason = normalizeReason(reason);
        if (reportRepository.existsByReporterAndTarget(
                reporterUserId,
                normalizedTargetType,
                targetId)) {
            throw new IllegalArgumentException("You have already reported this content.");
        }

        ReportDTO report = new ReportDTO();
        report.setReportId(reportRepository.generateNextReportId());
        report.setReporterTouristId(reporterUserId);
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
        reports.forEach(this::enrichReport);

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
            enrichReport(report);
        }
        return report;
    }

    public void dismissReport(String reportId, String currentUserId) {
        resolveReport(reportId, "DISMISSED", currentUserId);
    }

    public void hideReport(String reportId, String currentUserId) {
        requireAdmin(currentUserId);
        ReportDTO report = requireReport(reportId);

        if (!"PENDING".equalsIgnoreCase(report.getStatus())) {
            throw new IllegalArgumentException("Only pending reports can be hidden.");
        }

        if (REVIEW_TARGET.equals(report.getTargetType())) {
            reviewService.hideReview(report.getReviewId());
        } else {
            commentService.hideComment(report.getCommentId());
        }

        resolveReport(report, "HIDDEN", currentUserId);
    }

    public void restoreReportTarget(String reportId, String currentUserId) {
        requireAdmin(currentUserId);
        ReportDTO report = requireReport(reportId);

        if (!"HIDDEN".equalsIgnoreCase(report.getStatus())) {
            throw new IllegalArgumentException("Only hidden content can be restored.");
        }

        if (REVIEW_TARGET.equals(report.getTargetType())) {
            reviewService.restoreReview(report.getReviewId());
        } else {
            commentService.restoreComment(report.getCommentId());
        }

        resolveReport(report, "RESTORED", currentUserId);
    }

    public void deleteReportTarget(String reportId, String currentUserId) {
        requireAdmin(currentUserId);
        ReportDTO report = requireReport(reportId);

        if (REVIEW_TARGET.equals(report.getTargetType())) {
            reviewService.deleteReviewByAdmin(report.getReviewId());
        } else {
            commentService.deleteCommentByAdmin(report.getCommentId());
        }

        resolveReport(report, "DELETED", currentUserId);
    }

    public void requireAdmin(String currentUserId) {
        if (currentUserId == null || currentUserId.isBlank()
                || !adminRepository.existsByUserId(currentUserId)) {
            throw new AccessDeniedException("Admin access is required.");
        }
    }

    private void resolveReport(String reportId, String status, String currentUserId) {
        requireAdmin(currentUserId);
        resolveReport(requireReport(reportId), status, currentUserId);
    }

    private void resolveReport(ReportDTO report, String status, String currentUserId) {
        report.setStatus(status);
        report.setResolvedBy(getCurrentModerationAdminId(currentUserId));
        report.setResolvedAt(LocalDateTime.now().toString());
        reportRepository.update(report);
    }

    public String getCurrentModerationAdminId(String currentUserId) {
        String adminId = adminRepository.findAdminIdByUserId(currentUserId);
        return adminId == null || adminId.isBlank() ? currentUserId : adminId;
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

    private void enrichReport(ReportDTO report) {
        report.setReporterName(userService.getDisplayNameByUserId(report.getReporterTouristId()));
        enrichTarget(report);
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
