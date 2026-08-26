package com.warisango.service;

import com.warisango.dto.RecentVisitDTO;
import com.warisango.model.CheckInRecord;
import com.warisango.model.repository.CheckInRepository;
import com.warisango.model.service.CheckInService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CheckInServiceTests {

    @Test
    void findRecentVisitsReturnsFiveNewestRecordsForUser() {
        CheckInRepository repository = mock(CheckInRepository.class);
        CheckInService service = new CheckInService(repository);
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

    private CheckInRecord record(String businessName, String timestamp) {
        return new CheckInRecord(businessName, 50, Instant.parse(timestamp));
    }
}
