package com.paragon.client.systems.module.impl.movement;

import com.paragon.api.util.player.PlayerUtil;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * @author Wolfsurge
 */
public class Step extends Module {

    // Step mode
    private final ModeSetting<Mode> mode = new ModeSetting<>("Mode", "What mode to use", Mode.PACKET);

    // Vanilla step height
    private final NumberSetting stepHeight = (NumberSetting) new NumberSetting("Step Height", "How high to step up", 1.5f, 0.5f, 2.5f, 0.5f).setVisiblity(() -> mode.getCurrentMode() == Mode.VANILLA);

    public Step() {
        super("Step", ModuleCategory.MOVEMENT, "Lets you instantly step up blocks");
        this.addSettings(mode, stepHeight);
    }

    @Override
    public void onDisable() {
        if (nullCheck()) {
            return;
        }

        // Set step height to normal
        mc.player.stepHeight = 0.5f;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (nullCheck()) {
            return;
        }

        switch (mode.getCurrentMode()) {
            case PACKET:
                // Set our position if we are: collided, on ground, not falling, not on a ladder, and not jumping
                if (mc.player.collidedHorizontally && mc.player.onGround && mc.player.fallDistance == 0.0f && !mc.player.isOnLadder() && !mc.player.movementInput.jump) {
                    // Send packet
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1, mc.player.posZ, true));

                    // Set position
                    mc.player.setPosition(mc.player.posX, mc.player.posY + 1, mc.player.posZ);

                    // We want to move a tiny bit forwards
                    PlayerUtil.move(0.01f);
                }
                break;

            case VANILLA:
                // Increase step height
                mc.player.stepHeight = stepHeight.getValue();
                break;

        }

    }

    public enum Mode {
        /**
         * Vanilla step - bypasses almost no servers :P
         */
        VANILLA,

        /**
         * Packet step - Higher chance of bypassing, probably
         */
        PACKET
    }

}
