package com.paragon.client.systems.module.impl.render

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.ColourUtil
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.world.BlockUtil.getBlockAtPos
import com.paragon.api.util.world.BlockUtil.getBlockBox
import com.paragon.api.util.world.BlockUtil.getSphere
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import java.awt.Color
import java.util.function.Consumer

/**
 * @author Surge
 */
class HoleESP : Module("HoleESP", Category.RENDER, "Highlights holes to stand in when crystalling") {

    // Hole filters and colours
    private var obsidian = Setting("Obsidian", true)
        .setDescription("Highlight obsidian holes")

    private var obsidianColour = Setting("Colour", ColourUtil.integrateAlpha(Color.RED, 130f))
        .setDescription("Colour of obsidian holes")
        .setParentSetting(obsidian)

    private var mixed = Setting("Mixed", true)
        .setDescription("Highlight mixed holes (holes that are a mix of obsidian and bedrock)")

    private var mixedColour = Setting("Colour", ColourUtil.integrateAlpha(Color.ORANGE, 130f))
        .setDescription("The colour for mixed holes")
        .setParentSetting(mixed)

    private var bedrock = Setting("Bedrock", true)
        .setDescription("Highlight bedrock holes")

    private var bedrockColour = Setting("Colour", ColourUtil.integrateAlpha(Color.GREEN, 130f))
        .setDescription("The colour for bedrock holes")
        .setParentSetting(bedrock)

    private var range = Setting("Range", 5f, 2f, 20f, 1f)
        .setDescription("The range to search for holes")

    // Render settings
    private var fill = Setting("Fill", true)
        .setDescription("Fill the holes ;)")

    private var fillHeight = Setting("Height", 0f, 0f, 2f, 0.01f)
        .setDescription("How tall the fill is")
        .setParentSetting(fill)

    private var outline = Setting("Outline", true)
        .setDescription("Outline the hole")

    private var outlineWidth = Setting("Width", 1f, 1f, 3f, 1f)
        .setDescription("The width of the outlines")
        .setParentSetting(outline)

    private var outlineHeight = Setting("Height", 0f, 0f, 2f, 0.01f)
        .setDescription("How tall the outline is")
        .setParentSetting(outline)

    private var glow = Setting("Gradient", true)
        .setDescription("Renders a glow effect above the box")

    private var glowHeight = Setting("Height", 1f, 0f, 2f, 0.01f)
        .setDescription("How tall the glow is")
        .setParentSetting(glow)

    private var hideCurrent = Setting("Hide Current", false)
        .setDescription("Doesn't render the hole if you are standing in it")

    // List of holes to render
    private val holes = ArrayList<Hole>()
    override fun onEnable() {
        holes.clear()
    }

    override fun onDisable() {
        holes.clear()
    }

    override fun onTick() {
        if (nullCheck()) {
            return
        }

        // Refresh holes list
        holes.clear()

        getSphere(range.value, false).forEach(Consumer { blockPos: BlockPos ->
            // Hide it if it's the hole we are standing in
            if (hideCurrent.value && blockPos == BlockPos(minecraft.player.posX.toInt(), minecraft.player.posY.toInt(), minecraft.player.posZ.toInt())) {
                return@Consumer
            }

            if (isSurroundedByBlock(blockPos, Blocks.OBSIDIAN) && obsidian.value) {
                holes.add(Hole(blockPos, HoleType.OBSIDIAN))
            } else if (isSurroundedByBlock(blockPos, Blocks.BEDROCK) && bedrock.value) {
                holes.add(Hole(blockPos, HoleType.BEDROCK))
            } else if (isHoleMixed(blockPos) && mixed.value) {
                holes.add(Hole(blockPos, HoleType.MIXED))
            }
        })
    }

    override fun onRender3D() {
        holes.forEach { hole ->
            val blockBB = getBlockBox(hole.holePosition)
            if (fill.value) {
                val fillBB = AxisAlignedBB(
                    blockBB.minX,
                    blockBB.minY,
                    blockBB.minZ,
                    blockBB.maxX,
                    blockBB.minY + fillHeight.value,
                    blockBB.maxZ
                )
                RenderUtil.drawFilledBox(fillBB, hole.holeColour,)
            }
            if (outline.value) {
                val outlineBB = AxisAlignedBB(
                    blockBB.minX,
                    blockBB.minY,
                    blockBB.minZ,
                    blockBB.maxX,
                    blockBB.minY + outlineHeight.value,
                    blockBB.maxZ
                )
                RenderUtil.drawBoundingBox(
                    outlineBB,
                    outlineWidth.value,
                    ColourUtil.integrateAlpha(hole.holeColour, 255f)
                )
            }
            if (glow.value) {
                val glowBB = AxisAlignedBB(
                    blockBB.minX,
                    blockBB.minY,
                    blockBB.minZ,
                    blockBB.maxX,
                    blockBB.minY + glowHeight.value,
                    blockBB.maxZ
                )
                RenderUtil.drawGradientBox(glowBB, Color(0, 0, 0, 0), hole.holeColour)
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

    inner class Hole(
        /**
         * Gets the hole's position
         *
         * @return The hole's position
         */
        val holePosition: BlockPos, private val holeType: HoleType
    ) {
        /**
         * Gets the colour of the hole
         *
         * @return The colour of the hole
         */
        val holeColour: Color
            get() {
                return when (holeType) {
                    HoleType.OBSIDIAN -> ColourUtil.integrateAlpha(obsidianColour.value, obsidianColour.alpha)
                    HoleType.MIXED -> ColourUtil.integrateAlpha(mixedColour.value, mixedColour.alpha)
                    HoleType.BEDROCK -> ColourUtil.integrateAlpha(bedrockColour.value, bedrockColour.alpha)
                }
            }

    }

}