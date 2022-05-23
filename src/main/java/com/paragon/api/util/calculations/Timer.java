package com.paragon.api.util.calculations;

public class Timer {

    public long milliseconds;

    public Timer() {
        this.milliseconds = -1;
    }

    public boolean hasMSPassed(double time) {
        return System.currentTimeMillis() - milliseconds > time;
    }

    public void reset() {
        this.milliseconds = System.currentTimeMillis();
    }

}