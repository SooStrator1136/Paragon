package com.paragon.client.systems.module.impl.misc;

import com.paragon.asm.mixins.accessor.IMinecraft;
import com.paragon.asm.mixins.accessor.ITimer;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.NumberSetting;

/**
 * @author Wolfsurge
 */
public class Timer extends Module {

    private final NumberSetting timer = new NumberSetting("Timer Speed", "How much to multiply the timer speed by", 1.25f, 0.01f, 20, 0.01f);

    public Timer() {
        super("Timer", ModuleCategory.MISC, "Modifies how long each tick takes");
        this.addSettings(timer);
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
