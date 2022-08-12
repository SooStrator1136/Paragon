package com.paragon.client.systems.module.impl.render

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.render.ColourUtil.integrateAlpha
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.system.backgroundThread
import com.paragon.api.util.world.BlockUtil.getBlockAtPos
import com.paragon.api.util.world.BlockUtil.getBlockBox
import com.paragon.api.util.world.BlockUtil.getSphere
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author Surge
 */
object HoleESP : Module("HoleESP", Category.RENDER, "Highlights holes to stand in when crystalling") {

    // Hole filters and colours
    private val obsidian = Setting(
        "Obsidian",
        true
    ) describedBy "Highlight obsidian holes"

    private val obsidianColour = Setting(
        "Colour",
        Color.RED.integrateAlpha(130f)
    ) describedBy "Colour of obsidian holes" subOf obsidian

    private val mixed = Setting(
        "Mixed",
        true
    ) describedBy "Highlight mixed holes (holes that are a mix of obsidian and bedrock)"

    private val mixedColour = Setting(
        "Colour",
        Color.ORANGE.integrateAlpha(130f)
    ) describedBy "The colour for mixed holes" subOf (mixed)

    private val bedrock = Setting("Bedrock", true) describedBy "Highlight bedrock holes"

    private val bedrockColour = Setting(
        "Colour",
        Color.GREEN.integrateAlpha(130f)
    ) describedBy "The colour for bedrock holes" subOf bedrock

    private val range = Setting(
        "Range",
        5f,
        2f,
        20f,
        1f
    ) describedBy "The range to search for holes"

    // Render settings
    private val fill = Setting(
        "Fill",
        true
    ) describedBy "Fill the holes ;)"

    private val fillHeight = Setting(
        "Height",
        0f,
        0f,
        2f,
        0.01f
    ) describedBy "How tall the fill is" subOf fill

    private val outline = Setting(
        "Outline",
        true
    ) describedBy "Outline the hole"

    private val outlineWidth = Setting(
        "Width",
        1f,
        1f,
        3f,
        1f
    ) describedBy "The width of the outlines" subOf outline

    private val outlineHeight = Setting(
        "Height",
        0f,
        0f,
        2f,
        0.01f
    ) describedBy "How tall the outline is" subOf outline

    private val glow = Setting(
        "Gradient",
        true
    ) describedBy "Renders a glow effect above the box"

    private val glowHeight = Setting(
        "Height",
        1f,
        0f,
        2f,
        0.01f
    ) describedBy "How tall the glow is" subOf glow

    private val hideCurrent = Setting(
        "Hide Current",
        false
    ) describedBy "Doesn't render the hole if you are standing in it"

    // List of holes to render
    private val holes = CopyOnWriteArrayList<Hole>()

    private var lastJob: Job? = null

    override fun onEnable() {
        holes.clear()
    }

    override fun onDisable() {
        holes.clear()
    }

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        // Refresh holes list
        backgroundThread {
            if (lastJob == null || lastJob!!.isCompleted) {
                lastJob = launch {
                    holes.removeIf {
                        !(isSurroundedByBlock(it.holePosition, Blocks.OBSIDIAN) && obsidian.value) ||
                                !(isSurroundedByBlock(it.holePosition, Blocks.BEDROCK) && bedrock.value) ||
                                !(isHoleMixed(it.holePosition) && mixed.value) ||
                                it.holePosition.getDistance(
                                    minecraft.player.posX.toInt(),
                                    (minecraft.player.posY + minecraft.player.getEyeHeight()).toInt(),
                                    minecraft.player.posZ.toInt()
                                ) > range.value
                    }

                    getSphere(range.value, false).forEach {
                        // Hide it if it's the hole we are standing in
                        if (holes.any { hole -> hole.holePosition == it } || (hideCurrent.value && it == BlockPos(
                                minecraft.player.posX.toInt(),
                                minecraft.player.posY.toInt(),
                                minecraft.player.posZ.toInt()
                            ))
                        ) {
                            return@forEach
                        }

                        holes.add(
                            Hole(
                                it,
                                if (isSurroundedByBlock(it, Blocks.OBSIDIAN) && obsidian.value) HoleType.OBSIDIAN
                                else if (isSurroundedByBlock(it, Blocks.BEDROCK) && bedrock.value) HoleType.BEDROCK
                                else if (isHoleMixed(it) && mixed.value) HoleType.MIXED
                                else return@forEach
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onRender3D() {
        holes.forEach {
            val blockBB = getBlockBox(it.holePosition)
            if (fill.value) {
                RenderUtil.drawFilledBox(
                    AxisAlignedBB(
                        blockBB.minX,
                        blockBB.minY,
                        blockBB.minZ,
                        blockBB.maxX,
                        blockBB.minY + fillHeight.value,
                        blockBB.maxZ
                    ),
                    it.holeColour,
                )
            }
            if (outline.value) {
                RenderUtil.drawBoundingBox(
                    AxisAlignedBB(
                        blockBB.minX,
                        blockBB.minY,
                        blockBB.minZ,
                        blockBB.maxX,
                        blockBB.minY + outlineHeight.value,
                        blockBB.maxZ
                    ),
                    outlineWidth.value,
                    it.holeColour.integrateAlpha(255f)
                )
            }
            if (glow.value) {
                RenderUtil.drawGradientBox(
                    AxisAlignedBB(
                        blockBB.minX,
                        blockBB.minY,
                        blockBB.minZ,
                        blockBB.maxX,
                        blockBB.minY + glowHeight.value,
                        blockBB.maxZ
                    ), Color(0, 0, 0, 0), it.holeColour
                )
            }
        }
    }

    /**
     * It works... if this can be done in a better way, I'll be glad to hear it.
     *
     * @param pos The position to check
     * @return Whether the hole is mixed or not
     */
    private fun isHoleMixed(pos: BlockPos): Boolean {
        return (getBlockAtPos(pos.north()) === Blocks.OBSIDIAN || getBlockAtPos(pos.north()) === Blocks.BEDROCK) &&
                (getBlockAtPos(pos.west()) === Blocks.OBSIDIAN || getBlockAtPos(pos.west()) === Blocks.BEDROCK) &&
                (getBlockAtPos(pos.east()) === Blocks.OBSIDIAN || getBlockAtPos(pos.east()) === Blocks.BEDROCK) &&
                (getBlockAtPos(pos.south()) === Blocks.OBSIDIAN || getBlockAtPos(pos.south()) === Blocks.BEDROCK) && getBlockAtPos(
            pos
        ) === Blocks.AIR && getBlockAtPos(pos.up()) === Blocks.AIR && getBlockAtPos(
            pos.up().up()
        ) === Blocks.AIR && getBlockAtPos(pos.down()) !== Blocks.AIR
    }

    private fun isSurroundedByBlock(pos: BlockPos, blockCheck: Block): Boolean {
        return getBlockAtPos(pos) === Blocks.AIR && getBlockAtPos(pos.north()) === blockCheck && getBlockAtPos(pos.south()) === blockCheck && getBlockAtPos(
            pos.east()
        ) === blockCheck && getBlockAtPos(pos.west()) === blockCheck && getBlockAtPos(pos.up()) === Blocks.AIR && getBlockAtPos(
            pos.up().up()
        ) === Blocks.AIR && getBlockAtPos(pos.down()) !== Blocks.AIR
    }

    enum class HoleType {
        /**
         * A hole made of obsidian
         */
        OBSIDIAN,

        /**
         * A hole made of bedrock or obsidian
         */
        MIXED,

        /**
         * A hole made of bedrock
         */
        BEDROCK
    }

    class Hole(val holePosition: BlockPos, private val holeType: HoleType) {
        /**
         * Gets the colour of the hole
         *
         * @return The colour of the hole
         */
        val holeColour: Color
            get() {
                return when (holeType) {
                    HoleType.OBSIDIAN -> obsidianColour.value
                    HoleType.MIXED -> mixedColour.value
                    HoleType.BEDROCK -> bedrockColour.value
                }
            }

    }

}