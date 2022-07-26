package com.paragon.client.ui.util.animation

import net.minecraft.util.math.MathHelper
import java.util.function.Supplier

/**
 * @author Surge
 * @since 13/07/22
 */
class Animation(val length: Supplier<Float>, private val initialState: Boolean, val easing: Supplier<Easing>) {

    // Time since last state update
    private var lastMillis: Long = 0L

    // Current state of the animation
    // True = Expanding / Expanded
    // False = Contracting / Collapsed
    var state: Boolean = initialState
        set(value) {
            lastMillis = if (!value) {
                System.currentTimeMillis() - ((1 - getLinearFactor()) * length.get().toLong()).toLong()
            } else {
                System.currentTimeMillis() - (getLinearFactor() * length.get().toLong()).toLong()
            }

            field = value
        }

    /**
     * Gets the animation factor (value between 0 and 1)
     *
     * @return The animation factor
     */
    fun getAnimationFactor(): Double = if (state) {
        easing.get().ease(MathHelper.clamp((System.currentTimeMillis() - lastMillis.toDouble()) / length.get().toDouble(), 0.0, 1.0))
    } else {
        easing.get().ease(MathHelper.clamp(1 - (System.currentTimeMillis() - lastMillis.toDouble()) / length.get().toDouble(), 0.0, 1.0))
    }

    /**
     * Resets the animation to the original constructor parameters
     */
    fun resetToDefault() {
        state = initialState

        lastMillis = if (initialState) {
            System.currentTimeMillis() - ((1 - getLinearFactor()) * length.get().toLong()).toLong()
        } else {
            System.currentTimeMillis() - (getLinearFactor() * length.get().toLong()).toLong()
        }
    }

    /**
     * For internal use only!
     */
    private fun getLinearFactor(): Double = if (!state) MathHelper.clamp(
        1 - (System.currentTimeMillis() - lastMillis.toDouble()) / length.get().toDouble(),
        0.0,
        1.0
    ) else MathHelper.clamp((System.currentTimeMillis() - lastMillis.toDouble()) / length.get().toDouble(), 0.0, 1.0)

}