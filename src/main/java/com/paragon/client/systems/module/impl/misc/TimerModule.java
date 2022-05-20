package com.paragon.client.systems.module.impl.misc;

import com.paragon.asm.mixins.accessor.IMinecraft;
import com.paragon.asm.mixins.accessor.ITimer;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;

/**
 * @author Wolfsurge
 */
public class TimerModule extends Module {

    public static TimerModule INSTANCE;

    public static Setting<Float> timer = new Setting<>("Timer Speed", 1.25f, 0.01f, 20f, 0.01f)
            .setDescription("How much to multiply the timer speed by");

    public TimerModule() {
        super("Timer", Category.MISC, "Modifies how long each tick takes");

        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        ((ITimer) ((IMinecraft) mc).getTimer()).setTickLength(50);
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        ((ITimer) ((IMinecraft) mc).getTimer()).setTickLength(50 / timer.getValue());
    }
}
