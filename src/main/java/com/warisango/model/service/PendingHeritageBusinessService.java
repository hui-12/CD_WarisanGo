package com.warisango.model.service;

import com.warisango.dto.HeritageBusinessView;
import com.warisango.exception.BusinessNotFoundException;
import com.warisango.model.HeritageBusiness;
import com.warisango.model.repository.HeritageBusinessRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class PendingHeritageBusinessService {

    private static final String PENDING_STATUS = "Pending";

    private final HeritageBusinessRepository heritageBusinessRepository;

    public PendingHeritageBusinessService(
            HeritageBusinessRepository heritageBusinessRepository) {

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
        HeritageBusiness business = heritageBusinessRepository.findById(businessId)
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

    private void requirePending(String businessId) {
        heritageBusinessRepository.findById(businessId)
                .filter(item -> PENDING_STATUS.equals(item.status()))
                .orElseThrow(() -> new BusinessNotFoundException(businessId));
    }

    private HeritageBusinessView toView(HeritageBusiness business) {
        return new HeritageBusinessView(
                business.businessId(),
                business.name(),
                business.address(),
                business.city(),
                business.description(),
                business.latitude(),
                business.longitude(),
                business.sourceVideoLink(),
                business.status(),
                business.averageRating(),
                business.createdAt(),
                business.approveAt(),
                business.rejectAt()
        );
    }
}
