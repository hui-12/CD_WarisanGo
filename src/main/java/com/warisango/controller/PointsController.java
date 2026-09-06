package com.warisango.controller;

import com.warisango.dto.LeaderboardEntryDTO;
import com.warisango.dto.PointHistoryDTO;
import com.warisango.dto.UserPointsDTO;
import com.warisango.service.PointsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/points")
public class PointsController {
    private static final Logger logger = LoggerFactory.getLogger(PointsController.class);
    private final PointsService pointsService;

    public PointsController(PointsService pointsService) {
        this.pointsService = pointsService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserPointsDTO> getCurrentPoints(Authentication authentication) throws Exception {
        String userId = authentication.getName();
        return ResponseEntity.ok(new UserPointsDTO(userId, pointsService.getCurrentPoints(userId)));
    }

    @GetMapping("/history")
    public ResponseEntity<List<PointHistoryDTO>> getHistory(Authentication authentication) {
        try {
            return ResponseEntity.ok(pointsService.getHistory(authentication.getName()));
        } catch (Exception exception) {
            logger.error("Unable to load point history for {}.", authentication.getName(), exception);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDTO>> getLeaderboard() {
        try {
            return ResponseEntity.ok(pointsService.getLeaderboard());
        } catch (Exception exception) {
            logger.error("Unable to load the leaderboard.", exception);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/leaderboard/me")
    public ResponseEntity<LeaderboardEntryDTO> getCurrentRank(Authentication authentication) {
        try {
            return pointsService.getLeaderboardEntry(authentication.getName())
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception exception) {
            logger.error("Unable to load leaderboard rank for {}.", authentication.getName(), exception);
            return ResponseEntity.internalServerError().build();
        }
    }
}
