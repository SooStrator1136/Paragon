package com.paragon.impl.module.combat

import com.paragon.impl.event.network.PacketEvent.PreSend
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category
import com.paragon.util.calculations.Timer
import com.paragon.util.player.InventoryUtil.isHolding
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerDigging
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

object BowBomb : Module("BowBomb", Category.COMBAT, "Makes bows speedy bois") {

    private val ticks = Setting("Ticks", 10f, 1f, 50f, 1f) describedBy "ticks? i don't know"

    private val projectileTimer = Timer()

    @Listener
    fun onPacketSend(event: PreSend) {
        if (event.packet is CPacketPlayerDigging && event.packet.action == CPacketPlayerDigging.Action.RELEASE_USE_ITEM) {
            if (isHolding(Items.BOW) && projectileTimer.hasMSPassed(5000.0)) {
                minecraft.player.connection.sendPacket(
                    CPacketEntityAction(
                        minecraft.player, CPacketEntityAction.Action.START_SPRINTING
                    )
                )
                minecraft.player.connection.sendPacket(
                    CPacketEntityAction(
                        minecraft.player, CPacketEntityAction.Action.START_SPRINTING
                    )
                )

                val projectileRandom = Random()

                for (tick in 0 until ticks.value.toInt()) {
                    val sin = -sin(Math.toRadians(minecraft.player.rotationYaw.toDouble()))
                    val cos = cos(Math.toRadians(minecraft.player.rotationYaw.toDouble()))

                    if (projectileRandom.nextBoolean()) {
                        minecraft.player.connection.sendPacket(
                            CPacketPlayer.Position(
                                minecraft.player.posX + sin * 100, minecraft.player.posY + 5, minecraft.player.posZ + cos * 100, false
                            )
                        )
                        minecraft.player.connection.sendPacket(
                            CPacketPlayer.Position(
                                minecraft.player.posX - sin * 100, minecraft.player.posY, minecraft.player.posZ - cos * 100, true
                            )
                        )
                    }
                    else {
                        minecraft.player.connection.sendPacket(
                            CPacketPlayer.Position(
                                minecraft.player.posX - sin * 100, minecraft.player.posY, minecraft.player.posZ - cos * 100, true
                            )
                        )
                        minecraft.player.connection.sendPacket(
                            CPacketPlayer.Position(
                                minecraft.player.posX + sin * 100, minecraft.player.posY + 5, minecraft.player.posZ + cos * 100, false
                            )
                        )
                    }

                    projectileTimer.reset()
                }
            }
        }
    }

}