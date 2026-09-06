package com.warisango.service;

import com.warisango.dto.CheckInRequest;
import com.warisango.dto.CheckInResponse;
import com.warisango.dto.RecentVisitDTO;
import com.warisango.repository.BusinessRepository;
import com.warisango.repository.CheckInRepository;
import com.warisango.model.CheckIn;
import com.warisango.model.HeritageBusiness;
import com.warisango.util.DistanceCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class CheckInService {
    private static final Logger logger = LoggerFactory.getLogger(CheckInService.class);
    private static final double MAX_DISTANCE_METERS = 50.0;
    private static final int RECENT_VISIT_LIMIT = 5;
    private static final ZoneId CHECK_IN_TIME_ZONE = ZoneId.of("Asia/Kuala_Lumpur");
    private static final DateTimeFormatter VISIT_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("dd MMM yyyy").withZone(CHECK_IN_TIME_ZONE);
    private final BusinessRepository businessRepository;
    private final CheckInRepository checkInRepository;
    private final boolean enforceDistanceLimit;

    public CheckInService(
            BusinessRepository businessRepository,
            CheckInRepository checkInRepository,
            @Value("${warisango.check-in.enforce-distance-limit:true}") boolean enforceDistanceLimit) {
        this.businessRepository = businessRepository;
        this.checkInRepository = checkInRepository;
        this.enforceDistanceLimit = enforceDistanceLimit;
    }

    public List<RecentVisitDTO> findRecentVisits(String touristId) {
        if (touristId == null || touristId.isBlank()) {
            throw new IllegalArgumentException("Authenticated user ID is required.");
        }
        return checkInRepository.findByUserId(touristId).stream()
                .filter(record -> record != null && record.getCheckInTimestamp() != null)
                .sorted(Comparator.comparing(CheckIn::getCheckInTimestamp).reversed())
                .limit(RECENT_VISIT_LIMIT)
                .map(record -> new RecentVisitDTO(record.getBusinessName(), "",
                        VISIT_DATE_FORMATTER.format(record.getCheckInTimestamp().toDate().toInstant()),
                        record.getPointsAwarded()))
                .toList();
    }

    public CheckInResponse processCheckIn(String userId, CheckInRequest request) {
        try {
            validateRequest(userId, request);
            HeritageBusiness business = businessRepository.findByBusinessId(request.getBusinessId());
            if (business == null) {
                return failure("This approved business could not be found.", 0, 0);
            }
            double distance = DistanceCalculator.distanceMeters(
                    request.getUserLatitude(), request.getUserLongitude(),
                    business.latitude() == null ? 0.0 : business.latitude(),
                    business.longitude() == null ? 0.0 : business.longitude());
            if (enforceDistanceLimit && distance > MAX_DISTANCE_METERS) {
                return failure("You must be within 50 metres to check in.",
                        checkInRepository.getCurrentPoints(userId), distance);
            }
            if (hasCheckedInToday(userId, business.businessId())) {
                return failure("You have already checked in at this business today.",
                        checkInRepository.getCurrentPoints(userId), distance);
            }
            int points = business.checkInPoints() != null && business.checkInPoints() > 0
                    ? business.checkInPoints()
                    : 50;
            int updatedPoints = checkInRepository.saveAndAwardPoints(userId, business.businessId(),
                    business.name(), request.getUserLatitude(), request.getUserLongitude(), points);
            return new CheckInResponse(true, "Check-in successful.", points, updatedPoints, distance);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return failure("The check-in operation was interrupted.", 0, 0);
        } catch (Exception exception) {
            logger.error("Unable to process check-in.", exception);
            return failure("Unable to complete check-in. Please try again.", 0,
                    0);
        }
    }

    public int getCurrentPoints(String touristId) throws Exception {
        return checkInRepository.getCurrentPoints(touristId);
    }

    public boolean hasCheckedInToday(String touristId, String businessId) throws Exception {
        if (touristId == null || touristId.isBlank() || businessId == null || businessId.isBlank()) return false;
        return checkInRepository.hasCheckedInToday(touristId, businessId, CHECK_IN_TIME_ZONE);
    }

    public Set<String> getCheckedInBusinessIdsToday(String touristId) throws Exception {
        if (touristId == null || touristId.isBlank()) {
            throw new IllegalArgumentException("Authenticated user ID is required.");
        }
        return checkInRepository.findCheckedInBusinessIdsToday(touristId, CHECK_IN_TIME_ZONE);
    }

    private void validateRequest(String userId, CheckInRequest request) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Authenticated user ID is required.");
        }
        if (request == null || request.getBusinessId() == null || request.getBusinessId().isBlank()) {
            throw new IllegalArgumentException("Business ID is required.");
        }
        if (request.getUserLatitude() == null || request.getUserLongitude() == null) {
            throw new IllegalArgumentException("Current GPS coordinates are required.");
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
