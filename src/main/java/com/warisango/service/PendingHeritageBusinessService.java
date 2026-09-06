package com.warisango.service;

import com.warisango.dto.HeritageBusinessView;
import com.warisango.dto.HeritageBusinessUpdateRequest;
import com.warisango.exception.BusinessNotFoundException;
import com.warisango.model.HeritageBusiness;
import com.warisango.repository.BusinessRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class PendingHeritageBusinessService {

    private static final String PENDING_STATUS = "Pending";

    private final BusinessRepository heritageBusinessRepository;

    public PendingHeritageBusinessService(
            BusinessRepository heritageBusinessRepository) {

        this.heritageBusinessRepository = heritageBusinessRepository;
    }

    public List<HeritageBusinessView> findAll() {
        return heritageBusinessRepository.findByStatus(PENDING_STATUS)
                .stream()
                .sorted(Comparator.comparing(
                        HeritageBusiness::createdAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .map(this::toView)
                .toList();
    }

    public HeritageBusinessView findById(String businessId) {
        HeritageBusiness business = heritageBusinessRepository.findHeritageBusinessById(businessId)
                .filter(item -> PENDING_STATUS.equals(item.status()))
                .orElseThrow(() -> new BusinessNotFoundException(businessId));

        return toView(business);
    }

    public void approve(String businessId) {
        requirePending(businessId);
        heritageBusinessRepository.approve(businessId);
    }

    public void reject(String businessId) {
        requirePending(businessId);
        heritageBusinessRepository.reject(businessId);
    }

    public void update(String businessId, HeritageBusinessUpdateRequest request) {
        requirePending(businessId);
        heritageBusinessRepository.updateBusiness(businessId, new HeritageBusiness(
                businessId,
                request.getName(),
                request.getAddress(),
                request.getState(),
                request.getCity(),
                request.getDescription(),
                request.getLatitude(),
                request.getLongitude(),
                request.getOperatingHour(),
                request.getSourceVideoLink(),
                null,
                request.getAverageRating(),
                request.getCheckInPoints(),
                null,
                null,
                null
        ));
    }

    public HeritageBusinessUpdateRequest createUpdateRequest(HeritageBusinessView business) {
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

    private void requirePending(String businessId) {
        heritageBusinessRepository.findHeritageBusinessById(businessId)
                .filter(item -> PENDING_STATUS.equals(item.status()))
                .orElseThrow(() -> new BusinessNotFoundException(businessId));
    }

    private HeritageBusinessView toView(HeritageBusiness business) {
        return new HeritageBusinessView(
                business.businessId(),
                business.name(),
                business.address(),
                business.state(),
                business.city(),
                business.description(),
                business.latitude(),
                business.longitude(),
                business.operatingHour(),
                business.sourceVideoLink(),
                business.status(),
                business.averageRating(),
                business.checkInPoints(),
                business.createdAt(),
                business.approveAt(),
                business.rejectedAt()
        );
    }
}
