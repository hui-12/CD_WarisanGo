package com.warisango.model.service;

import com.google.cloud.firestore.GeoPoint;
import com.warisango.dto.CheckInRequest;
import com.warisango.dto.CheckInResponse;
import com.warisango.dto.HeritageBusinessDTO;
import com.warisango.model.repository.BusinessRepository;
import com.warisango.model.repository.CheckInRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZoneId;

@Service
public class CheckInService {
    private static final Logger logger = LoggerFactory.getLogger(CheckInService.class);
    private static final double MAX_DISTANCE_METERS = 50.0;
    private static final boolean ENFORCE_DISTANCE_LIMIT = false;
    private static final ZoneId CHECK_IN_TIME_ZONE = ZoneId.of("Asia/Kuala_Lumpur");

    private final BusinessRepository businessRepository;
    private final CheckInRepository checkInRepository;

    public CheckInService(BusinessRepository businessRepository, CheckInRepository checkInRepository) {
        this.businessRepository = businessRepository;
        this.checkInRepository = checkInRepository;
    }

    public CheckInResponse processCheckIn(CheckInRequest request) {
        try {
            validateRequest(request);
            HeritageBusinessDTO business = businessRepository.findByBusinessId(request.getBusinessId());
            if (business == null) {
                return failure("This approved business could not be found.", 0, request.getDistanceMeters());
            }

            double distance = GeoUtils.distanceMeters(
                    request.getUserLatitude(), request.getUserLongitude(),
                    business.getLatitude(), business.getLongitude());
            if (ENFORCE_DISTANCE_LIMIT && distance > MAX_DISTANCE_METERS) {
                int currentPoints = checkInRepository.getCurrentPoints(request.getUserId());
                return failure("You must be within 50 metres to check in.", currentPoints, distance);
            }

            if (hasCheckedInToday(request.getUserId(), business.getBusinessId())) {
                int currentPoints = checkInRepository.getCurrentPoints(request.getUserId());
                return failure("You have already checked in at this business today.", currentPoints, distance);
            }

            int points = business.getCheckInPoints() > 0 ? business.getCheckInPoints() : 50;
            GeoPoint userLocation = new GeoPoint(request.getUserLatitude(), request.getUserLongitude());
            int updatedPoints = checkInRepository.saveAndAwardPoints(
                    request.getUserId(), business.getBusinessId(), userLocation, points);
            return new CheckInResponse(true, "Check-in successful.", points, updatedPoints, distance);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return failure("The check-in operation was interrupted.", 0, request.getDistanceMeters());
        } catch (Exception exception) {
            String businessId = request == null ? null : request.getBusinessId();
            logger.error("Unable to process check-in for business {}.", businessId, exception);
            return failure("Unable to complete check-in. Please try again.", 0,
                    request == null ? 0 : request.getDistanceMeters());
        }
    }

    public int getCurrentPoints(String touristId) throws Exception {
        return checkInRepository.getCurrentPoints(touristId);
    }

    public boolean hasCheckedInToday(String touristId, String businessId) throws Exception {
        if (touristId == null || touristId.isBlank() || businessId == null || businessId.isBlank()) {
            return false;
        }
        return checkInRepository.hasCheckedInToday(touristId, businessId, CHECK_IN_TIME_ZONE);
    }

    private void validateRequest(CheckInRequest request) {
        if (request == null || request.getUserId() == null || request.getUserId().isBlank()) {
            throw new IllegalArgumentException("Authenticated user ID is required.");
        }
        if (request.getBusinessId() == null || request.getBusinessId().isBlank()) {
            throw new IllegalArgumentException("Business ID is required.");
        }
        if (request.getUserLatitude() < -90 || request.getUserLatitude() > 90
                || request.getUserLongitude() < -180 || request.getUserLongitude() > 180) {
            throw new IllegalArgumentException("Invalid GPS coordinates.");
        }
    }

    private CheckInResponse failure(String message, int currentPoints, double distanceMeters) {
        return new CheckInResponse(false, message, 0, currentPoints, distanceMeters);
    }
}
