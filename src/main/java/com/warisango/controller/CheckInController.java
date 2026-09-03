package com.warisango.controller;

import com.warisango.dto.CheckInRequest;
import com.warisango.dto.CheckInResponse;
import com.warisango.dto.RecentVisitDTO;
import com.warisango.service.CheckInService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CheckInController {
    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @GetMapping("/visits/recent")
    public ResponseEntity<List<RecentVisitDTO>> getRecentVisits(Authentication authentication) {
        return ResponseEntity.ok(checkInService.findRecentVisits(authentication.getName()));
    }

    @PostMapping("/checkin")
    public ResponseEntity<CheckInResponse> checkIn(
            @Valid @RequestBody CheckInRequest request,
            Authentication authentication) {

        CheckInResponse response = checkInService.processCheckIn(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/checkin/status")
    public ResponseEntity<Map<String, Boolean>> getCheckInStatus(
            @RequestParam String businessId,
            Authentication authentication) {
        try {
            boolean checkedInToday = checkInService.hasCheckedInToday(authentication.getName(), businessId);
            return ResponseEntity.ok(Map.of("checkedInToday", checkedInToday));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().build();
        } catch (Exception exception) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
