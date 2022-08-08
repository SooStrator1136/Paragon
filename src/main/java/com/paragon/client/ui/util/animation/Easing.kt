package com.paragon.client.ui.util.animation

import kotlin.math.pow

/**
 * @author Surge
 */
@Suppress("unused")
enum class Easing(private val easeFunction: (Double) -> Double) {

    /**
     * No easing
     */
    LINEAR({ input -> input }),

    /**
     * Speed gradually increases
     */
    CUBIC_IN({ it * it * it }),

    /**
     * Speed gradually decreases
     */
    CUBIC_OUT({ 1 - (1 - it).pow(3.0) }),

    /**
     * Speed gradually increases until halfway and then decreases
     */
    CUBIC_IN_OUT({ if (it < 0.5) 4 * it * it * it else 1 - (-2 * it + 2).pow(3.0) / 2 }),

    /**
     * Speed gradually increases
     */
    QUINT_IN({ it * it * it * it * it }),

    /**
     * Speed gradually decreases
     */
    QUINT_OUT({ 1 - (1 - it).pow(5.0) }),

    /**
     * Speed gradually increases until halfway and then decreases
     */
    QUINT_IN_OUT({ if (it < 0.5) 16 * it * it * it * it * it else 1 - (-2 * it + 2).pow(5.0) / 2 }),

    /**
     * Speed gradually increases
     */
    EXPO_IN({ if (it == 0.0) 0.0 else 2.0.pow(10.0 * it - 10.0) }),

    /**
     * Speed gradually decreases
     */
    EXPO_OUT({ if (it == 1.0) 1.0 else 1 - 2.0.pow(-10 * it) }),

    /**
     * Speed gradually increases until halfway and then decreases
     */
    EXPO_IN_OUT({ if (it == 0.0) 0.0 else if (it == 1.0) 1.0 else if (it < 0.5) 2.0.pow(20 * it - 10) / 2.0 else (2 - 2.0.pow(-20 * it + 10)) / 2.0 });

    /**
     * Apply the easing function to the input
     *
     * @param input The linear animation that we want to ease
     * @return The eased animation
     */
    open fun ease(input: Double) = easeFunction.invoke(input)

}