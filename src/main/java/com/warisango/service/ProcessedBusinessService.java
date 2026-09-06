package com.warisango.service;

import com.warisango.dto.ProcessedBusinessEntryView;
import com.warisango.dto.BusinessPhotoView;
import com.warisango.model.HeritageBusiness;
import com.warisango.repository.BusinessRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class ProcessedBusinessService {

    private static final String APPROVED_STATUS = "Approved";
    private static final String REJECTED_STATUS = "Rejected";
    private static final List<String> REVIEWED_STATUSES = List.of(
            APPROVED_STATUS,
            REJECTED_STATUS,
            "Inactive"
    );
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
                    .withZone(ZoneId.of("Asia/Kuala_Lumpur"));

    private final BusinessRepository heritageBusinessRepository;
    private final BusinessPhotoService businessPhotoService;

    public ProcessedBusinessService(BusinessRepository heritageBusinessRepository,
                                    BusinessPhotoService businessPhotoService) {
        this.heritageBusinessRepository = heritageBusinessRepository;
        this.businessPhotoService = businessPhotoService;
    }

    public List<ProcessedBusinessEntryView> findAll() {
        return heritageBusinessRepository.findByStatuses(REVIEWED_STATUSES)
                .stream()
                .map(this::toView)
                .sorted(Comparator.comparing(
                        ProcessedBusinessEntryView::reviewedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();
    }

    private ProcessedBusinessEntryView toView(HeritageBusiness business) {
        Instant reviewedAt = REJECTED_STATUS.equals(business.status())
                ? business.rejectedAt()
                : business.approveAt();
        String reviewedAtDisplay = reviewedAt == null
                ? "Time not recorded"
                : DATE_TIME_FORMATTER.format(reviewedAt);

        List<BusinessPhotoView> photos = businessPhotoService.getPhotos(business.businessId());
        BusinessPhotoView previewPhoto = photos.isEmpty() ? null : photos.getFirst();
        return new ProcessedBusinessEntryView(
                business.businessId(),
                business.name(),
                business.address(),
                business.status(),
                reviewedAt,
                reviewedAtDisplay,
                previewPhoto == null ? null : previewPhoto.photoId(),
                previewPhoto == null ? null : previewPhoto.imageUrl(),
                previewPhoto == null ? null : previewPhoto.uploaderName(),
                previewPhoto == null ? null : previewPhoto.uploadedAtDisplay(),
                photos.size()
        );
    }
}
