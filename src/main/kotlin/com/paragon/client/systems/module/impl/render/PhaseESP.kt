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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author SooStrator1136
 */
object PhaseESP : Module("PhaseESP", Category.RENDER, "Highlights phased players") {

    private val range = Setting(
        "Range",
        20F,
        5F,
        50F,
        1F
    ) describedBy "Range in which to look for phasing players"

    private val phaseColor = Setting(
        "Color",
        Color.ORANGE.integrateAlpha(100F)
    ) describedBy "Color of the phase indicator"

    private val self = Setting(
        "Self",
        false
    ) describedBy "Highlight yourself when phasing"

    //Ikik but blocking is even worse (I actually tested)
    private val phased: MutableList<EntityPlayer> = CopyOnWriteArrayList()

    private var lastJob: Job? = null

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        backgroundThread {
            if (lastJob == null || lastJob!!.isCompleted) {
                lastJob = launch {
                    phased.addAll(minecraft.world.playerEntities.filter {
                        if (it.uniqueID == minecraft.player.uniqueID && !self.value) {
                            return@filter false
                        }

                        !phased.contains(it)
                                && BlockPos(it.posX, it.posY, it.posZ).getBlockAtPos() != Blocks.AIR
                                && it.getDistance(minecraft.player) <= range.value
                    })

                    phased.removeIf {
                        it.getDistance(minecraft.player) > range.value
                                || BlockPos(it.posX, it.posY, it.posZ).getBlockAtPos() == Blocks.AIR
                                || it.uniqueID == minecraft.player.uniqueID && !self.value
                    }
                }
            }
        }
    }

    override fun onRender3D() {
        phased.forEach {
            RenderBuilder()
                .boundingBox(BlockUtil.getBlockBox(BlockPos(it.posX, it.posY, it.posZ)))
                .inner(phaseColor.value)
                .outer(phaseColor.value.integrateAlpha(255f))
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
        phased.clear()
    }

}