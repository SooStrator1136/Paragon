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
    private Supplier<Easing> easing;

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
            return easing.get().apply(MathHelper.clamp((System.currentTimeMillis() - currentStateStart) / time, 0, 1));
        } else if (currentState.equals(State.RETRACTING)) {
            return easing.get().apply(MathHelper.clamp(1 - (System.currentTimeMillis() - currentStateStart) / time, 0, 1));
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

    /**
     * Sets the state (no animation)
     *
     * @param expand Expand or retract
     */
    public void setStateHard(boolean expand) {
        if (expand) {
            currentState = State.EXPANDING;
            initialState = true;

            // reset time
            currentStateStart = System.currentTimeMillis();
        } else {
            previousState = State.RETRACTING;
            currentState = State.RETRACTING;
            initialState = false;
        }
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

    public enum Easing {
        /**
         * No easing -> A - B
         */
        LINEAR((input) -> input),

        /**
         * Speed increases till halfway, then decreases
         */
        EXPO_IN_OUT((input) -> input < 0.5 ? 4 * input * input * input : 1 - Math.pow(-2 * input + 2, 3) / 2),

        /**
         * Speed increases till halfway, then decreases, but slightly less 'steep' than EXPO_IN_OUT
         */
        CUBIC_IN_OUT((input) -> input < 0.5 ? 4 * input * input * input : 1 - Math.pow(-2 * input + 2, 3) / 2);

        private final Function<Double, Double> function;

        Easing(Function<Double, Double> function) {
            this.function = function;
        }

        public double apply(double input) {
            return function.apply(input);
        }
    }
}