package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import com.paragon.api.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Surge
 */
public class AutoEZ extends Module {

    public static AutoEZ INSTANCE;

    public static Setting<Double> maximumRange = new Setting<>("MaxRange", 10.0D, 1.0D, 20.0D, 0.1D)
            .setDescription("The furthest distance from the player to target");

    // List of targeted players
    private final List<EntityPlayer> targeted = new CopyOnWriteArrayList<>();

    public AutoEZ() {
        super("AutoEZ", Category.MISC, "Automatically sends a message when you kill an opponent");

        INSTANCE = this;
    }

    public static void addTarget(String name) {
        if (!INSTANCE.targeted.contains(mc.world.getPlayerEntityByName(name))) {
            INSTANCE.targeted.add(mc.world.getPlayerEntityByName(name));
        }
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        targeted.removeIf(player -> !(player.getDistance(mc.player) <= maximumRange.getValue()));

        // Iterate through entities
        targeted.forEach(player -> {
            if (player.getHealth() <= 0 && targeted.contains(player)) {
                mc.player.sendChatMessage(player.getName() + ", did you really just die to the worst client?!");
                targeted.remove(player);
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

}
