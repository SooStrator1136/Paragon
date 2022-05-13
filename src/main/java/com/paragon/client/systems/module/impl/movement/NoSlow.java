package com.paragon.client.systems.module.impl.movement;

import com.paragon.api.event.world.PlayerCollideWithBlockEvent;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.init.Blocks;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Wolfsurge
 */
public class NoSlow extends Module {

    private final Setting<Boolean> soulSand = new Setting<>("Soul Sand", true)
            .setDescription("Stop soul sand from slowing you down");

    private final Setting<Boolean> slime = new Setting<>("Slime", true)
            .setDescription("Stop slime blocks from slowing you down");

    private final Setting<Boolean> items = new Setting<>("Items", true)
            .setDescription("Stop items from slowing you down");

    public NoSlow() {
        super("NoSlow", Category.MOVEMENT, "Stop certain blocks and actions from slowing you down");
        this.addSettings(soulSand, slime, items);
    }

    @SubscribeEvent
    public void onInput(InputUpdateEvent event) {
        if (nullCheck()) {
            return;
        }

        if (items.getValue() && mc.player.isHandActive() && !mc.player.isRiding()) {
            mc.player.movementInput.moveForward *= 5;
            mc.player.movementInput.moveStrafe *= 5;
        }
    }

    @Listener
    public void onCollideWithBlock(PlayerCollideWithBlockEvent event) {
        if (event.getBlockType() == Blocks.SOUL_SAND && soulSand.getValue() || event.getBlockType() == Blocks.SLIME_BLOCK && slime.getValue()) {
            event.cancel();
        }
    }

}
