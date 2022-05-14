package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.util.calculations.Timer;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.Module;

/**
 * ehehehehhehehehehhe
 */
public class TeleTofu extends Module {

    private final Timer timer = new Timer();

    public TeleTofu() {
        super("TeleTofu", Category.MISC, "Performs a TeleTofu on the target");
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        if (timer.hasMSPassed(100D)) {
            mc.player.sendChatMessage("I'M GAY FOR TELETOFU WOOOO");

            timer.reset();
        }
    }
}
