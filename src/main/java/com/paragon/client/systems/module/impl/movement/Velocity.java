package com.paragon.client.systems.module.impl.movement;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.event.world.entity.EntityPushEvent;
import com.paragon.asm.mixins.accessor.ISPacketEntityVelocity;
import com.paragon.asm.mixins.accessor.ISPacketExplosion;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;

/**
 * @author Wolfsurge
 */
public class Velocity extends Module {

    public static Velocity INSTANCE;

    public static Setting<Boolean> velocityPacket = new Setting<>("VelocityPacket", true)
            .setDescription("Cancels or modifies the velocity packet");

    public static Setting<Boolean> explosions = new Setting<>("Explosions", true)
            .setDescription("Cancels or modifies the explosion knockback");

    public static Setting<Float> horizontal = new Setting<>("Horizontal", 0f, 0f, 100f, 1f)
            .setDescription("The horizontal modifier");

    public static Setting<Float> vertical = new Setting<>("Vertical", 0f, 0f, 100f, 1f)
            .setDescription("The vertical modifier");

    public static Setting<Boolean> noPush = new Setting<>("NoPush", true)
            .setDescription("Prevents the player from being pushed by entities");

    public Velocity() {
        super("Velocity", Category.MOVEMENT, "Stops crystals and mobs from causing you knockback");

        INSTANCE = this;
    }

    @Listener
    public void onPacket(PacketEvent.PreReceive event) {
        if (event.getPacket() instanceof SPacketEntityVelocity && velocityPacket.getValue()) {
            // Check it is for us
            if (((SPacketEntityVelocity) event.getPacket()).getEntityID() == mc.player.getEntityId()) {
                // We can just cancel the packet if both horizontal and vertical are 0
                if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
                    event.cancel();
                }
                // Otherwise, we want to modify the values
                else {
                    ((ISPacketEntityVelocity) event.getPacket()).setMotionX((int) ((((SPacketEntityVelocity) event.getPacket()).getMotionX() / 100) * (horizontal.getValue() / 100)));
                    ((ISPacketEntityVelocity) event.getPacket()).setMotionY(vertical.getValue().intValue() / 100);
                    ((ISPacketEntityVelocity) event.getPacket()).setMotionZ((int) ((((SPacketEntityVelocity) event.getPacket()).getMotionZ() / 100) * (horizontal.getValue() / 100)));
                }
            }
        }

        if (event.getPacket() instanceof SPacketExplosion && explosions.getValue()) {
            // We can just cancel the packet if both horizontal and vertical are 0
            if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
                event.cancel();
            }
            // Otherwise, we want to modify the values
            else {
                ((ISPacketExplosion) event.getPacket()).setMotionX((horizontal.getValue() / 100) * (((SPacketExplosion) event.getPacket()).getMotionX()));
                ((ISPacketExplosion) event.getPacket()).setMotionY((vertical.getValue() / 100) * (((SPacketExplosion) event.getPacket()).getMotionY()));
                ((ISPacketExplosion) event.getPacket()).setMotionZ((horizontal.getValue() / 100) * (((SPacketExplosion) event.getPacket()).getMotionZ()));
            }
        }
    }

    @Listener
    public void onEntityPush(EntityPushEvent event) {
        if (noPush.getValue() && event.getEntity() == mc.player) {
            event.cancel();
        }
    }

    @Override
    public String getData() {
        return " H% " + horizontal.getValue() + ", V% " + vertical.getValue();
    }
}
