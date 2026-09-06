package com.warisango.service;

import com.warisango.dto.BusinessPhotoReportView;
import com.warisango.dto.BusinessPhotoView;
import com.warisango.dto.AdminPhotoReportGroup;
import com.warisango.model.HeritageBusinessImage;
import com.warisango.model.BusinessPhotoReport;
import com.warisango.repository.AdminRepository;
import com.warisango.repository.BusinessPhotoReportRepository;
import com.warisango.repository.BusinessRepository;
import com.warisango.repository.HeritageBusinessImageRepository;
import com.warisango.util.ReviewDateFormatter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Arrays;

@Service
public class BusinessPhotoService {
    private static final Set<String> REPORT_REASONS = Set.of(
            "INAPPROPRIATE", "SPAM", "NOT_THIS_PLACE", "PRIVACY", "OTHER");

    private final HeritageBusinessImageRepository photoRepository;
    private final BusinessPhotoReportRepository reportRepository;
    private final BusinessRepository businessRepository;
    private final ReviewPhotoStorageService storageService;
    private final UserService userService;
    private final AdminRepository adminRepository;

    public BusinessPhotoService(
            HeritageBusinessImageRepository photoRepository,
            BusinessPhotoReportRepository reportRepository,
            BusinessRepository businessRepository,
            ReviewPhotoStorageService storageService,
            UserService userService,
            AdminRepository adminRepository) {
        this.photoRepository = photoRepository;
        this.reportRepository = reportRepository;
        this.businessRepository = businessRepository;
        this.storageService = storageService;
        this.userService = userService;
        this.adminRepository = adminRepository;
    }

    public List<BusinessPhotoView> getPhotos(String businessId) {
        return photoRepository.findByBusinessId(businessId).stream().map(this::toView).toList();
    }

    public void upload(String businessId, String userId, MultipartFile[] files) {
        requireUser(userId);
        if (businessRepository.findById(businessId).isEmpty()) {
            throw new IllegalArgumentException("Business was not found.");
        }
        List<MultipartFile> photos = files == null ? List.of() : Arrays.stream(files)
                .filter(file -> file != null && !file.isEmpty()).toList();
        storageService.validateFiles(photos);
        if (photos.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one photo.");
        }
        for (MultipartFile file : photos) {
            var stored = storageService.storeBusinessPhoto(businessId, file);
            try {
                photoRepository.save(businessId, stored.publicUrl(), stored.storagePath(), userId);
            } catch (RuntimeException exception) {
                storageService.delete(stored.publicUrl(), stored.storagePath());
                throw exception;
            }
        }
    }

    public void report(String photoId, String userId, String reason, String details) {
        requireUser(userId);
        HeritageBusinessImage photo = requirePhoto(photoId);
        String normalizedReason = reason == null ? "" : reason.trim().toUpperCase(Locale.ROOT);
        if (!REPORT_REASONS.contains(normalizedReason)) {
            throw new IllegalArgumentException("Please select a valid report reason.");
        }
        if (reportRepository.hasPendingReport(photoId, userId)) {
            throw new IllegalArgumentException("You have already reported this photo.");
        }
        String normalizedDetails = details == null || details.isBlank() ? null : details.trim();
        if (normalizedDetails != null && normalizedDetails.length() > 500) {
            throw new IllegalArgumentException("Report details cannot exceed 500 characters.");
        }
        reportRepository.save(photoId, photo.businessId(), userId, normalizedReason, normalizedDetails);
    }

    public List<BusinessPhotoReportView> getReports(String adminUserId) {
        requireAdmin(adminUserId);
        return reportRepository.findAll().stream()
                .sorted(Comparator.comparing(BusinessPhotoReport::reportedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toReportView)
                .toList();
    }

    public List<AdminPhotoReportGroup> groupReports(List<BusinessPhotoReportView> reports) {
        return List.of(
                groupByStatus(reports, "PENDING", "Pending"),
                groupByStatus(reports, "DISMISSED", "Dismissed"),
                groupByStatus(reports, "REMOVED", "Removed")
        );
    }

    private AdminPhotoReportGroup groupByStatus(List<BusinessPhotoReportView> reports,
                                                String status, String label) {
        List<BusinessPhotoReportView> matches = reports.stream()
                .filter(report -> status.equalsIgnoreCase(report.status()))
                .toList();
        return new AdminPhotoReportGroup(status, label, matches);
    }

    public void dismiss(String reportId, String adminUserId) {
        requireAdmin(adminUserId);
        requirePendingReport(reportId);
        reportRepository.resolve(reportId, "DISMISSED", adminUserId);
    }

    public void takeDown(String reportId, String adminUserId) {
        requireAdmin(adminUserId);
        BusinessPhotoReport report = requirePendingReport(reportId);
        HeritageBusinessImage photo = requirePhoto(report.photoId());
        photoRepository.markRemoved(photo.photoId());
        reportRepository.resolve(reportId, "REMOVED", adminUserId);
        storageService.delete(photo.imageUrl(), photo.storagePath());
    }

    public void takeDownPhoto(String photoId, String adminUserId) {
        requireAdmin(adminUserId);
        HeritageBusinessImage photo = requirePhoto(photoId);
        photoRepository.markRemoved(photoId);
        storageService.delete(photo.imageUrl(), photo.storagePath());
    }

    private BusinessPhotoReport requirePendingReport(String reportId) {
        BusinessPhotoReport report = reportRepository.findById(reportId);
        if (report == null) {
            throw new IllegalArgumentException("Photo report was not found.");
        }
        if (!"PENDING".equalsIgnoreCase(report.status())) {
            throw new IllegalArgumentException("This photo report has already been resolved.");
        }
        return report;
    }

    private HeritageBusinessImage requirePhoto(String photoId) {
        HeritageBusinessImage photo = photoRepository.findById(photoId);
        if (photo == null || "REMOVED".equalsIgnoreCase(photo.status())) {
            throw new IllegalArgumentException("Photo was not found.");
        }
        return photo;
    }

    private BusinessPhotoView toView(HeritageBusinessImage photo) {
        String uploaderName = photo.uploadedBy() == null || photo.uploadedBy().isBlank()
                ? "WarisanGo contributor"
                : userService.getDisplayNameByUserId(photo.uploadedBy());
        if (uploaderName == null || uploaderName.isBlank()) {
            uploaderName = "WarisanGo contributor";
        }
        return new BusinessPhotoView(photo.photoId(), photo.businessId(), photo.imageUrl(),
                photo.uploadedBy(), uploaderName, ReviewDateFormatter.format(photo.uploadedAt()));
    }

    private BusinessPhotoReportView toReportView(BusinessPhotoReport report) {
        HeritageBusinessImage photo = photoRepository.findById(report.photoId());
        boolean photoAvailable = photo != null && !"REMOVED".equalsIgnoreCase(photo.status());
        String imageUrl = photoAvailable ? photo.imageUrl() : null;
        String uploaderName = photo == null || photo.uploadedBy() == null
                ? "Unknown contributor" : userService.getDisplayNameByUserId(photo.uploadedBy());
        String businessName = businessRepository.findHeritageBusinessById(report.businessId())
                .map(business -> business.name()).orElse(report.businessId());
        return new BusinessPhotoReportView(
                report.reportId(), report.photoId(), report.businessId(), businessName, imageUrl,
                report.reporterId(), userService.getDisplayNameByUserId(report.reporterId()), uploaderName,
                report.reason(), report.details(), report.status(), ReviewDateFormatter.format(report.reportedAt()),
                photoAvailable);
    }

    private void requireUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new AccessDeniedException("Authentication is required.");
        }
    }

    private void requireAdmin(String userId) {
        requireUser(userId);
        if (!adminRepository.existsByUserId(userId)) {
            throw new AccessDeniedException("Admin access is required.");
        }
    }
}
