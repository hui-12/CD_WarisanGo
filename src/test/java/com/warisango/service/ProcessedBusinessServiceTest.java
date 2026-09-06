package com.warisango.service;

import com.warisango.dto.ProcessedBusinessEntryView;
import com.warisango.model.HeritageBusiness;
import com.warisango.repository.BusinessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessedBusinessServiceTest {

    @Mock
    private BusinessRepository heritageBusinessRepository;
    @Mock
    private BusinessPhotoService businessPhotoService;

    private ProcessedBusinessService processedBusinessService;

    @BeforeEach
    void setUp() {
        processedBusinessService = new ProcessedBusinessService(heritageBusinessRepository, businessPhotoService);
    }

    @Test
    void findAllReturnsReviewedBusinessesNewestFirst() {
        HeritageBusiness approved = business(
                "hb_001",
                "Approved Business",
                "Approved",
                Instant.parse("2026-08-19T10:00:00Z"),
                null
        );
        HeritageBusiness rejected = business(
                "hb_002",
                "Rejected Business",
                "Rejected",
                null,
                Instant.parse("2026-08-20T10:00:00Z")
        );
        when(heritageBusinessRepository.findByStatuses(
                List.of("Approved", "Rejected", "Inactive")
        )).thenReturn(List.of(approved, rejected));

        List<ProcessedBusinessEntryView> result = processedBusinessService.findAll();

        assertEquals(List.of("hb_002", "hb_001"), result.stream()
                .map(ProcessedBusinessEntryView::businessId)
                .toList());
        assertEquals("Rejected", result.getFirst().status());
        verify(heritageBusinessRepository).findByStatuses(
                List.of("Approved", "Rejected", "Inactive")
        );
    }

    @Test
    void findAllKeepsOlderRecordsWithoutDecisionTime() {
        HeritageBusiness olderRecord = business(
                "hb_001",
                "Older Business",
                "Rejected",
                null,
                null
        );
        when(heritageBusinessRepository.findByStatuses(
                List.of("Approved", "Rejected", "Inactive")
        )).thenReturn(List.of(olderRecord));

        List<ProcessedBusinessEntryView> result = processedBusinessService.findAll();

        assertEquals("Time not recorded", result.getFirst().reviewedAtDisplay());
    }

    private HeritageBusiness business(
            String businessId,
            String name,
            String status,
            Instant approveAt,
            Instant rejectAt) {

        return new HeritageBusiness(
                businessId,
                name,
                "Address",
                "Kuala Lumpur",
                "City",
                "Description",
                3.139,
                101.687,
                "08:00 - 23:00",
                "https://www.youtube.com/watch?v=example",
                status,
                null,
                50,
                Instant.parse("2026-08-18T10:00:00Z"),
                approveAt,
                rejectAt
        );
    }
}
