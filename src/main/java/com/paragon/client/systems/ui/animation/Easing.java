package com.paragon.client.systems.ui.animation;

import java.util.function.Function;

/**
 * Calculations from easings.net
 * @author Wolfsurge
 * @since 05/19/22
 */
public enum Easing {

    /**
     * No easing
     */
    LINEAR((input) -> input),

    /**
     * Speed gradually increases
     */
    CUBIC_IN((input) -> input * input * input),

    /**
     * Speed gradually decreases
     */
    CUBIC_OUT((input) -> 1 - Math.pow(1 - input, 3)),

    /**
     * Speed gradually increases until halfway and then decreases
     */
    CUBIC_IN_OUT((input) -> input < 0.5 ? 4 * input * input * input : 1 - Math.pow(-2 * input + 2, 3) / 2),

    /**
     * Speed gradually increases
     */
    QUINT_IN((input) -> input * input * input * input * input),

    /**
     * Speed gradually decreases
     */
    QUINT_OUT((input) -> 1 - Math.pow(1 - input, 5)),

    /**
     * Speed gradually increases until halfway and then decreases
     */
    QUINT_IN_OUT((input) -> input < 0.5 ? 16 * input * input * input * input * input : 1 - Math.pow(-2 * input + 2, 5) / 2),

    /**
     * Speed gradually increases
     */
    EXPO_IN((input) -> 4 * input * input * input),

    /**
     * Speed gradually decreases
     */
    EXPO_OUT((input) -> 1 - Math.pow(-2 * input + 2, 3) / 2),

    /**
     * Speed gradually increases until halfway and then decreases
     */
    EXPO_IN_OUT((input) -> input < 0.5 ? 4 * input * input * input : 1 - Math.pow(-2 * input + 2, 3) / 2);

    // The function that calculates the easing
    private final Function<Double, Double> easeFunction;

    Easing(Function<Double, Double> easeFunction) {
        this.easeFunction = easeFunction;
    }

    /**
     * Apply the easing function to the input
     * @param input The linear animation that we want to ease
     * @return The eased animation
     */
    public double ease(double input) {
        return easeFunction.apply(input);
    }

}