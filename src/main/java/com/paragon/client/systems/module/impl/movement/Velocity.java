package com.paragon.client.systems.module.impl.movement;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.asm.mixins.accessor.ISPacketEntityVelocity;
import com.paragon.asm.mixins.accessor.ISPacketExplosion;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Wolfsurge
 */
public class Velocity extends Module {

    private final BooleanSetting velocityPacket = new BooleanSetting("Velocity Packet", "Cancels or modifies the velocity packet", true);
    private final BooleanSetting explosions = new BooleanSetting("Explosions", "Cancels or modifies the explosion knockback", true);
    private final NumberSetting horizontal = new NumberSetting("Horizontal", "The horizontal modifier", 0, 0, 100, 1);
    private final NumberSetting vertical = new NumberSetting("Vertical", "The vertical modifier", 0, 0, 100, 1);

    public Velocity() {
        super("Velocity", ModuleCategory.MOVEMENT, "Stops crystals and mobs from causing you knockback");
        this.addSettings(velocityPacket, explosions, horizontal, vertical);
    }

    @Listener
    public void onPacket(PacketEvent.PreReceive event) {
        if (event.getPacket() instanceof SPacketEntityVelocity && velocityPacket.isEnabled()) {
            // Check it is for us
            if (((SPacketEntityVelocity) event.getPacket()).getEntityID() == mc.player.getEntityId()) {
                // We can just cancel the packet if both horizontal and vertical are 0
                if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
                    event.cancel();
                }
                // Otherwise, we want to modify the values
                else {
                    ((ISPacketEntityVelocity) event.getPacket()).setMotionX((int) ((((SPacketEntityVelocity) event.getPacket()).getMotionX() / 100) * (horizontal.getValue() / 100)));
                    ((ISPacketEntityVelocity) event.getPacket()).setMotionY((int) vertical.getValue() / 100);
                    ((ISPacketEntityVelocity) event.getPacket()).setMotionZ((int) ((((SPacketEntityVelocity) event.getPacket()).getMotionZ() / 100) * (horizontal.getValue() / 100)));
                }
            }
        }

        if (event.getPacket() instanceof SPacketExplosion && explosions.isEnabled()) {
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

    @Override
    public String getArrayListInfo() {
        return " H% " + horizontal.getValue() + ", V% " + vertical.getValue();
    }
}
