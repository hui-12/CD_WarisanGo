package com.warisango.service;

import com.warisango.dto.BusinessReportSummary;
import com.warisango.dto.BusinessReportView;
import com.warisango.dto.HeritageBusinessDTO;
import com.warisango.exception.BusinessReportException;
import com.warisango.model.BusinessReport;
import com.warisango.repository.AdminRepository;
import com.warisango.repository.BusinessReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class BusinessReportService {
    private static final Set<String> REASONS = Set.of(
            "PERMANENTLY_CLOSED", "INCORRECT_LOCATION", "OUTDATED_INFO", "NAME_ERROR", "OTHER");

    private final BusinessReportRepository reportRepository;
    private final BusinessService businessService;
    private final UserService userService;
    private final AdminRepository adminRepository;

    public BusinessReportService(BusinessReportRepository reportRepository,
                                 BusinessService businessService,
                                 UserService userService,
                                 AdminRepository adminRepository) {
        this.reportRepository = reportRepository;
        this.businessService = businessService;
        this.userService = userService;
        this.adminRepository = adminRepository;
    }

    public void submit(String touristId, String businessId, String reason, String details) {
        String normalizedReason = reason == null ? "" : reason.trim().toUpperCase(Locale.ROOT);
        if (!REASONS.contains(normalizedReason)) {
            throw new IllegalArgumentException("Please select a valid report reason.");
        }
        if (details == null || details.isBlank() || details.trim().length() > 1000) {
            throw new IllegalArgumentException("Report details must contain between 1 and 1000 characters.");
        }
        if (businessService.getBusinessById(businessId).isEmpty()) {
            throw new IllegalArgumentException("Only approved businesses can be reported.");
        }
        try {
            reportRepository.save(touristId, businessId, normalizedReason, details.trim());
        } catch (Exception exception) {
            throw new BusinessReportException("Unable to submit the business report.", exception);
        }
    }

    public List<BusinessReportView> getAll(String adminUserId) {
        requireAdmin(adminUserId);
        try {
            return reportRepository.findAll().stream()
                    .map(this::toView)
                    .sorted(Comparator.comparing(BusinessReportView::submittedAt,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();
        } catch (Exception exception) {
            throw new BusinessReportException("Unable to load business reports.", exception);
        }
    }

    public BusinessReportSummary summarize(List<BusinessReportView> reports) {
        return new BusinessReportSummary(
                reports.size(),
                countByStatus(reports, "PENDING_REVIEW"),
                countByStatus(reports, "RESOLVED"),
                countByStatus(reports, "DISMISSED")
        );
    }

    public BusinessReportView get(String reportId, String adminUserId) {
        requireAdmin(adminUserId);
        try {
            BusinessReport report = reportRepository.findById(reportId);
            if (report == null) throw new IllegalArgumentException("Business report was not found.");
            return toView(report);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessReportException("Unable to load the business report.", exception);
        }
    }

    public void dismiss(String reportId, String adminUserId, String note) {
        updateOutcome(reportId, adminUserId, "DISMISSED", normalizeOptional(note, 500));
    }

    public void resolveWithoutChanges(String reportId, String adminUserId, String note) {
        updateOutcome(reportId, adminUserId, "RESOLVED", normalizeOptional(note, 500));
    }

    private BusinessReportView getPending(String reportId, String adminUserId) {
        BusinessReportView report = get(reportId, adminUserId);
        if (!"PENDING_REVIEW".equalsIgnoreCase(report.status())) {
            throw new IllegalArgumentException("Only pending reports can be processed.");
        }
        return report;
    }

    private void updateOutcome(String reportId, String adminUserId, String status, String note) {
        getPending(reportId, adminUserId);
        try {
            String adminId = adminRepository.findAdminIdByUserId(adminUserId);
            reportRepository.updateOutcome(reportId, status,
                    adminId == null || adminId.isBlank() ? adminUserId : adminId, note);
        } catch (Exception exception) {
            throw new BusinessReportException("Unable to update the business report.", exception);
        }
    }


    private String normalizeOptional(String value, int maximumLength) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (normalized.length() > maximumLength) {
            throw new IllegalArgumentException("Resolution note is too long.");
        }
        return normalized;
    }

    private BusinessReportView toView(BusinessReport report) {
        String touristName = null;
        String touristAvatar = null;
        var tourist = userService.getUserByUid(report.getTouristId());
        if (tourist.isPresent()) {
            touristName = tourist.get().getName();
            touristAvatar = tourist.get().getAvatar();
        }
        HeritageBusinessDTO business = businessService.getBusinessById(report.getBusinessId()).orElse(null);
        return BusinessReportView.from(report, touristName, touristAvatar, business);
    }

    private void requireAdmin(String userId) {
        if (userId == null || !adminRepository.existsByUserId(userId)) {
            throw new AccessDeniedException("Admin access is required.");
        }
    }

    private long countByStatus(List<BusinessReportView> reports, String status) {
        return reports.stream()
                .filter(report -> status.equalsIgnoreCase(report.status()))
                .count();
    }
}
