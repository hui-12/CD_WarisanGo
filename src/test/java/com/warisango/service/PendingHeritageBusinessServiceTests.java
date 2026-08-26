package com.warisango.service;

import com.warisango.dto.HeritageBusinessView;
import com.warisango.exception.BusinessNotFoundException;
import com.warisango.model.HeritageBusiness;
import com.warisango.model.repository.HeritageBusinessRepository;
import com.warisango.model.service.PendingHeritageBusinessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PendingHeritageBusinessServiceTests {

    @Mock
    private HeritageBusinessRepository heritageBusinessRepository;

    private PendingHeritageBusinessService pendingHeritageBusinessService;

    @BeforeEach
    void setUp() {
        pendingHeritageBusinessService = new PendingHeritageBusinessService(
                heritageBusinessRepository
        );
    }

    @Test
    void findAllReturnsPendingBusinessesNewestFirst() {
        HeritageBusiness older = business(
                "hb_001",
                "Older Business",
                "Pending",
                Instant.parse("2026-08-19T10:00:00Z")
        );
        HeritageBusiness newer = business(
                "hb_002",
                "Newer Business",
                "Pending",
                Instant.parse("2026-08-20T10:00:00Z")
        );
        when(heritageBusinessRepository.findByStatus("Pending"))
                .thenReturn(List.of(older, newer));

        List<HeritageBusinessView> result = pendingHeritageBusinessService.findAll();

        assertEquals(List.of("hb_002", "hb_001"), result.stream()
                .map(HeritageBusinessView::businessId)
                .toList());
        verify(heritageBusinessRepository).findByStatus("Pending");
    }

    @Test
    void findByIdReturnsPendingBusiness() {
        HeritageBusiness business = business(
                "hb_001",
                "Pending Business",
                "Pending",
                Instant.parse("2026-08-20T10:00:00Z")
        );
        when(heritageBusinessRepository.findById("hb_001"))
                .thenReturn(Optional.of(business));

        HeritageBusinessView result = pendingHeritageBusinessService.findById("hb_001");

        assertEquals("hb_001", result.businessId());
        assertEquals("Pending Business", result.name());
    }

    @Test
    void findByIdRejectsBusinessThatIsNotPending() {
        HeritageBusiness business = business(
                "hb_001",
                "Approved Business",
                "Approved",
                Instant.parse("2026-08-20T10:00:00Z")
        );
        when(heritageBusinessRepository.findById("hb_001"))
                .thenReturn(Optional.of(business));

        assertThrows(
                BusinessNotFoundException.class,
                () -> pendingHeritageBusinessService.findById("hb_001")
        );
    }

    @Test
    void approveUpdatesPendingBusiness() {
        HeritageBusiness business = business(
                "hb_001",
                "Pending Business",
                "Pending",
                Instant.parse("2026-08-20T10:00:00Z")
        );
        when(heritageBusinessRepository.findById("hb_001"))
                .thenReturn(Optional.of(business));

        pendingHeritageBusinessService.approve("hb_001");

        verify(heritageBusinessRepository).approve("hb_001");
    }

    @Test
    void approveRejectsBusinessThatIsNotPending() {
        HeritageBusiness business = business(
                "hb_001",
                "Approved Business",
                "Approved",
                Instant.parse("2026-08-20T10:00:00Z")
        );
        when(heritageBusinessRepository.findById("hb_001"))
                .thenReturn(Optional.of(business));

        assertThrows(
                BusinessNotFoundException.class,
                () -> pendingHeritageBusinessService.approve("hb_001")
        );
        verify(heritageBusinessRepository, never()).approve("hb_001");
    }

    @Test
    void rejectUpdatesPendingBusiness() {
        HeritageBusiness business = business(
                "hb_001",
                "Pending Business",
                "Pending",
                Instant.parse("2026-08-20T10:00:00Z")
        );
        when(heritageBusinessRepository.findById("hb_001"))
                .thenReturn(Optional.of(business));

        pendingHeritageBusinessService.reject("hb_001");

        verify(heritageBusinessRepository).reject("hb_001");
    }

    @Test
    void rejectRejectsBusinessThatIsNotPending() {
        HeritageBusiness business = business(
                "hb_001",
                "Rejected Business",
                "Rejected",
                Instant.parse("2026-08-20T10:00:00Z")
        );
        when(heritageBusinessRepository.findById("hb_001"))
                .thenReturn(Optional.of(business));

        assertThrows(
                BusinessNotFoundException.class,
                () -> pendingHeritageBusinessService.reject("hb_001")
        );
        verify(heritageBusinessRepository, never()).reject("hb_001");
    }

    private HeritageBusiness business(
            String businessId,
            String name,
            String status,
            Instant createdAt) {

        return new HeritageBusiness(
                businessId,
                name,
                "Address",
                "City",
                "Description",
                3.139,
                101.687,
                "https://www.youtube.com/watch?v=example",
                status,
                null,
                createdAt,
                null,
                null
        );
    }
}
