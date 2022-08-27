package com.paragon.client.systems.module.impl.movement

import com.paragon.api.event.network.PacketEvent.PreReceive
import com.paragon.api.event.world.entity.EntityPushEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.mixins.accessor.ISPacketEntityVelocity
import com.paragon.mixins.accessor.ISPacketExplosion
import com.paragon.bus.listener.Listener
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.network.play.server.SPacketExplosion

/**
 * @author Surge
 */
object Velocity : Module("Velocity", Category.MOVEMENT, "Stops crystals and mobs from causing you knockback") {

    private val velocityPacket = Setting("VelocityPacket", true, null, null, null) describedBy "Cancels or modifies the velocity packet"
    private val explosions = Setting("Explosions", true, null, null, null) describedBy "Cancels or modifies the explosion knockback"
    private val horizontal = Setting("Horizontal", 0f, 0f, 100f, 1f) describedBy "The horizontal modifier"
    private val vertical = Setting("Vertical", 0f, 0f, 100f, 1f) describedBy "The vertical modifier"
    private val noPush = Setting("NoPush", true, null, null, null) describedBy "Prevents the player from being pushed by entities"

    @Listener
    fun onPacket(event: PreReceive) {
        if (event.packet is SPacketEntityVelocity && velocityPacket.value!!) {
            // Check it is for us
            if (event.packet.entityID == minecraft.player.getEntityId()) {
                // We can just cancel the packet if both horizontal and vertical are 0
                if (horizontal.value == 0f && vertical.value == 0f) {
                    event.cancel()
                }

                else {
                    (event.packet as ISPacketEntityVelocity).setMotionX(((event.packet as SPacketEntityVelocity).motionX / 100 * (horizontal.value / 100)).toInt())
                    (event.packet as ISPacketEntityVelocity).setMotionY(vertical.value.toInt() / 100)
                    (event.packet as ISPacketEntityVelocity).setMotionZ(((event.packet as SPacketEntityVelocity).motionZ / 100 * (horizontal.value / 100)).toInt())
                }
            }
        }
        if (event.packet is SPacketExplosion && explosions.value!!) {
            // We can just cancel the packet if both horizontal and vertical are 0
            if (horizontal.value == 0f && vertical.value == 0f) {
                event.cancel()
            }

            else {
                (event.packet as ISPacketExplosion).setMotionX(horizontal.value / 100 * (event.packet as SPacketExplosion).motionX)
                (event.packet as ISPacketExplosion).setMotionY(vertical.value / 100 * (event.packet as SPacketExplosion).motionY)
                (event.packet as ISPacketExplosion).setMotionZ(horizontal.value / 100 * (event.packet as SPacketExplosion).motionZ)
            }
        }
    }

    @Listener
    fun onEntityPush(event: EntityPushEvent) {
        if (noPush.value!! && event.entity === minecraft.player) {
            event.cancel()
        }
    }

    override fun getData(): String {
        return "H% " + horizontal.value + ", V% " + vertical.value
    }

}