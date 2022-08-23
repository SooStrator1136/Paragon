package com.paragon.client.systems.module.impl.render

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.render.ColourUtil.integrateAlpha
import com.paragon.api.util.render.builder.BoxRenderMode
import com.paragon.api.util.render.builder.RenderBuilder
import com.paragon.api.util.system.backgroundThread
import com.paragon.api.util.world.BlockUtil
import com.paragon.api.util.world.BlockUtil.getBlockAtPos
import com.paragon.api.util.world.BlockUtil.isSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.minecraft.block.BlockLiquid
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author SooStrator1136
 */
object SourceESP : Module("SourceESP", Category.RENDER, "Highlights liquid source blocks") {

    private val range = Setting(
        "Range",
        20F,
        5F,
        50F,
        1F
    ) describedBy "Range to search in"

    private val onlyTop = Setting(
        "OnlyTop",
        true
    )

    private val lavaColor = Setting(
        "Lava color",
        Color.RED.integrateAlpha(75F)
    ) describedBy "Color with which lava will be highlighted"
    private val waterColor = Setting(
        "Water color",
        Color.BLUE.integrateAlpha(75F)
    ) describedBy "Color with which water will be highlighted"

    //Might fuck up performance 🤷
    private val sources: MutableList<BlockPos> = CopyOnWriteArrayList()

    private var lastJob: Job? = null

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        backgroundThread {
            if (lastJob == null || lastJob!!.isCompleted) {
                lastJob = launch {
                    sources.addAll(BlockUtil.getSphere(range.value, true).filter {
                        !sources.contains(it) && it.isSource()
                    })

                    sources.removeIf {
                        if (onlyTop.value && !isTopSource(it)) {
                            return@removeIf true
                        }

                        return@removeIf !it.isSource()
                                || it.getDistance(
                            minecraft.player.posX.toInt(),
                            minecraft.player.posY.toInt(),
                            minecraft.player.posZ.toInt()
                        ) > range.value
                    }
                }
            }
        }
    }

    private fun isTopSource(pos: BlockPos) = pos.up().getBlockAtPos() !is BlockLiquid

    override fun onRender3D() {
        sources.forEach {
            RenderBuilder()
                .boundingBox(BlockUtil.getBlockBox(it))
                .inner(if (it.getBlockAtPos() == Blocks.WATER) waterColor.value else lavaColor.value)
                .outer(if (it.getBlockAtPos() == Blocks.WATER) waterColor.value.integrateAlpha(255f) else lavaColor.value.integrateAlpha(255f))
                .type(BoxRenderMode.BOTH)

                .start()

                .blend(true)
                .depth(true)
                .texture(true)
                .lineWidth(1f)

                .build(false)
        }
    }

    override fun onDisable() {
        lastJob = null
        sources.clear()
    }

}