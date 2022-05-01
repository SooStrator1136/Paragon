package com.paragon.client.systems.module.impl.movement;

import com.paragon.api.event.world.PlayerCollideWithBlockEvent;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.init.Blocks;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

/**
 * @author Wolfsurge
 */
public class NoSlow extends Module {

    private final BooleanSetting soulSand = new BooleanSetting("Soul Sand", "Stop soul sand from slowing you down", true);
    private final BooleanSetting slime = new BooleanSetting("Slime", "Stop slime blocks from slowing you down", true);
    private final BooleanSetting items = new BooleanSetting("Items", "Stop items from slowing you down", true);

    public NoSlow() {
        super("NoSlow", ModuleCategory.MOVEMENT, "Stop certain blocks and actions from slowing you down");
        this.addSettings(soulSand, slime, items);
    }

    @SubscribeEvent
    public void onInput(InputUpdateEvent event) {
        if (nullCheck()) {
            return;
        }

        if (items.isEnabled() && mc.player.isHandActive() && !mc.player.isRiding()) {
            mc.player.movementInput.moveForward *= 5;
            mc.player.movementInput.moveStrafe *= 5;
        }
    }

    @Listener
    public void onCollideWithBlock(PlayerCollideWithBlockEvent event) {
        if (event.getBlockType() == Blocks.SOUL_SAND && soulSand.isEnabled() || event.getBlockType() == Blocks.SLIME_BLOCK && slime.isEnabled()) {
            event.cancel();
        }
    }

}
