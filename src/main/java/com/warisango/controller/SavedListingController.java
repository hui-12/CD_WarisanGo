package com.warisango.controller;

import com.warisango.dto.HeritageBusinessDTO;
import com.warisango.service.SavedListingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/saved-listings")
public class SavedListingController {
    private static final Logger logger = LoggerFactory.getLogger(SavedListingController.class);
    private final SavedListingService savedListingService;

    public SavedListingController(SavedListingService savedListingService) {
        this.savedListingService = savedListingService;
    }

    @GetMapping
    public ResponseEntity<List<HeritageBusinessDTO>> getSavedBusinesses(Authentication authentication) {
        return ResponseEntity.ok(savedListingService.getSavedBusinesses(authentication.getName()));
    }

    @GetMapping("/ids")
    public ResponseEntity<Set<String>> getSavedBusinessIds(Authentication authentication) {
        return ResponseEntity.ok(savedListingService.getSavedBusinessIds(authentication.getName()));
    }

    @GetMapping("/{businessId}/status")
    public ResponseEntity<Map<String, Boolean>> getStatus(@PathVariable String businessId,
                                                          Authentication authentication) {
        boolean saved = savedListingService.isSaved(authentication.getName(), businessId);
        return ResponseEntity.ok(Map.of("saved", saved));
    }

    @PostMapping("/{businessId}")
    public ResponseEntity<?> save(@PathVariable String businessId, Authentication authentication) {
        try {
            savedListingService.save(authentication.getName(), businessId);
            return ResponseEntity.ok(Map.of("saved", true));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (Exception exception) {
            logger.error("Unable to save business {} for {}.", businessId, authentication.getName(), exception);
            return ResponseEntity.internalServerError().body(Map.of("message", "Unable to save listing."));
        }
    }

    @DeleteMapping("/{businessId}")
    public ResponseEntity<?> remove(@PathVariable String businessId, Authentication authentication) {
        try {
            savedListingService.remove(authentication.getName(), businessId);
            return ResponseEntity.ok(Map.of("saved", false));
        } catch (Exception exception) {
            logger.error("Unable to remove business {} for {}.", businessId, authentication.getName(), exception);
            return ResponseEntity.internalServerError().body(Map.of("message", "Unable to remove listing."));
        }
    }
}
