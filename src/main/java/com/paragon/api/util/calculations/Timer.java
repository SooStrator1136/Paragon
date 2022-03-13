package com.paragon.api.util.calculations;

public class Timer {

    public long lastMS;

    public void reset() {
        lastMS = System.currentTimeMillis();
    }

    public boolean hasTimePassed(long time) {
        return System.currentTimeMillis() - lastMS > time;
    }

    public Timer() {
        this.lastMS = 0L;
    }

    public long getTime() {
        return System.nanoTime() / 1000000L;
    }

}