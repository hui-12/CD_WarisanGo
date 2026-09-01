package com.warisango.model.service;

import com.google.cloud.firestore.GeoPoint;
import com.warisango.dto.BusinessCorrectionRequest;
import com.warisango.exception.BusinessReportException;
import com.warisango.model.BusinessReport;
import com.warisango.model.repository.AdminRepository;
import com.warisango.model.repository.BusinessReportRepository;
import org.springframework.stereotype.Service;

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

    public List<BusinessReport> getAll(String adminUserId) {
        requireAdmin(adminUserId);
        try {
            return reportRepository.findAll().stream()
                    .peek(this::enrich)
                    .sorted(Comparator.comparing(BusinessReport::getSubmittedAt,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();
        } catch (Exception exception) {
            throw new BusinessReportException("Unable to load business reports.", exception);
        }
    }

    public BusinessReport get(String reportId, String adminUserId) {
        requireAdmin(adminUserId);
        try {
            BusinessReport report = reportRepository.findById(reportId);
            if (report == null) throw new IllegalArgumentException("Business report was not found.");
            enrich(report);
            return report;
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
        BusinessReport report = getPending(reportId, adminUserId);
        Map<String, Object> corrections = buildCorrections(request);
        if (corrections.isEmpty()) {
            throw new IllegalArgumentException("Update at least one business field before resolving the report.");
        }
        businessService.updateReportedDetails(report.getBusinessId(), corrections);
        updateOutcome(reportId, adminUserId, "RESOLVED", normalizeOptional(request.getResolutionNote(), 500));
    }

    private BusinessReport getPending(String reportId, String adminUserId) {
        BusinessReport report = get(reportId, adminUserId);
        if (!"PENDING_REVIEW".equalsIgnoreCase(report.getStatus())) {
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

    private void enrich(BusinessReport report) {
        userService.getUserByUid(report.getTouristId()).ifPresent(user -> {
            report.setTouristName(user.getName());
            report.setTouristAvatar(user.getAvatar());
        });
        businessService.getBusinessById(report.getBusinessId()).ifPresent(report::setReportedBusiness);
    }

    private void requireAdmin(String userId) {
        if (userId == null || !adminRepository.existsByUserId(userId)) {
            throw new SecurityException("Admin access is required.");
        }
    }
}
