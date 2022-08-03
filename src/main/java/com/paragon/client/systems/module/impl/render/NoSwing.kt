package com.paragon.client.systems.module.impl.render

import com.paragon.api.event.network.PacketEvent.PreReceive
import com.paragon.api.event.network.PacketEvent.PreSend
import com.paragon.api.event.render.entity.SwingArmEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import me.wolfsurge.cerauno.listener.Listener
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.server.SPacketAnimation

/**
 * @author Surge
 */
object NoSwing : Module("NoSwing", Category.RENDER, "Cancels the swing animation") {

    private val mode = Setting("Mode", Mode.PACKET_CANCEL)
        .setDescription("How to not swing")

    private val others = Setting("Others", true)
        .setDescription("Whether to cancel other players' animations")

    @Listener
    fun onPacketSend(event: PreSend) {
        if (mode.value == Mode.PACKET_CANCEL && event.packet is CPacketAnimation) {
            event.cancel()
        }
    }

    @Listener
    fun onPacketReceive(event: PreReceive) {
        if (others.value && event.packet is SPacketAnimation) {
            event.cancel()
        }
    }

    @Listener
    fun onSwingArm(event: SwingArmEvent) {
        if (mode.value == Mode.METHOD_CANCEL) {
            event.cancel()
        }
    }

    enum class Mode {
        /**
         * Cancels the swing animation packet
         */
        PACKET_CANCEL,

        /**
         * Cancels the swing method from invoking
         */
        METHOD_CANCEL
    }

}