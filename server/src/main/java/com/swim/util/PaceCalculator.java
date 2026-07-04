package com.swim.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PaceCalculator {

    private PaceCalculator() {}

    public static BigDecimal calcPace(int distance, int duration) {
        if (distance <= 0) {
            return null;
        }
        return BigDecimal.valueOf(duration)
                .divide(BigDecimal.valueOf(distance / 100.0), 2, RoundingMode.HALF_UP);
    }

    public record Aggregate(int totalDistance, int totalDuration, BigDecimal avgPace) {}

    public static Aggregate aggregate(int totalDistance, int totalDuration) {
        return new Aggregate(totalDistance, totalDuration, calcPace(totalDistance, totalDuration));
    }
}
