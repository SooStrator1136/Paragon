package com.paragon.client.managers.rotation

import com.paragon.Paragon
import com.paragon.api.event.network.PacketEvent.PreSend
import com.paragon.api.event.player.RotationUpdateEvent
import com.paragon.api.util.Wrapper
import com.paragon.api.util.player.RotationUtil
import com.paragon.asm.mixins.accessor.ICPacketPlayer
import io.ktor.client.engine.*
import me.wolfsurge.cerauno.listener.Listener
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.Vec2f
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import kotlin.math.min

/**
 * @author Wolfsurge
 */
class RotationManager : Wrapper {
    private val rotationsQueue = CopyOnWriteArrayList<Rotation>()
    private var packetYaw = -1f
    private var packetPitch = -1f

    private var serverRotation: Vec2f = Vec2f(-1f, -1f)

    @Listener
    fun onRotationUpdate(event: RotationUpdateEvent) {
        if (!rotationsQueue.isEmpty()) {
            event.cancel()
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (nullCheck()) {
            rotationsQueue.clear()
            return
        }

        if (!rotationsQueue.isEmpty()) {
            // Fix ArrayIndexOutOfBoundsException
            if (rotationsQueue.size == 0) {
                return
            }

            rotationsQueue.removeIf { rotation -> rotation.rotate == Rotate.NONE }

            rotationsQueue.sortBy { rotation -> rotation.priority.priority }

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

    init {
        MinecraftForge.EVENT_BUS.register(this)
        Paragon.INSTANCE.eventBus.register(this)
    }
}