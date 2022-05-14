package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.network.play.server.SPacketTimeUpdate;

public class CustomWorld extends Module {

    private final Setting<Boolean> customWeather = new Setting<>("Custom Weather", true)
            .setDescription("Set the world weather to a custom value");

    private final Setting<Weather> weather = new Setting<>("Weather", Weather.CLEAR)
            .setDescription("The weather to display")
            .setParentSetting(customWeather);

    private final Setting<Boolean> customTime = new Setting<>("Custom Time", true)
            .setDescription("Set the world time to a custom value");

    private final Setting<Float> time = new Setting<>("Time", 1000f, 0f, 24000f, 1f)
            .setDescription("The time of day")
            .setParentSetting(customTime);

    public CustomWorld() {
        super("CustomWorld", Category.MISC, "Changes the way the world is shown client side");
        this.addSettings(customWeather, customTime);
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        mc.world.setRainStrength(weather.getValue().getRainStrength());
        mc.world.setWorldTime(time.getValue().longValue());
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
         *
         * @return The rain strength
         */
        public int getRainStrength() {
            return rainStrength;
        }
    }
}
