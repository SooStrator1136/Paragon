package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.asm.mixins.accessor.IMinecraft;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import com.paragon.api.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;

import java.util.Random;

/**
 * @author Surge
 */
public class FastUse extends Module {

    public static FastUse INSTANCE;

    // Options
    public static Setting<Boolean> xp = new Setting<>("XP", true)
            .setDescription("Fast use XP bottles");

    public static Setting<Boolean> rotate = new Setting<>("Rotate", true)
            .setDescription("Rotate your player when using XP bottles")
            .setParentSetting(xp);

    public static Setting<Boolean> crystals = new Setting<>("Crystals", true)
            .setDescription("Place crystals fast");

    public static Setting<Boolean> randomPause = new Setting<>("RandomPause", true)
            .setDescription("Randomly pauses to try and prevent you from being kicked");

    public static Setting<Float> randomChance = new Setting<>("Chance", 50f, 2f, 100f, 1f)
            .setDescription("The chance to pause")
            .setParentSetting(randomPause);

    public FastUse() {
        super("FastUse", Category.MISC, "Allows you to use items quicker than you would be able to in vanilla");

        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        // Check we want to set the delay timer to 0
        if (xp.getValue() && InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE) || crystals.getValue() && InventoryUtil.isHolding(Items.END_CRYSTAL)) {
            Random random = new Random();

            if (randomPause.getValue() && random.nextInt(randomChance.getValue().intValue()) == 1) {
                ((IMinecraft) mc).setRightClickDelayTimer(4);
                return;
            }

            if (InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE)) {
                mc.player.xpCooldown = 0;
            }

            ((IMinecraft) mc).setRightClickDelayTimer(0);
        }
    }

    @Listener
    public void onPacketSend(PacketEvent.PreSend event) {
        if (event.getPacket() instanceof CPacketPlayerTryUseItem && InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE) && xp.getValue() && rotate.getValue()) {
            // Send rotation packet. We aren't using the rotation manager as it doesn't immediately rotate the player
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, 90f, mc.player.onGround));
        }
    }
}
