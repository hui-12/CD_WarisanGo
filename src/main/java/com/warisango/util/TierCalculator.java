package com.warisango.util;

public final class TierCalculator {
    private TierCalculator() {
    }

    public static String tierFor(int points) {
        if (points >= 2000) return "Platinum";
        if (points >= 1000) return "Gold";
        if (points >= 500) return "Silver";
        return "Bronze";
    }
}
