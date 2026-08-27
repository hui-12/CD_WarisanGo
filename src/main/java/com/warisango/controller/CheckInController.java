package com.warisango.controller;

import com.warisango.dto.CheckInRequest;
import com.warisango.dto.CheckInResponse;
import com.warisango.dto.UserPointsDTO;
import com.warisango.model.service.CheckInService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CheckInController {
    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @GetMapping("/user/points")
    public ResponseEntity<UserPointsDTO> getPoints(Authentication authentication) {
        String userId = authentication.getName();
        try {
            int points = checkInService.getCurrentPoints(userId);
            return ResponseEntity.ok(new UserPointsDTO(userId, points));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/checkin")
    public ResponseEntity<CheckInResponse> checkIn(
            @RequestBody CheckInRequest request,
            Authentication authentication) {

        request.setUserId(authentication.getName());
        CheckInResponse response = checkInService.processCheckIn(request);
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
