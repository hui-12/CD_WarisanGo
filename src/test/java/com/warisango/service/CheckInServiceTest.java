package com.warisango.service;

import com.warisango.dto.RecentVisitDTO;
import com.warisango.model.CheckIn;
import com.google.cloud.Timestamp;
import com.warisango.repository.BusinessRepository;
import com.warisango.repository.CheckInRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CheckInServiceTest {

    @Test
    void getCheckedInBusinessIdsTodayReturnsRepositoryResult() throws Exception {
        CheckInRepository repository = mock(CheckInRepository.class);
        BusinessRepository businessRepository = mock(BusinessRepository.class);
        CheckInService service = new CheckInService(businessRepository, repository, false);
        String userId = "user-123";
        Set<String> checkedInBusinessIds = Set.of("business-1", "business-2");

        when(repository.findCheckedInBusinessIdsToday(userId, java.time.ZoneId.of("Asia/Kuala_Lumpur")))
                .thenReturn(checkedInBusinessIds);

        assertEquals(checkedInBusinessIds, service.getCheckedInBusinessIdsToday(userId));
    }

    @Test
    void findRecentVisitsReturnsFiveNewestRecordsForUser() {
        CheckInRepository repository = mock(CheckInRepository.class);
        BusinessRepository businessRepository = mock(BusinessRepository.class);
        CheckInService service = new CheckInService(businessRepository, repository, false);
        String userId = "user-123";

        when(repository.findByUserId(userId)).thenReturn(List.of(
                record("Oldest", "2026-08-01T10:00:00Z"),
                record("Second", "2026-08-02T10:00:00Z"),
                record("Third", "2026-08-03T10:00:00Z"),
                record("Fourth", "2026-08-04T10:00:00Z"),
                record("Fifth", "2026-08-05T10:00:00Z"),
                record("Newest", "2026-08-06T10:00:00Z")));

        List<RecentVisitDTO> visits = service.findRecentVisits(userId);

        assertEquals(5, visits.size());
        assertEquals("Newest", visits.getFirst().businessName());
        assertEquals("Second", visits.getLast().businessName());
        assertEquals("06 Aug 2026", visits.getFirst().date());
        assertEquals(50, visits.getFirst().points());
    }

    private CheckIn record(String businessName, String timestamp) {
        CheckIn checkIn = new CheckIn();
        checkIn.setBusinessName(businessName);
        checkIn.setPointsAwarded(50);
        checkIn.setCheckInTimestamp(Timestamp.ofTimeSecondsAndNanos(Instant.parse(timestamp).getEpochSecond(), 0));
        return checkIn;
    }
}
