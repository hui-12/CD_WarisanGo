package com.warisango.service;

import com.warisango.dto.HeritageBusinessDTO;
import com.warisango.model.SavedListing;
import com.warisango.model.repository.SavedListingRepository;
import com.warisango.model.service.BusinessService;
import com.warisango.model.service.SavedListingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SavedListingServiceTests {

    @Mock
    private SavedListingRepository savedListingRepository;

    @Mock
    private BusinessService businessService;

    @InjectMocks
    private SavedListingService savedListingService;

    @Test
    void saveCreatesOneRecordForAnApprovedBusiness() throws Exception {
        HeritageBusinessDTO business = business("hb_001");
        when(businessService.getBusinessById("hb_001")).thenReturn(Optional.of(business));
        when(savedListingRepository.exists("tourist_001", "hb_001")).thenReturn(false);

        savedListingService.save("tourist_001", "hb_001");

        verify(savedListingRepository).save("tourist_001", "hb_001");
    }

    @Test
    void saveRejectsBusinessesThatAreNotApprovedOrDoNotExist() throws Exception {
        when(businessService.getBusinessById("hb_missing")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> savedListingService.save("tourist_001", "hb_missing"));

        verify(savedListingRepository, never()).save("tourist_001", "hb_missing");
    }

    @Test
    void getSavedBusinessesOnlyReturnsApprovedSavedEntries() throws Exception {
        SavedListing savedListing = new SavedListing();
        savedListing.setBusinessId("hb_001");
        when(savedListingRepository.findByTouristId("tourist_001")).thenReturn(List.of(savedListing));
        when(businessService.getApprovedBusinesses()).thenReturn(List.of(business("hb_001"), business("hb_002")));

        List<HeritageBusinessDTO> result = savedListingService.getSavedBusinesses("tourist_001");

        assertEquals(List.of("hb_001"), result.stream().map(HeritageBusinessDTO::getBusinessId).toList());
    }

    private HeritageBusinessDTO business(String businessId) {
        HeritageBusinessDTO business = new HeritageBusinessDTO();
        business.setBusinessId(businessId);
        return business;
    }
}
