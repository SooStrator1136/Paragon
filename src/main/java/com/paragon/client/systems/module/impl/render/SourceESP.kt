package com.paragon.client.systems.module.impl.render

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.render.ColourUtil.integrateAlpha
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.system.backgroundThread
import com.paragon.api.util.world.BlockUtil
import net.minecraft.block.BlockLiquid
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author SooStrator1136
 */
object SourceESP : Module("SourceESP", Category.RENDER, "Highlights liquid source blocks") {

    private val range = Setting("Range", 20F, 5F, 50F, 1F) describedBy "Range to search in"

    private val lavaColor = Setting("Lava color", Color.RED.integrateAlpha(75F))
    private val waterColor = Setting("Water color", Color.BLUE.integrateAlpha(75F))

    //Might fuck up performance 🤷
    private val sources: MutableList<BlockPos> = CopyOnWriteArrayList()

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        backgroundThread {
            sources.addAll(BlockUtil.getSphere(range.value, true).filter {
                !sources.contains(it)
                        && BlockUtil.getBlockAtPos(it) is BlockLiquid
                        && minecraft.world.getBlockState(it).getValue(BlockLiquid.LEVEL) == 0
            })

            sources.removeIf {
                BlockUtil.getBlockAtPos(it) !is BlockLiquid
                        || minecraft.world.getBlockState(it).getValue(BlockLiquid.LEVEL) != 0
                        || it.getDistance(
                    minecraft.player.posX.toInt(),
                    minecraft.player.posY.toInt(),
                    minecraft.player.posZ.toInt()
                ) > range.value
            }
        }
    }

    override fun onRender3D() {
        sources.forEach {
            RenderUtil.drawFilledBox(
                BlockUtil.getBlockBox(it),
                if (BlockUtil.getBlockAtPos(it) == Blocks.WATER) waterColor.value else lavaColor.value
            )
        }
    }

    override fun onDisable() {
        sources.clear()
    }

}