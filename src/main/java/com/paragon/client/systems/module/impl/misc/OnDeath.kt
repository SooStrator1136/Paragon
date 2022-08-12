package com.paragon.client.systems.module.impl.misc

import com.paragon.Paragon
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * @author Surge
 */
object OnDeath : Module("OnDeath", Category.MISC, "Do certain actions when you die") {

    private val printCoords = Setting(
        "PrintCoords",
        true
    ) describedBy "Prints your death coordinates in chat (client-side only)"

    private val respawn = Setting(
        "Respawn",
        true
    ) describedBy "Respawns you after death"

    @SubscribeEvent
    fun onLivingDeath(event: LivingDeathEvent) {
        if (minecraft.anyNull) {
            return
        }

        // Check that the entity that died has the same ID that the player does
        if (event.entity.entityId == minecraft.player.entityId) {
            val entity = event.entity
            if (printCoords.value) {
                val pos = entity.position

                // Build the death coord string
                val string = TextFormatting.RED.toString() + "You died at" +
                        TextFormatting.WHITE + " X " + TextFormatting.GRAY + pos.x +
                        TextFormatting.WHITE + " Y " + TextFormatting.GRAY + pos.y +
                        TextFormatting.WHITE + " Z " + TextFormatting.GRAY + pos.z

                // Display the client message
                Paragon.INSTANCE.commandManager.sendClientMessage(string, false)
            }
            if (respawn.value) {
                // Respawn the player
                minecraft.player.respawnPlayer()
            }
        }
    }

}