package com.warisango.service;

import com.warisango.dto.HeritageBusinessDTO;
import com.warisango.exception.SavedListingException;
import com.warisango.model.SavedListing;
import com.warisango.repository.SavedListingRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class SavedListingService {
    private final SavedListingRepository savedListingRepository;
    private final BusinessService businessService;

    public SavedListingService(SavedListingRepository savedListingRepository,
                               BusinessService businessService) {
        this.savedListingRepository = savedListingRepository;
        this.businessService = businessService;
    }

    public List<HeritageBusinessDTO> getSavedBusinesses(String touristId) {
        try {
            Set<String> savedIds = getSavedBusinessIds(touristId);
            return businessService.getApprovedBusinesses().stream()
                    .filter(business -> savedIds.contains(business.getBusinessId()))
                    .toList();
        } catch (Exception exception) {
            throw new SavedListingException("Unable to load saved listings.", exception);
        }
    }

    public Set<String> getSavedBusinessIds(String touristId) {
        try {
            return savedListingRepository.findByTouristId(touristId).stream()
                    .map(SavedListing::getBusinessId)
                    .collect(LinkedHashSet::new, Set::add, Set::addAll);
        } catch (Exception exception) {
            throw new SavedListingException("Unable to load saved listing IDs.", exception);
        }
    }

    public boolean isSaved(String touristId, String businessId) {
        try {
            return savedListingRepository.exists(touristId, businessId);
        } catch (Exception exception) {
            throw new SavedListingException("Unable to load saved listing status.", exception);
        }
    }

    public void save(String touristId, String businessId) {
        if (businessService.getBusinessById(businessId).isEmpty()) {
            throw new IllegalArgumentException("Only approved heritage businesses can be saved.");
        }
        try {
            if (!savedListingRepository.exists(touristId, businessId)) {
                savedListingRepository.save(touristId, businessId);
            }
        } catch (Exception exception) {
            throw new SavedListingException("Unable to save the heritage business.", exception);
        }
    }

    public void remove(String touristId, String businessId) {
        try {
            savedListingRepository.delete(touristId, businessId);
        } catch (Exception exception) {
            throw new SavedListingException("Unable to remove the saved heritage business.", exception);
        }
    }
}
