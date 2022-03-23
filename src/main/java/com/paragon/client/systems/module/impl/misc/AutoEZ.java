package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.client.managers.CommandManager;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.ArrayList;
import java.util.List;

public class AutoEZ extends Module {

    private static final List<String> players = new ArrayList<>();

    public AutoEZ() {
        super("AutoEZ", ModuleCategory.MISC, "Automatically sends a message when you kill an opponent");
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        mc.world.loadedEntityList.forEach(entity -> {
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;

                if (player.isDead && players.contains(player.getName())) {
                    // mc.player.sendChatMessage(player.getName() + ", imagine getting killed by the worst client");
                    players.remove(player.getName());
                }
            }
        });
    }

    @Listener
    public void onPacketSent(PacketEvent.PreSend event) {
        if (event.getPacket() instanceof CPacketUseEntity) {
            CPacketUseEntity packet = (CPacketUseEntity) event.getPacket();

            if (packet.getAction() == CPacketUseEntity.Action.ATTACK) {
                Entity targetEntity = packet.getEntityFromWorld(mc.world);

                if (targetEntity instanceof EntityPlayer) {
                    players.add(targetEntity.getName());
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();

            if (player.getHealth() <= 0 && players.contains(player.getName())) {
                mc.player.sendChatMessage(player.getName() + ", imagine getting killed by the worst client");
                players.remove(player.getName());
            }
        }
    }

    public static void addTarget(String name) {
        if (!name.equals(mc.player.getName())) {
            players.add(name);
        }
    }

}
