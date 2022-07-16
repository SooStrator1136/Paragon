package com.paragon.client.systems.module.impl.movement

import com.paragon.api.event.player.PlayerMoveEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.world.BlockUtil
import me.wolfsurge.cerauno.listener.Listener
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import java.awt.Color

/**
 * @author Wolfsurge
 */
object AntiVoidinq : Module("AntiVoidinq", Category.MOVEMENT, "Avoids void holes for you") {

    private val mode = Setting("Mode", Mode.MOTION)
        .setDescription("How to prevent falling through void holes")

    private var renderPosition: BlockPos? = null

    @Listener
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (minecraft.player.posY < 2.1 && minecraft.world.getBlockState(BlockPos(minecraft.player.posX, 0.0, minecraft.player.posZ)).material.isReplaceable) {
            var y = MathHelper.clamp(minecraft.player.posY, 0.0, Double.MAX_VALUE)

            while (y > 0.0 && minecraft.world.getBlockState(BlockPos(minecraft.player.posX, y, minecraft.player.posZ)).material.isReplaceable) {
                val pos = BlockPos(minecraft.player.posX, y, minecraft.player.posZ)

                // Intercepting block
                if (!minecraft.world.getBlockState(pos).material.isReplaceable) {
                    return
                }

                y--
            }

            when (mode.value) {
                Mode.MOTION -> {
                    event.y = 0.0624
                    minecraft.player.setVelocity(0.0, 0.0624, 0.0)

                    renderPosition = BlockPos(minecraft.player.posX, 0.0, minecraft.player.posZ)
                }

                Mode.LAGBACK -> {
                    minecraft.player.connection.sendPacket(CPacketPlayer.Position(minecraft.player.posX, minecraft.player.posY + 100, minecraft.player.posZ, minecraft.player.onGround))
                }
            }
        }

        else {
            renderPosition = null
        }
    }

    override fun onRender3D() {
        if (renderPosition != null) {
            RenderUtil.drawFilledBox(BlockUtil.getBlockBox(renderPosition!!), Color(200, 0, 0, 150))
        }
    }

    enum class Mode {
        /**
         * Adds a slight velocity onto the player's Y motion, could also cause rubberbands
         */
        MOTION,

        /**
         * Send invalid packet to cause lagback / rubberband
         */
        LAGBACK
    }

}