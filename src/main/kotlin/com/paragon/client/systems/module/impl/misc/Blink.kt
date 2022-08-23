package com.paragon.client.systems.module.impl.misc

import com.paragon.api.event.network.PacketEvent.PreSend
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.calculations.Timer
import com.paragon.bus.listener.Listener
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author Surge
 */
@SideOnly(Side.CLIENT)
object Blink : Module("Blink", Category.MISC, "Cancels sending packets for a length of time") {

    // General
    private val mode = Setting(
        "Mode",
        Mode.PACKETS_QUEUED
    ) describedBy "When to send queued packets"

    // Packet queue flush settings
    private val queueLength = Setting(
        "QueueLength",
        50.0,
        1.0,
        1000.0,
        1.0
    ) describedBy "The size of the queue to start sending packets" subOf mode visibleWhen { mode.value == Mode.PACKETS_QUEUED }

    private var delay = Setting(
        "Delay",
        4.0,
        0.1,
        10.0,
        0.1
    ) describedBy "The delay between sending packets in seconds" subOf mode visibleWhen { mode.value == Mode.DELAY }

    private val distance = Setting(
        "Distance",
        10.0,
        1.0,
        100.0,
        0.1
    ) describedBy "The distance to the fake player to start sending packets" subOf mode visibleWhen { mode.value == Mode.DISTANCE }

    // Using CopyOnWriteArrayList to avoid ConcurrentModificationException
    private val packetQueue: MutableList<CPacketPlayer> = CopyOnWriteArrayList()
    private val timer = Timer()
    private var lastPosition: BlockPos? = null

    override fun onEnable() {
        if (minecraft.anyNull) {
            return
        }

        val fakePlayer = EntityOtherPlayerMP(minecraft.world, minecraft.player.gameProfile)
        fakePlayer.copyLocationAndAnglesFrom(minecraft.player)
        fakePlayer.rotationYawHead = minecraft.player.rotationYawHead
        fakePlayer.inventory.copyInventory(minecraft.player.inventory)
        fakePlayer.isSneaking = minecraft.player.isSneaking
        fakePlayer.primaryHand = minecraft.player.primaryHand
        minecraft.world.addEntityToWorld(-351352, fakePlayer)
        lastPosition = minecraft.player.position
    }

    override fun onDisable() {
        if (minecraft.anyNull) {
            return
        }

        sendPackets()
        minecraft.world.removeEntityFromWorld(-351352)
        lastPosition = null
    }

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        if (lastPosition == null) {
            lastPosition = minecraft.player.position
        }

        when (mode.value) {
            Mode.PACKETS_QUEUED -> if (packetQueue.size >= queueLength.value) {
                sendPackets()
            }

            Mode.DELAY -> if (timer.hasMSPassed(delay.value * 1000)) {
                sendPackets()
                timer.reset()
            }

            Mode.DISTANCE -> if (minecraft.player.getDistance(lastPosition!!.x.toDouble(), lastPosition!!.y.toDouble(), lastPosition!!.z.toDouble()) >= distance.value) {
                sendPackets()
            }

            else -> {}
        }
    }

    @Listener
    fun onPacketSent(event: PreSend) {
        if (minecraft.anyNull || event.packet !is CPacketPlayer) {
            return
        }

        event.cancel()
        packetQueue.add(event.packet)
    }

    private fun sendPackets() {
        lastPosition = minecraft.player.position
        minecraft.world.removeEntityFromWorld(-351352)

        if (packetQueue.isNotEmpty()) {
            packetQueue.forEach { minecraft.player.connection.sendPacket(it) }
            packetQueue.clear()
        }

        val fakePlayer = EntityOtherPlayerMP(minecraft.world, minecraft.player.gameProfile)
        fakePlayer.copyLocationAndAnglesFrom(minecraft.player)
        fakePlayer.rotationYawHead = minecraft.player.rotationYawHead
        fakePlayer.inventory.copyInventory(minecraft.player.inventory)
        fakePlayer.isSneaking = minecraft.player.isSneaking
        fakePlayer.primaryHand = minecraft.player.primaryHand
        minecraft.world.addEntityToWorld(-351352, fakePlayer)
    }

    enum class Mode {
        /**
         * Send queued packets after a certain amount of packets have been queued
         */
        PACKETS_QUEUED,

        /**
         * Send queued packets after you have reached a distance away from the fake player
         */
        DISTANCE,

        /**
         * Send queued packets after you have reached a certain amount of time
         */
        DELAY,

        /**
         * Manually send queued packets by toggling the module
         */
        MANUAL
    }

}