package com.paragon.api.util.calculations;

public class Timer {

    public long lastMS;

    public void reset() {
        lastMS = System.currentTimeMillis();
    }

    public boolean hasTimePassed(long time) {
        if ((System.currentTimeMillis() - lastMS) > time) {
            reset();

            return true;
        }

        return false;
    }

    public boolean hasTimePassed(long time, boolean reset) {
        if ((System.currentTimeMillis() - lastMS) > time) {
            if (reset) {
                reset();
            }

            return true;
        }

        return false;
    }

    public Timer() {
        this.lastMS = 0L;
    }

    public long getTime() {
        return System.nanoTime() / 1000000L;
    }

}