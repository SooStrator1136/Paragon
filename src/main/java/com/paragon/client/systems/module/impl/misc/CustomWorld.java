package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.network.play.server.SPacketTimeUpdate;

public class CustomWorld extends Module {

    private final BooleanSetting customWeather = new BooleanSetting("Custom Weather", "Set the world weather to a custom value", true);
    private final ModeSetting<Weather> weather = (ModeSetting<Weather>) new ModeSetting<>("Weather", "The weather to display", Weather.CLEAR).setParentSetting(customWeather);

    private final BooleanSetting customTime = new BooleanSetting("Custom Time", "Set the world time to a custom value", true);
    private final NumberSetting time = (NumberSetting) new NumberSetting("Time", "The time of day", 1000, 0, 24000, 1).setParentSetting(customTime);

    public CustomWorld() {
        super("CustomWorld", ModuleCategory.MISC, "Changes the way the world is shown client side");
        this.addSettings(customWeather, customTime);
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        mc.world.setRainStrength(weather.getCurrentMode().getRainStrength());
        mc.world.setWorldTime((long) time.getValue());
    }

    @Listener
    public void onPacketReceive(PacketEvent.PreReceive event) {
        if (event.getPacket() instanceof SPacketTimeUpdate) {
            // Stop the world from updating the time
            event.cancel();
        }
    }

    public enum Weather {
        /**
         * Clear weather - no rain or thunder
         */
        CLEAR(0),

        /**
         * Rainy weather - just rain, darker sky
         */
        RAIN(1),

        /**
         * Thunder - rain + thunder
         */
        THUNDER(2);

        private int rainStrength;

        Weather(int rainStrength) {
            this.rainStrength = rainStrength;
        }

        /**
         * Gets the rain strength
         * @return The rain strength
         */
        public int getRainStrength() {
            return rainStrength;
        }
    }
}
