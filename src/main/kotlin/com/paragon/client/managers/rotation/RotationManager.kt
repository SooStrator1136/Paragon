package com.paragon.client.managers.rotation

import com.paragon.Paragon
import com.paragon.api.event.network.PacketEvent.PreSend
import com.paragon.api.util.Wrapper
import com.paragon.api.util.anyNull
import com.paragon.api.util.player.RotationUtil
import com.paragon.mixins.accessor.ICPacketPlayer
import com.paragon.bus.listener.Listener
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.Vec2f
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs

/**
 * @author Surge
 */
class RotationManager : Wrapper {

    private val rotationsQueue = CopyOnWriteArrayList<Rotation>()
    private var packetYaw = -1f
    private var packetPitch = -1f

    var serverRotation = Vec2f(-1f, -1f)

    init {
        MinecraftForge.EVENT_BUS.register(this)
        Paragon.INSTANCE.eventBus.register(this)
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (minecraft.anyNull) {
            rotationsQueue.clear()
            return
        }

        rotationsQueue.removeIf { it.rotate == Rotate.NONE }

        if (rotationsQueue.isNotEmpty()) {

            rotationsQueue.sortBy { it.priority.priority }

            val rotation = rotationsQueue[0]

            // We use server rotation because it will be updated whether the mode is packet or not
            packetYaw = calculateAngle(serverRotation.x, rotation.yaw)
            packetPitch = calculateAngle(serverRotation.y, rotation.pitch)

            if (rotation.rotate == Rotate.LEGIT) {
                minecraft.player.rotationYaw = packetYaw
                minecraft.player.rotationYawHead = packetYaw
                minecraft.player.rotationPitch = packetPitch
            }

            minecraft.player.connection.sendPacket(CPacketPlayer())

            rotationsQueue.clear()
        }
    }

    @Listener
    fun onPacketSend(event: PreSend) {
        if (event.packet is CPacketPlayer) {
            if (packetYaw != -1f && packetPitch != -1f) {
                event.cancel()

                (event.packet as ICPacketPlayer).yaw = packetYaw
                (event.packet as ICPacketPlayer).pitch = packetPitch

                packetYaw = -1f
                packetPitch = -1f
            }

            serverRotation = Vec2f((event.packet as ICPacketPlayer).yaw, (event.packet as ICPacketPlayer).pitch)
        }
    }

    fun addRotation(rotation: Rotation) {
        rotationsQueue.add(rotation)
    }

    private fun calculateAngle(playerAngle: Float, wantedAngle: Float): Float {
        var calculatedAngle = wantedAngle - playerAngle

        if (abs(calculatedAngle) > 180) {
            calculatedAngle = RotationUtil.normalizeAngle(calculatedAngle)
        }

        // 55 is half max FOV, should work
        calculatedAngle = if (abs(calculatedAngle) > 55) {
            RotationUtil.normalizeAngle(playerAngle + 55 * if (wantedAngle > 0) 1 else -1)
        } else {
            wantedAngle
        }

        return calculatedAngle
    }

}