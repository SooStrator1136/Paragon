package com.paragon.client.systems.module.impl.combat;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.module.Category;
import com.paragon.api.module.Module;

import com.paragon.api.setting.Setting;
import com.paragon.api.util.calculations.Timer;
import com.paragon.api.util.player.InventoryUtil;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;


import java.util.Random;

public class BowBomb extends Module {

    public static Setting<Float> ticks = new Setting<>("Ticks", 10.0F, 1.0F, 50.0F, 1.0F);

    public BowBomb() {
        super("BowBomb", Category.COMBAT, "Makes bows speedy bois");

    }

    private final Timer projectileTimer = new Timer();

    @Listener
    public void onPacketSend(PacketEvent.PreSend event) {
        if (event.getPacket() instanceof CPacketPlayerDigging && ((CPacketPlayerDigging) event.getPacket()).getAction().equals(CPacketPlayerDigging.Action.RELEASE_USE_ITEM)) {
            if (InventoryUtil.isHolding(Items.BOW) && projectileTimer.hasMSPassed(5000)) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));


                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));

                Random projectileRandom = new Random();

                for (int tick = 0; tick < ticks.getValue(); tick++) {

                    double sin = -Math.sin(Math.toRadians(mc.player.rotationYaw));
                    double cos = Math.cos(Math.toRadians(mc.player.rotationYaw));

                    if (projectileRandom.nextBoolean()) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + (sin * 100), mc.player.posY + 5, mc.player.posZ + (cos * 100), false));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX - (sin * 100), mc.player.posY, mc.player.posZ - (cos * 100), true));
                    } else {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX - (sin * 100), mc.player.posY, mc.player.posZ - (cos * 100), true));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + (sin * 100), mc.player.posY + 5, mc.player.posZ + (cos * 100), false));
                    }
                    projectileTimer.reset();
                }
            }
        }
    }
}