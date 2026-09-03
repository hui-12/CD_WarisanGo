package com.warisango.util;

public final class TierCalculator {

    public static final int SILVER_MINIMUM_POINTS = 500;
    public static final int GOLD_MINIMUM_POINTS = 1_000;
    public static final int PLATINUM_MINIMUM_POINTS = 2_000;

    private TierCalculator() {
    }

    public static String tierFor(int points) {
        if (points >= PLATINUM_MINIMUM_POINTS) {
            return "Platinum";
        }
        if (points >= GOLD_MINIMUM_POINTS) {
            return "Gold";
        }
        if (points >= SILVER_MINIMUM_POINTS) {
            return "Silver";
        }
        return "Bronze";
    }
}
