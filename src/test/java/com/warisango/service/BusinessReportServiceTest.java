package com.warisango.service;

import com.warisango.dto.HeritageBusinessDTO;
import com.warisango.model.BusinessReport;
import com.warisango.repository.AdminRepository;
import com.warisango.repository.BusinessReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessReportServiceTest {
    @Mock private BusinessReportRepository reportRepository;
    @Mock private BusinessService businessService;
    @Mock private UserService userService;
    @Mock private AdminRepository adminRepository;
    @InjectMocks private BusinessReportService businessReportService;

    @Test
    void submitCreatesPendingReportForApprovedBusiness() throws Exception {
        when(businessService.getBusinessById("hb_001")).thenReturn(Optional.of(new HeritageBusinessDTO()));

        businessReportService.submit("tourist_001", "hb_001", "outdated_info", "Hours changed.");

        verify(reportRepository).save("tourist_001", "hb_001", "OUTDATED_INFO", "Hours changed.");
    }

    @Test
    void submitRequiresExplanation() {
        assertThrows(IllegalArgumentException.class,
                () -> businessReportService.submit("tourist_001", "hb_001", "OTHER", " "));
    }

    @Test
    void resolveRecordsAdminOutcomeWithoutDuplicatingBusinessEditing() throws Exception {
        BusinessReport report = new BusinessReport();
        report.setReportId("business_report_001");
        report.setBusinessId("hb_001");
        report.setTouristId("tourist_001");
        report.setStatus("PENDING_REVIEW");
        when(adminRepository.existsByUserId("admin_uid")).thenReturn(true);
        when(adminRepository.findAdminIdByUserId("admin_uid")).thenReturn("admin_001");
        when(reportRepository.findById("business_report_001")).thenReturn(report);
        when(userService.getUserByUid("tourist_001")).thenReturn(Optional.empty());
        when(businessService.getBusinessById("hb_001")).thenReturn(Optional.of(new HeritageBusinessDTO()));
        businessReportService.resolveWithoutChanges("business_report_001", "admin_uid", "Hours verified.");

        verify(reportRepository).updateOutcome(
                "business_report_001", "RESOLVED", "admin_001", "Hours verified.");
    }
}
