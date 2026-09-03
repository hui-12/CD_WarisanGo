package com.warisango.controller;

import com.warisango.dto.ChallengeCreatedResponse;
import com.warisango.dto.ChallengeDefinitionRequest;
import com.warisango.dto.SuccessResponse;
import com.warisango.service.ChallengeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;

@RestController
public class ChallengeApiController {
    private final ChallengeService service;
    public ChallengeApiController(ChallengeService service) { this.service = service; }

    @GetMapping("/api/challenges")
    public ResponseEntity<List<Map<String, Object>>> list(Authentication authentication) throws Exception {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(service.getGuestChallenges());
        }
        return ResponseEntity.ok(service.getChallenges(authentication.getName()));
    }

    @PostMapping("/api/challenges/{id}/join")
    public ResponseEntity<SuccessResponse> join(
            @PathVariable String id,
            Authentication authentication) throws Exception {
        service.join(authentication.getName(), id);
        return ResponseEntity.ok(new SuccessResponse(true));
    }

    @PostMapping("/api/challenges/{id}/claim")
    public ResponseEntity<Map<String, Object>> claim(
            @PathVariable String id,
            Authentication authentication) throws Exception {
        return ResponseEntity.ok(service.claim(authentication.getName(), id));
    }

    @GetMapping("/api/admin/challenges")
    public ResponseEntity<List<Map<String, Object>>> adminList() throws Exception {
        return ResponseEntity.ok(service.adminList());
    }

    @PostMapping("/api/admin/challenges")
    public ResponseEntity<ChallengeCreatedResponse> create(
            @Valid @RequestBody ChallengeDefinitionRequest request) throws Exception {
        return ResponseEntity.ok(new ChallengeCreatedResponse(service.create(request.toMap())));
    }

    @PutMapping("/api/admin/challenges/{id}")
    public ResponseEntity<SuccessResponse> update(
            @PathVariable String id,
            @Valid @RequestBody ChallengeDefinitionRequest request) throws Exception {
        service.update(id, request.toMap());
        return ResponseEntity.ok(new SuccessResponse(true));
    }

    @DeleteMapping("/api/admin/challenges/{id}")
    public ResponseEntity<SuccessResponse> delete(@PathVariable String id) throws Exception {
        service.delete(id);
        return ResponseEntity.ok(new SuccessResponse(true));
    }
}
