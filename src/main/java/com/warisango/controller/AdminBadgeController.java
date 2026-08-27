package com.warisango.controller;

import com.warisango.model.service.BadgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/badges")
public class AdminBadgeController {
    private static final Logger logger = LoggerFactory.getLogger(AdminBadgeController.class);
    private final BadgeService badgeService;

    public AdminBadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    @GetMapping
    public ResponseEntity<?> list() {
        try {
            return ResponseEntity.ok(badgeService.adminList());
        } catch (Exception exception) {
            logger.error("Unable to load badges for administration.", exception);
            return ResponseEntity.internalServerError().body(Map.of("message", "Unable to load badges."));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(Map.of("badgeId", badgeService.create(body)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (Exception exception) {
            logger.error("Unable to create badge.", exception);
            return ResponseEntity.internalServerError().body(Map.of("message", "Unable to create badge."));
        }
    }

    @PutMapping("/{badgeId}")
    public ResponseEntity<?> update(@PathVariable String badgeId,
                                    @RequestBody Map<String, Object> body) {
        try {
            badgeService.update(badgeId, body);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (Exception exception) {
            logger.error("Unable to update badge {}.", badgeId, exception);
            return ResponseEntity.internalServerError().body(Map.of("message", "Unable to update badge."));
        }
    }

    @DeleteMapping("/{badgeId}")
    public ResponseEntity<?> delete(@PathVariable String badgeId) {
        try {
            badgeService.delete(badgeId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception exception) {
            logger.error("Unable to delete badge {}.", badgeId, exception);
            return ResponseEntity.internalServerError().body(Map.of("message", "Unable to delete badge."));
        }
    }
}
