package com.warisango.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DistanceCalculatorTest {

    @Test
    void returnsZeroForTheSameLocation() {
        assertEquals(0.0, DistanceCalculator.distanceMeters(3.1568, 101.7001, 3.1568, 101.7001));
    }

    @Test
    void calculatesDistanceBetweenKnownCoordinates() {
        double distance = DistanceCalculator.distanceMeters(3.1568, 101.7001, 3.1578, 101.7001);

        assertEquals(111.2, distance, 0.5);
    }

    @Test
    void rejectsCoordinatesOutsideValidRanges() {
        assertThrows(IllegalArgumentException.class,
                () -> DistanceCalculator.distanceMeters(91, 101.7001, 3.1568, 101.7001));
        assertThrows(IllegalArgumentException.class,
                () -> DistanceCalculator.distanceMeters(3.1568, Double.NaN, 3.1568, 101.7001));
    }
}
