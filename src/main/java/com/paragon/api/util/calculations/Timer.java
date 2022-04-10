package com.paragon.api.util.calculations;

import com.paragon.api.util.Wrapper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Timer implements Wrapper {

    public long milliseconds;
    public int tickCount;

    public Timer() {
        milliseconds = -1;
        tickCount = -1;

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientTick(LivingEvent.LivingUpdateEvent event) {
        if (nullCheck()) {
            milliseconds = -1;
            tickCount = -1;
        } else {
            tickCount++;
        }
    }

    public boolean hasTimePassed(long time, TimeFormat format) {
        switch (format) {
            case MILLISECONDS:
                return System.currentTimeMillis() - milliseconds > time;
            case SECONDS:
                return System.currentTimeMillis() - milliseconds > time * 1000;
            case TICKS:
                return tickCount > time;
        }

        return false;
    }

    public void reset() {
        milliseconds = System.currentTimeMillis();
    }

    public enum TimeFormat {
        /**
         * Time in milliseconds
         */
        MILLISECONDS,

        /**
         * Time in seconds
         */
        SECONDS,

        /**
         * Time in in-game ticks
         */
        TICKS
    }

}