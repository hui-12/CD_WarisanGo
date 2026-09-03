package com.warisango.service;

import com.warisango.dto.AuditLogEntryView;
import com.warisango.model.HeritageBusiness;
import com.warisango.repository.BusinessRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class AuditLogService {

    private static final String APPROVED_STATUS = "Approved";
    private static final String REJECTED_STATUS = "Rejected";
    private static final List<String> REVIEWED_STATUSES = List.of(
            APPROVED_STATUS,
            REJECTED_STATUS
    );
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
                    .withZone(ZoneId.of("Asia/Kuala_Lumpur"));

    private final BusinessRepository heritageBusinessRepository;

    public AuditLogService(BusinessRepository heritageBusinessRepository) {
        this.heritageBusinessRepository = heritageBusinessRepository;
    }

    public List<AuditLogEntryView> findAll() {
        return heritageBusinessRepository.findByStatuses(REVIEWED_STATUSES)
                .stream()
                .map(this::toView)
                .sorted(Comparator.comparing(
                        AuditLogEntryView::reviewedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();
    }

    private AuditLogEntryView toView(HeritageBusiness business) {
        Instant reviewedAt = APPROVED_STATUS.equals(business.status())
                ? business.approveAt()
                : business.rejectedAt();
        String reviewedAtDisplay = reviewedAt == null
                ? "Time not recorded"
                : DATE_TIME_FORMATTER.format(reviewedAt);

        return new AuditLogEntryView(
                business.businessId(),
                business.name(),
                business.address(),
                business.status(),
                reviewedAt,
                reviewedAtDisplay
        );
    }
}
