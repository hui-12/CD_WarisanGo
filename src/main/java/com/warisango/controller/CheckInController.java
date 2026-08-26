package com.warisango.controller;

import com.warisango.dto.CheckInRequest;
import com.warisango.dto.CheckInResponse;
import com.warisango.dto.UserPointsDTO;
import com.warisango.model.repository.UserRepository;
import com.warisango.model.service.CheckInService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
public class CheckInController {
    private final CheckInService checkInService;
    private final UserRepository userRepository;

    public CheckInController(CheckInService checkInService, UserRepository userRepository) {
        this.checkInService = checkInService;
        this.userRepository = userRepository;
    }

    @GetMapping("/user/points")
    public ResponseEntity<UserPointsDTO> getPoints(Authentication authentication) {
        String userId = authentication.getName();
        try {
            int points = userRepository.getOrCreateCurrentPoints(userId);
            return ResponseEntity.ok(new UserPointsDTO(userId, points));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().build();
        } catch (ExecutionException e) {
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
}
