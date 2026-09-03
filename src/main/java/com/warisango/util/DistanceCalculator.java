package com.warisango.util;

/**
 * Calculates geographical distances using the Haversine formula.
 */
public final class DistanceCalculator {

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private DistanceCalculator() {
    }

    public static double distanceMeters(double latitude1, double longitude1,
                                        double latitude2, double longitude2) {
        validateCoordinates(latitude1, longitude1);
        validateCoordinates(latitude2, longitude2);

        double latitude1Radians = Math.toRadians(latitude1);
        double latitude2Radians = Math.toRadians(latitude2);
        double latitudeDifference = Math.toRadians(latitude2 - latitude1);
        double longitudeDifference = Math.toRadians(longitude2 - longitude1);

        double haversine = Math.sin(latitudeDifference / 2) * Math.sin(latitudeDifference / 2)
                + Math.cos(latitude1Radians) * Math.cos(latitude2Radians)
                * Math.sin(longitudeDifference / 2) * Math.sin(longitudeDifference / 2);
        double boundedHaversine = Math.clamp(haversine, 0.0, 1.0);
        double angularDistance = 2 * Math.atan2(
                Math.sqrt(boundedHaversine),
                Math.sqrt(1 - boundedHaversine));

        return EARTH_RADIUS_METERS * angularDistance;
    }

    private static void validateCoordinates(double latitude, double longitude) {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90
                || !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Latitude or longitude is outside its valid range.");
        }
    }
}
