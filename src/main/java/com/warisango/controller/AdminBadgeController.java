package com.warisango.controller;

import com.warisango.dto.BadgeCreatedResponse;
import com.warisango.dto.BadgeDefinitionRequest;
import com.warisango.dto.SuccessResponse;
import com.warisango.service.BadgeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/badges")
public class AdminBadgeController {
    private final BadgeService badgeService;

    public AdminBadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() throws Exception {
        return ResponseEntity.ok(badgeService.adminList());
    }

    @PostMapping
    public ResponseEntity<BadgeCreatedResponse> create(
            @Valid @RequestBody BadgeDefinitionRequest request) throws Exception {
        return ResponseEntity.ok(new BadgeCreatedResponse(badgeService.create(request.toMap())));
    }

    @PutMapping("/{badgeId}")
    public ResponseEntity<SuccessResponse> update(
            @PathVariable String badgeId,
            @Valid @RequestBody BadgeDefinitionRequest request) throws Exception {
        badgeService.update(badgeId, request.toMap());
        return ResponseEntity.ok(new SuccessResponse(true));
    }

    @DeleteMapping("/{badgeId}")
    public ResponseEntity<SuccessResponse> delete(@PathVariable String badgeId) throws Exception {
        badgeService.delete(badgeId);
        return ResponseEntity.ok(new SuccessResponse(true));
    }
}
