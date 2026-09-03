package com.warisango.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TierCalculatorTest {

    @ParameterizedTest
    @CsvSource({
            "-1, Bronze",
            "0, Bronze",
            "499, Bronze",
            "500, Silver",
            "999, Silver",
            "1000, Gold",
            "1999, Gold",
            "2000, Platinum"
    })
    void calculatesTierAtEachBoundary(int points, String expectedTier) {
        assertEquals(expectedTier, TierCalculator.tierFor(points));
    }
}
