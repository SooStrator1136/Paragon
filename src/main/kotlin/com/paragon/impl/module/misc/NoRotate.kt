package com.paragon.impl.module.misc

import com.paragon.impl.event.network.PacketEvent.PreReceive
import com.paragon.impl.module.Module
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category
import com.paragon.mixins.accessor.ISPacketPlayerPosLook
import com.paragon.util.anyNull
import net.minecraft.network.play.server.SPacketPlayerPosLook

/**
 * @author Surge
 */
object NoRotate : Module("NoRotate", Category.MISC, "Stops the server from rotating your head") {

    @Listener
    fun onPacketReceive(event: PreReceive) {
        if (!minecraft.anyNull && event.packet is SPacketPlayerPosLook) {
            (event.packet as ISPacketPlayerPosLook).hookSetYaw(minecraft.player.rotationYaw)
            (event.packet as ISPacketPlayerPosLook).hookSetPitch(minecraft.player.rotationPitch)
        }
    }

}