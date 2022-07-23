package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import com.paragon.api.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Wolfsurge
 * @since 02/05/2022
 */
public class SoundHighlight extends Module {

    public static Setting<Boolean> format = new Setting<>("Format", true)
            .setDescription("Use the translated names rather than the raw sound paths");

    // Map of sounds to render
    private final Map<Vec3d, Pair<String, Long>> soundMap = new ConcurrentHashMap<>();

    public SoundHighlight() {
        super("SoundHighlight", Category.RENDER, "Highlights the positions of sounds in the world");
    }

    @Override
    public void onRender3D() {
        // Iterate through sounds
        soundMap.forEach((vec, pair) -> {
            // Draw nametag at sound position
            RenderUtil.drawNametagText(pair.getLeft(), vec, new Color(255, 255, 255, MathHelper.clamp(pair.getRight().intValue(), 4, 255)).getRGB());

            // Decrement alpha
            soundMap.put(vec, Pair.of(pair.getLeft(), pair.getRight() - 1));
        });

        // Remove sounds with alpha 0
        soundMap.entrySet().removeIf(entry -> entry.getValue().getRight() <= 0);
    }

    @Listener
    public void onPacketReceive(PacketEvent.PostReceive event) {
        // Packet is a sound packet
        if (event.getPacket() instanceof SPacketSoundEffect) {
            SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();

            String path = format.getValue() ? I18n.format("subtitles." + ((SPacketSoundEffect) event.getPacket()).getSound().getSoundName().getResourcePath()) : ((SPacketSoundEffect) event.getPacket()).getSound().getSoundName().getResourcePath();

            // Exclude sounds that don't have translations
            if (!path.contains("subtitles.")) {
                // Add sound to map
                soundMap.put(new Vec3d(packet.getX(), packet.getY(), packet.getZ()), Pair.of(path, 255L));
            }
        }
    }

}
