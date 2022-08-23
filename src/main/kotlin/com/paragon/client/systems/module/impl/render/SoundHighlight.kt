package com.paragon.client.systems.module.impl.render

import com.paragon.api.event.network.PacketEvent.PostReceive
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.RenderUtil.drawNametagText
import com.paragon.bus.listener.Listener
import net.minecraft.client.resources.I18n
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import org.apache.commons.lang3.tuple.Pair
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Surge
 * @since 02/05/2022
 */
object SoundHighlight : Module("SoundHighlight", Category.RENDER, "Highlights the positions of sounds in the world") {

    private val format = Setting(
        "Format",
        true
    ) describedBy "Use the translated names rather than the raw sound paths"

    // Map of sounds to render
    private val soundMap: MutableMap<Vec3d, Pair<String, Long>> = ConcurrentHashMap()

    override fun onRender3D() {
        // Iterate through sounds
        soundMap.forEach { (vec, pair) ->
            // Draw nametag at sound position
            drawNametagText(pair.left, vec, Color(255, 255, 255, MathHelper.clamp(pair.right.toInt(), 4, 255)).rgb)

            // Decrement alpha
            soundMap[vec] = Pair.of(pair.left, pair.right - 1)
        }

        // Remove sounds with alpha 0
        soundMap.entries.removeIf { (_, value) -> value.right <= 0 }
    }

    @Listener
    fun onPacketReceive(event: PostReceive) {
        // Packet is a sound packet
        if (event.packet is SPacketSoundEffect) {
            val packet = event.packet
            val path = if (format.value) {
                I18n.format("subtitles." + event.packet.sound.soundName.resourcePath)
            } else event.packet.sound.soundName.resourcePath

            // Exclude sounds that don't have translations
            if (!path.contains("subtitles.")) {
                // Add sound to map
                soundMap[Vec3d(packet.x, packet.y, packet.z)] = Pair.of(path, 255L)
            }
        }
    }

}