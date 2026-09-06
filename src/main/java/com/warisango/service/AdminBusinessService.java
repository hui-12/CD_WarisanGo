package com.warisango.service;

import com.warisango.dto.AdminBusinessView;
import com.warisango.dto.HeritageBusinessUpdateRequest;
import com.warisango.exception.BusinessNotFoundException;
import com.warisango.exception.OperationConflictException;
import com.warisango.model.HeritageBusiness;
import com.warisango.repository.AdminRepository;
import com.warisango.repository.BusinessRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class AdminBusinessService {
    private final BusinessRepository businessRepository;
    private final BusinessPhotoService photoService;
    private final AdminRepository adminRepository;

    public AdminBusinessService(BusinessRepository businessRepository, BusinessPhotoService photoService,
                                AdminRepository adminRepository) {
        this.businessRepository = businessRepository;
        this.photoService = photoService;
        this.adminRepository = adminRepository;
    }

    public AdminBusinessView get(String businessId, String adminUserId) {
        requireAdmin(adminUserId);
        HeritageBusiness business = requireBusiness(businessId);
        return new AdminBusinessView(businessId, business.name(), business.status(),
                toRequest(business), photoService.getPhotos(businessId));
    }

    public void update(String businessId, HeritageBusinessUpdateRequest request, String adminUserId) {
        requireAdmin(adminUserId);
        HeritageBusiness existing = requireBusiness(businessId);
        businessRepository.updateBusiness(businessId, new HeritageBusiness(
                businessId, request.getName(), request.getAddress(), request.getState(), request.getCity(),
                request.getDescription(), request.getLatitude(), request.getLongitude(), request.getOperatingHour(),
                request.getSourceVideoLink(), existing.status(), request.getAverageRating(), request.getCheckInPoints(),
                existing.createdAt(), existing.approveAt(), existing.rejectedAt()));
    }

    public void setActive(String businessId, boolean active, String adminUserId) {
        requireAdmin(adminUserId);
        HeritageBusiness business = requireBusiness(businessId);
        if (!"Approved".equals(business.status()) && !"Inactive".equals(business.status())) {
            throw new OperationConflictException(
                    "Only approved businesses can be activated or deactivated. Rejected businesses cannot be reactivated.");
        }
        if (active == "Approved".equals(business.status())) {
            return;
        }
        if (active) {
            businessRepository.approve(businessId);
        } else {
            businessRepository.deactivate(businessId);
        }
    }

    private HeritageBusiness requireBusiness(String businessId) {
        return businessRepository.findHeritageBusinessById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException(businessId));
    }

    private HeritageBusinessUpdateRequest toRequest(HeritageBusiness business) {
        HeritageBusinessUpdateRequest request = new HeritageBusinessUpdateRequest();
        request.setName(business.name());
        request.setAddress(business.address());
        request.setState(business.state());
        request.setCity(business.city());
        request.setDescription(business.description());
        request.setLatitude(business.latitude());
        request.setLongitude(business.longitude());
        request.setOperatingHour(business.operatingHour());
        request.setAverageRating(business.averageRating());
        request.setCheckInPoints(business.checkInPoints());
        request.setSourceVideoLink(business.sourceVideoLink());
        return request;
    }

    private void requireAdmin(String userId) {
        if (userId == null || !adminRepository.existsByUserId(userId)) {
            throw new AccessDeniedException("Admin access is required.");
        }
    }
}
