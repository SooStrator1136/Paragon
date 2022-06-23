package com.paragon.api.util.calculations;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathsUtil {

    /**
     * Rounds a value to the nearest place
     *
     * @param value The number to round
     * @param scale  The scale
     * @return The rounded number
     */
    public static double roundDouble(double value, int scale) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(scale, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static boolean isNearlyEqual(float value, float target, float threshold) {
        return value >= target - threshold && value <= target + threshold;
    }

    public static float getPercent(float value, float total) {
        return value * 100 / total;
    }

}
