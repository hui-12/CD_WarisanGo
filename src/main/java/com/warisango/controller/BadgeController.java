package com.warisango.controller;

import com.warisango.dto.BadgeDTO;
import com.warisango.dto.BadgeSummaryDTO;
import com.warisango.model.service.BadgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
public class BadgeController {
    private static final Logger logger = LoggerFactory.getLogger(BadgeController.class);
    private final BadgeService badgeService;

    public BadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    @GetMapping
    public ResponseEntity<List<BadgeDTO>> getBadges(Authentication authentication) {
        try {
            return ResponseEntity.ok(badgeService.getBadges(authentication.getName()));
        } catch (Exception exception) {
            logger.error("Unable to load badges for {}.", authentication.getName(), exception);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<BadgeSummaryDTO> getSummary(Authentication authentication) {
        try {
            return ResponseEntity.ok(badgeService.getSummary(authentication.getName()));
        } catch (Exception exception) {
            logger.error("Unable to load badge summary for {}.", authentication.getName(), exception);
            return ResponseEntity.internalServerError().build();
        }
    }
}
