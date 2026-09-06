package com.warisango.service;

import com.warisango.model.BusinessPhoto;
import com.warisango.model.BusinessPhotoReport;
import com.warisango.model.HeritageBusiness;
import com.warisango.repository.AdminRepository;
import com.warisango.repository.BusinessPhotoReportRepository;
import com.warisango.repository.BusinessRepository;
import com.warisango.repository.HeritageBusinessImageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessPhotoServiceTest {
    @Mock private HeritageBusinessImageRepository photoRepository;
    @Mock private BusinessPhotoReportRepository reportRepository;
    @Mock private BusinessRepository businessRepository;
    @Mock private ReviewPhotoStorageService storageService;
    @Mock private UserService userService;
    @Mock private AdminRepository adminRepository;
    @Mock private MultipartFile photoFile;
    @InjectMocks private BusinessPhotoService photoService;

    @Test
    void uploadStoresPhotoWithAuthenticatedUploader() {
        when(businessRepository.findById("hb_001")).thenReturn(Optional.of(business()));
        when(photoFile.isEmpty()).thenReturn(false);
        when(storageService.storeBusinessPhoto("hb_001", photoFile))
                .thenReturn(new ReviewPhotoStorageService.StoredPhoto("business-photos/hb_001/photo.jpg", "url"));

        photoService.upload("hb_001", "tourist_001", new MultipartFile[]{photoFile});

        verify(storageService).validateFiles(anyList());
        verify(photoRepository).save(
                "hb_001", "url", "business-photos/hb_001/photo.jpg", "tourist_001");
    }

    @Test
    void duplicatePendingPhotoReportIsRejected() {
        when(photoRepository.findById("photo_001")).thenReturn(photo());
        when(reportRepository.hasPendingReport("photo_001", "tourist_001")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> photoService.report("photo_001", "tourist_001", "SPAM", null));
    }

    @Test
    void adminTakedownHidesPhotoAndResolvesReport() {
        BusinessPhotoReport report = new BusinessPhotoReport(
                "report_001", "photo_001", "hb_001", "tourist_002", "SPAM", null,
                "PENDING", null, null, null);
        when(adminRepository.existsByUserId("admin_001")).thenReturn(true);
        when(reportRepository.findById("report_001")).thenReturn(report);
        when(photoRepository.findById("photo_001")).thenReturn(photo());

        photoService.takeDown("report_001", "admin_001");

        verify(photoRepository).markRemoved("photo_001");
        verify(reportRepository).resolve("report_001", "REMOVED", "admin_001");
        verify(storageService).delete("url", "business-photos/hb_001/photo.jpg");
    }

    private BusinessPhoto photo() {
        return new BusinessPhoto("photo_001", "hb_001", "url", "business-photos/hb_001/photo.jpg",
                "tourist_001", null, 1, "ACTIVE");
    }

    private HeritageBusiness business() {
        return new HeritageBusiness("hb_001", "Business", null, null, null, null,
                null, null, null, null, "Approved", null, 50, null, null, null);
    }
}
