package com.warisango;

import com.warisango.util.ReviewDateFormatter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReviewDateFormatterTest {

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
