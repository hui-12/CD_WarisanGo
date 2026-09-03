package com.warisango.util;

import com.google.cloud.Timestamp;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReviewDateFormatterTest {

    @Test
    void formatsFirestoreTimestampInMalaysiaTime() {
        Timestamp timestamp = Timestamp.ofTimeSecondsAndNanos(1_787_018_655L, 36_000_000);

        assertEquals(ReviewDateFormatter.format(timestamp), ReviewDateFormatter.format(timestamp.toDate().toInstant()));
    }

    @Test
    void formatsInstantInMalaysiaTime() {
        assertEquals("18 Aug 2026, 10:04 AM",
                ReviewDateFormatter.format(Instant.parse("2026-08-18T02:04:15.036Z")));
    }

    @Test
    void formatsFirestoreUtcTimestampInMalaysiaTime() {
        assertEquals(
                "18 Aug 2026, 10:04 AM",
                ReviewDateFormatter.format("2026-08-18T02:04:15.036000000Z")
        );
    }

    @Test
    void keepsUnknownTimestampTextInsteadOfDiscardingIt() {
        assertEquals(
                "not-a-timestamp",
                ReviewDateFormatter.format("not-a-timestamp")
        );
    }
}
