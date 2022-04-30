package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.ArrayList;
import java.util.List;

public class AutoEZ extends Module {

    public static AutoEZ INSTANCE;

    private final List<EntityPlayer> targeted = new ArrayList<>();

    public AutoEZ() {
        super("AutoEZ", ModuleCategory.MISC, "Automatically sends a message when you kill an opponent");

        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        mc.world.loadedEntityList.forEach(entity -> {
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;

                if (player.getHealth() <= 0 && targeted.contains(player)) {
                    mc.player.sendChatMessage(player.getName() + ", did you really just die to the worst client?!");
                    targeted.remove(player);
                }
            }
        });
    }

    @Listener
    public void onPacketSent(PacketEvent.PreSend event) {
        if (event.getPacket() instanceof CPacketUseEntity) {
            CPacketUseEntity packet = (CPacketUseEntity) event.getPacket();

            if (packet.getAction() == CPacketUseEntity.Action.ATTACK) {
                if (packet.getEntityFromWorld(mc.world) instanceof EntityPlayer) {
                    addTarget(packet.getEntityFromWorld(mc.world).getName());
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (nullCheck()) {
            return;
        }

        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();

            if (player.getHealth() <= 0 && targeted.contains(player)) {
                mc.player.sendChatMessage(player.getName() + ", did you really just die to the worst client?!");
                targeted.remove(player);
            }
        }
    }

    public static void addTarget(String name) {
        INSTANCE.targeted.add(mc.world.getPlayerEntityByName(name));
    }

}
