package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.world.LiquidInteractEvent;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import me.wolfsurge.cerauno.listener.Listener;

public class LiquidInteract extends Module {

    public LiquidInteract() {
        super("LiquidInteract", Category.MISC, "Lets you place blocks in liquids");
    }

    @Listener
    public void onLiquidInteract(LiquidInteractEvent event) {
        // Cancel event
        event.cancel();
    }

}
