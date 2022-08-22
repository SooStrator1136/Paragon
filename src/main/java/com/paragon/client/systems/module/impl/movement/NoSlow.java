package com.paragon.client.systems.module.impl.movement;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.event.world.PlayerCollideWithBlockEvent;
import com.paragon.api.module.Category;
import com.paragon.api.module.Module;
import com.paragon.api.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Surge, aesthetical
 */
public class NoSlow extends Module {

    public static NoSlow INSTANCE;

    public static Setting<Boolean> soulSand = new Setting<>("SoulSand", true, null, null, null)
            .setDescription("Stop soul sand from slowing you down");

    public static Setting<Boolean> slime = new Setting<>("Slime", true, null, null, null)
            .setDescription("Stop slime blocks from slowing you down");

    public static Setting<Boolean> items = new Setting<>("Items", true, null, null, null)
            .setDescription("Stop items from slowing you down");

    public static Setting<Boolean> ncpStrict = new Setting<>("NCPStrict", false, null, null, null)
            .setDescription("If to bypass NCP strict checks");

    private boolean sneakState = false;
    private boolean sprintState = false;

    public NoSlow() {
        super("NoSlow", Category.MOVEMENT, "Stop certain blocks and actions from slowing you down");

        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (nullCheck()) {
            return;
        }

        if (sneakState && !mc.player.isSneaking()) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
            sneakState = false;
        }

        if (sprintState && mc.player.isSprinting()) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SPRINTING));
            sprintState = false;
        }
    }

    @SubscribeEvent
    public void onInput(InputUpdateEvent event) {
        if (nullCheck()) {
            return;
        }

        if (items.getValue() && mc.player.isHandActive() && !mc.player.isRiding()) {
            mc.player.movementInput.moveForward *= 5;
            mc.player.movementInput.moveStrafe *= 5;

            if (ncpStrict.getValue()) {

                // funny NCP bypass - good job ncp devs
                mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
            }
        }
    }

    @Listener
    public void onCollideWithBlock(PlayerCollideWithBlockEvent event) {
        if (event.getBlockType() == Blocks.SOUL_SAND && soulSand.getValue() || event.getBlockType() == Blocks.SLIME_BLOCK && slime.getValue()) {
            event.cancel();
        }
    }

    @Listener
    public void onPacketSendPre(PacketEvent.PreSend event) {
        if (event.getPacket() instanceof CPacketClickWindow && ncpStrict.getValue()) {

            // i love ncp updated devs - the inventory checks are almost as good as verus's

            if (!mc.player.isSneaking()) {
                sneakState = true;
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
            }

            if (mc.player.isSprinting()) {
                sprintState = true;
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SPRINTING));
            }
        }
    }

    @Listener
    public void onPacketSendPost(PacketEvent.PostSend event) {
        if (event.getPacket() instanceof CPacketClickWindow && ncpStrict.getValue()) {

            // reset states

            if (sneakState && !mc.player.isSneaking()) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
                sneakState = false;
            }

            if (sprintState && mc.player.isSprinting()) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SPRINTING));
                sprintState = false;
            }
        }
    }
}
