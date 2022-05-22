package com.paragon.client.systems.ui.animation;

import net.minecraft.util.math.MathHelper;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Tigermouthbear, linustouchtips, Wolfsurge
 * @since 06/08/2021
 * Easings
 * @since 12/05/2022
 */
public class Animation {

    // animation time
    public float time;

    // animation current state
    private State currentState = State.STATIC;
    private long currentStateStart = 0;

    // animation previous state
    private State previousState = State.STATIC;
    private boolean initialState;

    // The easing to apply
    private final Supplier<Easing> easing;

    public Animation(int time, boolean initialState, Supplier<Easing> easing) {
        this.time = time;
        this.initialState = initialState;
        this.easing = easing;

        // start as expanded
        if (initialState) {
            previousState = State.EXPANDING;
        }
    }

    /**
     * Gets the animation length (0 to 1)
     *
     * @return The animation length (0 to 1)
     */
    public double getAnimationFactor() {
        if (currentState.equals(State.EXPANDING)) {
            return MathHelper.clamp(easing.get().ease((System.currentTimeMillis() - currentStateStart) / time), 0, 1);
        } else if (currentState.equals(State.RETRACTING)) {
            return MathHelper.clamp(easing.get().ease(1 - (System.currentTimeMillis() - currentStateStart) / time), 0, 1);
        }

        return previousState.equals(State.EXPANDING) ? 1 : 0;
    }

    /**
     * Gets the initial state
     *
     * @return The initial state
     */
    public boolean getState() {
        return initialState;
    }

    /**
     * Sets the state
     *
     * @param expand Expand or retract
     */
    public void setState(boolean expand) {
        if (expand) {
            currentState = State.EXPANDING;
            initialState = true;
        } else {
            currentState = State.RETRACTING;
        }

        // reset time
        currentStateStart = System.currentTimeMillis();
    }

    public enum State {

        /**
         * Expands the animation
         */
        EXPANDING,

        /**
         * Retracts the animation
         */
        RETRACTING,

        /**
         * No animation
         */
        STATIC
    }
}