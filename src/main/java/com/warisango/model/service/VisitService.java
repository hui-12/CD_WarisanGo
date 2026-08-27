package com.warisango.model.service;

import com.warisango.dto.HeritageBusinessDTO;
import com.warisango.dto.VisitDTO;
import com.warisango.model.CheckIn;
import com.warisango.model.repository.CheckInRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VisitService {
    private static final ZoneId MALAYSIA_TIME = ZoneId.of("Asia/Kuala_Lumpur");

    private final CheckInRepository checkInRepository;
    private final BusinessService businessService;

    public VisitService(CheckInRepository checkInRepository, BusinessService businessService) {
        this.checkInRepository = checkInRepository;
        this.businessService = businessService;
    }

    public List<VisitDTO> getVisits(String touristId) {
        try {
            Map<String, HeritageBusinessDTO> businesses = businessService.getApprovedBusinesses().stream()
                    .collect(Collectors.toMap(HeritageBusinessDTO::getBusinessId, Function.identity()));
            return checkInRepository.findByTouristId(touristId).stream()
                    .map(checkIn -> toVisit(checkIn, businesses.get(checkIn.getBusinessId())))
                    .toList();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load visit history.", exception);
        }
    }

    public List<VisitDTO> getThisWeeksVisits(String touristId) {
        LocalDate today = LocalDate.now(MALAYSIA_TIME);
        LocalDate weekStart = today.minusDays(7);
        Instant start = weekStart.atStartOfDay(MALAYSIA_TIME).toInstant();
        return getVisits(touristId).stream()
                .filter(visit -> !visit.visitedAt().isBefore(start))
                .toList();
    }

    public List<String> getVisitedBusinessIds(String touristId) {
        return getVisits(touristId).stream().map(VisitDTO::businessId).distinct().toList();
    }

    private VisitDTO toVisit(CheckIn checkIn, HeritageBusinessDTO business) {
        String businessName = business == null ? "Unknown heritage business" : business.getName();
        String image = business == null || business.getImageUrls().isEmpty()
                ? null : business.getImageUrls().get(0);
        return new VisitDTO(checkIn.getCheckInId(), checkIn.getBusinessId(), businessName, image,
                checkIn.getCheckInTimestamp().toDate().toInstant(), checkIn.getPointsAwarded());
    }
}
