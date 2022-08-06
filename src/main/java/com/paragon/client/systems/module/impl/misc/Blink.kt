package com.paragon.client.systems.module.impl.misc

import com.paragon.api.event.network.PacketEvent.PreSend
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.Wrapper
import com.paragon.api.util.calculations.Timer
import me.wolfsurge.cerauno.listener.Listener
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Consumer

/**
 * @author Surge
 */
@SideOnly(Side.CLIENT)
object Blink : Module("Blink", Category.MISC, "Cancels sending packets for a length of time") {

    // General
    var mode = Setting<Mode?>("Mode", Mode.PACKETS_QUEUED)
        .setDescription("When to send queued packets")

    // Packet queue flush settings
    var queueLength = Setting("QueueLength", 50.0, 1.0, 1000.0, 1.0)
        .setDescription("The size of the queue to start sending packets")
        .setParentSetting(mode)
        .setVisibility { mode.value == Mode.PACKETS_QUEUED }
    
    var delay = Setting("Delay", 4.0, 0.1, 10.0, 0.1)
        .setDescription("The delay between sending packets in seconds")
        .setParentSetting(mode)
        .setVisibility { mode.value == Mode.DELAY }
    
    var distance = Setting("Distance", 10.0, 1.0, 100.0, 0.1)
        .setDescription("The distance to the fake player to start sending packets")
        .setParentSetting(mode)
        .setVisibility { mode.value == Mode.DISTANCE }
    
    // Using CopyOnWriteArrayList to avoid ConcurrentModificationException
    private val packetQueue: MutableList<CPacketPlayer> = CopyOnWriteArrayList()
    private val timer = Timer()
    private var lastPosition: BlockPos? = null

    override fun onEnable() {
        if (nullCheck()) {
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
        if (nullCheck()) {
            return
        }
        sendPackets()
        minecraft.world.removeEntityFromWorld(-351352)
        lastPosition = null
    }

    override fun onTick() {
        if (nullCheck()) {
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
        if (nullCheck()) {
            return
        }
        if (event.packet is CPacketPlayer) {
            event.cancel()
            packetQueue.add(event.packet)
        }
    }

    fun sendPackets() {
        lastPosition = minecraft.player.position
        minecraft.world.removeEntityFromWorld(-351352)
        if (!packetQueue.isEmpty()) {
            packetQueue.forEach(Consumer { packet: CPacketPlayer? -> minecraft.player.connection.sendPacket(packet) })
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