package com.paragon.client.systems.module.impl.misc

import com.paragon.api.event.network.PacketEvent.PreReceive
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.util.anyNull
import com.paragon.mixins.accessor.ISPacketPlayerPosLook
import com.paragon.bus.listener.Listener
import net.minecraft.network.play.server.SPacketPlayerPosLook

/**
 * @author Surge
 */
object NoRotate : Module("NoRotate", Category.MISC, "Stops the server from rotating your head") {

    @Listener
    fun onPacketReceive(event: PreReceive) {
        if (minecraft.anyNull) {
            return
        }

        if (event.packet is SPacketPlayerPosLook) {
            // Set packet yaw
            (event.packet as ISPacketPlayerPosLook).setYaw(minecraft.player.rotationYaw)

            // Set packet pitch
            (event.packet as ISPacketPlayerPosLook).setPitch(minecraft.player.rotationPitch)
        }
    }

}