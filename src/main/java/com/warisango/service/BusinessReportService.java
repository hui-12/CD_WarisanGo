package com.warisango.service;

import com.google.cloud.firestore.GeoPoint;
import com.warisango.dto.BusinessCorrectionRequest;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    public void resolve(String reportId, String adminUserId, BusinessCorrectionRequest request) {
        BusinessReportView report = getPending(reportId, adminUserId);
        Map<String, Object> corrections = buildCorrections(request);
        if (corrections.isEmpty()) {
            throw new IllegalArgumentException("Update at least one business field before resolving the report.");
        }
        businessService.updateReportedDetails(report.businessId(), corrections);
        updateOutcome(reportId, adminUserId, "RESOLVED", normalizeOptional(request.getResolutionNote(), 500));
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

    private Map<String, Object> buildCorrections(BusinessCorrectionRequest request) {
        Map<String, Object> corrections = new LinkedHashMap<>();
        putIfPresent(corrections, "name", request.getName(), 120);
        putIfPresent(corrections, "address", request.getAddress(), 300);
        putIfPresent(corrections, "city", request.getCity(), 100);
        putIfPresent(corrections, "state", request.getState(), 100);
        putIfPresent(corrections, "operatingHour", request.getOperatingHour(), 50);
        boolean hasLatitude = request.getLatitude() != null && !request.getLatitude().isBlank();
        boolean hasLongitude = request.getLongitude() != null && !request.getLongitude().isBlank();
        if (hasLatitude != hasLongitude) {
            throw new IllegalArgumentException("Both latitude and longitude are required to update the location.");
        }
        if (hasLatitude) {
            try {
                double latitude = Double.parseDouble(request.getLatitude());
                double longitude = Double.parseDouble(request.getLongitude());
                if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                    throw new IllegalArgumentException("Location coordinates are outside the valid range.");
                }
                corrections.put("location", new GeoPoint(latitude, longitude));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Location coordinates must be numbers.");
            }
        }
        return corrections;
    }

    private void putIfPresent(Map<String, Object> corrections, String field, String value, int maximumLength) {
        if (value == null || value.isBlank()) return;
        String normalized = value.trim();
        if (normalized.length() > maximumLength) {
            throw new IllegalArgumentException(field + " is too long.");
        }
        corrections.put(field, normalized);
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
