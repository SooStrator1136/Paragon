package com.paragon.client.systems.module.impl.movement;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
            if (((SPacketEntityVelocity) event.getPacket()).getEntityID() == mc.player.getEntityId()) {
                event.cancel();
            }
        }

        if (event.getPacket() instanceof SPacketExplosion && explosions.isEnabled()) {
            event.cancel();
        }
    }

    @Override
    public String getModuleInfo() {
        return " H% " + horizontal.getValue() + ", V% " + vertical.getValue();
    }
}
