package com.warisango.service;

import com.warisango.dto.HeritageBusinessDTO;
import com.warisango.exception.FirebasePersistenceException;
import com.warisango.model.HeritageBusiness;
import com.warisango.repository.BusinessRepository;
import com.warisango.repository.HeritageBusinessImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
public class BusinessService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessService.class);
    private final BusinessRepository businessRepository;
    private final HeritageBusinessImageRepository heritageBusinessImageRepository;

    public BusinessService(BusinessRepository businessRepository,
                           HeritageBusinessImageRepository heritageBusinessImageRepository) {
        this.businessRepository = businessRepository;
        this.heritageBusinessImageRepository = heritageBusinessImageRepository;
    }

    public List<HeritageBusinessDTO> getApprovedBusinesses() {
        try {
            return attachImages(businessRepository.findApprovedBusinesses().stream().map(this::toDto).toList());
        } catch (Exception e) {
            logger.error("Error fetching approved heritage businesses", e);
            throw persistenceFailure("Unable to load approved heritage businesses.", e);
        }
    }

    public Optional<HeritageBusinessDTO> getBusinessById(String id) {
        try {
            Optional<HeritageBusinessDTO> business = businessRepository.findById(id).map(this::toDto);
            business.ifPresent(this::attachImages);
            return business;
        } catch (Exception e) {
            logger.error("Error fetching business by id: {}", id, e);
            throw persistenceFailure("Unable to load the heritage business.", e);
        }
    }

    public void updateAverageRating(String businessId, Double averageRating) {
        try {
            businessRepository.updateAverageRating(businessId, averageRating);
        } catch (Exception exception) {
            logger.error("Failed to update average rating for business {}.", businessId, exception);
            throw persistenceFailure("Failed to update the business rating.", exception);
        }
    }

    /**
     * Loads an approved business for the review pages, accepting either a Firestore document ID
     * or the businessId stored in the document.
     */
    public HeritageBusinessDTO getApprovedBusinessForReview(String businessId) {
        try {
            HeritageBusiness storedBusiness = businessRepository.findByBusinessId(businessId);
            HeritageBusinessDTO business = storedBusiness == null ? null : toDto(storedBusiness);
            if (business != null) {
                attachImages(business);
            }
            return business;
        } catch (Exception e) {
            logger.warn("Could not load heritage business {} for Review page.", businessId, e);
            throw persistenceFailure("Unable to load the heritage business for review.", e);
        }
    }

    public Runnable subscribeToApprovedBusinesses(
            Consumer<List<HeritageBusinessDTO>> listener) {
        return businessRepository.addApprovedBusinessesListener(
                businesses -> listener.accept(attachImages(businesses.stream().map(this::toDto).toList())));
    }

    /**
     * Image records are stored separately in Firestore so business data remains lightweight.
     * This method joins them with a directory or detail result before it reaches the controller.
     */
    private List<HeritageBusinessDTO> attachImages(List<HeritageBusinessDTO> businesses) {
        for (HeritageBusinessDTO business : businesses) {
            attachImages(business);
        }
        return businesses;
    }

    private void attachImages(HeritageBusinessDTO business) {
        List<String> imageUrls = heritageBusinessImageRepository.findImageUrlsByBusinessId(business.getBusinessId());
        business.setImageUrls(imageUrls);
    }

    private HeritageBusinessDTO toDto(HeritageBusiness business) {
        HeritageBusinessDTO dto = new HeritageBusinessDTO(
                business.businessId(),
                business.name(),
                business.address(),
                business.state(),
                business.city(),
                business.description(),
                business.latitude() == null ? 0.0 : business.latitude(),
                business.longitude() == null ? 0.0 : business.longitude(),
                business.averageRating(),
                business.checkInPoints() == null || business.checkInPoints() <= 0 ? 50 : business.checkInPoints()
        );
        dto.setOperatingHour(business.operatingHour());
        dto.setSourceVideoLink(business.sourceVideoLink());
        dto.setCreatedAt(business.createdAt());
        dto.setApproveAt(business.approveAt());
        dto.setRejectedAt(business.rejectedAt());
        return dto;
    }

    private FirebasePersistenceException persistenceFailure(String message, Exception cause) {
        return cause instanceof FirebasePersistenceException firebaseException
                ? firebaseException
                : new FirebasePersistenceException(message, cause);
    }
}
