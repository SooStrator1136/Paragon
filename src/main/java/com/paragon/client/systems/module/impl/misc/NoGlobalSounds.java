package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundCategory;

/**
 * @author Wolfsurge
 * @since 14/05/22
 */
public class NoGlobalSounds extends Module {

    public static NoGlobalSounds INSTANCE;

    // Sounds to cancel
    public static Setting<Boolean> endPortal = new Setting<>("EndPortal", true)
            .setDescription("Disables the end portal spawn sound");

    public static Setting<Boolean> witherSpawn = new Setting<>("WitherSpawn", true)
            .setDescription("Disables the wither spawn sound");

    public static Setting<Boolean> dragonDeath = new Setting<>("DragonDeath", true)
            .setDescription("Disables the dragon death sound");

    public static Setting<Boolean> lightning = new Setting<>("Lightning", true)
            .setDescription("Disables the lightning sound");

    public NoGlobalSounds() {
        super("NoGlobalSounds", Category.MISC, "Prevents global sounds from playing");

        INSTANCE = this;
    }

    @Listener
    public void onPacketReceive(PacketEvent.PreReceive event) {
        if (event.getPacket() instanceof SPacketSoundEffect) {
            SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();

            if (packet.getCategory().equals(SoundCategory.WEATHER) && packet.getSound().equals(SoundEvents.ENTITY_LIGHTNING_THUNDER) && lightning.getValue()) {
                event.cancel();
            }
        }

        if (event.getPacket() instanceof SPacketEffect) {
            SPacketEffect packet = (SPacketEffect) event.getPacket();

            if (packet.getSoundType() == 1038 && endPortal.getValue() || packet.getSoundType() == 1023 && witherSpawn.getValue() || packet.getSoundType() == 1028 && dragonDeath.getValue()) {
                event.cancel();
            }
        }
    }

}
