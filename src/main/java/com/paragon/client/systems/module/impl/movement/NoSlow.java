package com.paragon.client.systems.module.impl.movement;

import com.paragon.api.event.world.PlayerCollideWithBlockEvent;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.init.Blocks;

/**
 * @author Wolfsurge
 */
public class NoSlow extends Module {

    private final BooleanSetting soulSand = new BooleanSetting("Soul Sand", "Stop soul sand from slowing you down", true);
    private final BooleanSetting slime = new BooleanSetting("Slime", "Stop slime blocks from slowing you down", true);

    public NoSlow() {
        super("NoSlow", ModuleCategory.MOVEMENT, "Stop certain blocks and actions from slowing you down");
        this.addSettings(soulSand, slime);
    }

    @Listener
    public void onCollideWithBlock(PlayerCollideWithBlockEvent event) {
        if (event.getBlockType() == Blocks.SOUL_SAND && soulSand.isEnabled() || event.getBlockType() == Blocks.SLIME_BLOCK && slime.isEnabled()) {
            event.cancel();
        }
    }

}
