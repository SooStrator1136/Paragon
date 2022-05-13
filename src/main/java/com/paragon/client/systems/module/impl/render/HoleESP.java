package com.paragon.client.systems.module.impl.render;

import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.world.BlockUtil;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author Wolfsurge
 */
public class HoleESP extends Module {

    // Hole filters and colours
    private final Setting<Boolean> obsidian = new Setting<>("Obsidian", true)
            .setDescription("Highlight obsidian holes");

    private final Setting<Color> obsidianColour = new Setting<>("Colour", ColourUtil.integrateAlpha(Color.RED, 130))
            .setDescription("Colour of obsidian holes")
            .setParentSetting(obsidian);

    private final Setting<Boolean> mixed = new Setting<>("Mixed", true)
            .setDescription("Highlight mixed holes (holes that are a mix of obsidian and bedrock)");

    private final Setting<Color> mixedColour = new Setting<>("Colour", ColourUtil.integrateAlpha(Color.ORANGE, 130))
            .setDescription("The colour for mixed holes")
            .setParentSetting(mixed);

    private final Setting<Boolean> bedrock = new Setting<>("Bedrock", true)
            .setDescription("Highlight bedrock holes");

    private final Setting<Color> bedrockColour = new Setting<>("Colour", ColourUtil.integrateAlpha(Color.GREEN, 130))
            .setDescription("The colour for bedrock holes")
            .setParentSetting(bedrock);

    private final Setting<Float> range = new Setting<>("Range", 5f, 2f, 20f, 1f)
            .setDescription("The range to search for holes");

    // Render settings
    private final Setting<Boolean> fill = new Setting<>("Fill", true)
            .setDescription("Fill the holes ;)");

    private final Setting<Float> fillHeight = new Setting<>("Height", 0f, 0f, 2f, 0.01f)
            .setDescription("How tall the fill is")
            .setParentSetting(fill);

    private final Setting<Boolean> outline = new Setting<>("Outline", true)
            .setDescription("Outline the hole");

    private final Setting<Float> outlineWidth = new Setting<>("Width", 1f, 1f, 3f, 1f)
            .setDescription("The width of the outlines")
            .setParentSetting(outline);

    private final Setting<Float> outlineHeight = new Setting<>("Height", 0f, 0f, 2f, 0.01f)
            .setDescription("How tall the outline is")
            .setParentSetting(outline);

    private final Setting<Boolean> glow = new Setting<>("Gradient", true)
            .setDescription("Renders a glow effect above the box");

    private final Setting<Float> glowHeight = new Setting<>("Height", 1f, 0f, 2f, 0.01f)
            .setDescription("How tall the glow is")
            .setParentSetting(glow);

    private final Setting<Boolean> hideCurrent = new Setting<>("Hide Current", false)
            .setDescription("Doesn't render the hole if you are standing in it");

    // List of holes to render
    private final ArrayList<Hole> holes = new ArrayList<>();

    public HoleESP() {
        super("HoleESP", Category.RENDER, "Highlights holes to stand in when crystalling");
        this.addSettings(obsidian, mixed, bedrock, range, fill, outline, glow, hideCurrent);
    }

    @Override
    public void onEnable() {
        holes.clear();
    }

    @Override
    public void onDisable() {
        holes.clear();
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        // Refresh holes list
        holes.clear();

        BlockUtil.getSphere(range.getValue(), false).forEach(blockPos -> {
            // Hide it if it's the hole we are standing in
            if (hideCurrent.getValue() && blockPos.equals(new BlockPos((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ))) {
                return;
            }

            if (isSurroundedByBlock(blockPos, Blocks.OBSIDIAN) && obsidian.getValue()) {
                holes.add(new Hole(blockPos, HoleType.OBSIDIAN));
            } else if (isSurroundedByBlock(blockPos, Blocks.BEDROCK) && bedrock.getValue()) {
                holes.add(new Hole(blockPos, HoleType.BEDROCK));
            } else if (isHoleMixed(blockPos) && mixed.getValue()) {
                holes.add(new Hole(blockPos, HoleType.MIXED));
            }
        });
    }

    @Override
    public void onRender3D() {
        holes.forEach(hole -> {
            AxisAlignedBB blockBB = BlockUtil.getBlockBox(hole.getHolePosition());

            if (fill.getValue()) {
                AxisAlignedBB fillBB = new AxisAlignedBB(blockBB.minX, blockBB.minY, blockBB.minZ, blockBB.maxX, blockBB.minY + fillHeight.getValue(), blockBB.maxZ);
                RenderUtil.drawFilledBox(fillBB, hole.getHoleColour());
            }

            if (outline.getValue()) {
                AxisAlignedBB outlineBB = new AxisAlignedBB(blockBB.minX, blockBB.minY, blockBB.minZ, blockBB.maxX, blockBB.minY + outlineHeight.getValue(), blockBB.maxZ);
                RenderUtil.drawBoundingBox(outlineBB, outlineWidth.getValue(), ColourUtil.integrateAlpha(hole.getHoleColour(), 255));
            }

            if (glow.getValue()) {
                AxisAlignedBB glowBB = new AxisAlignedBB(blockBB.minX, blockBB.minY, blockBB.minZ, blockBB.maxX, blockBB.minY + glowHeight.getValue(), blockBB.maxZ);
                RenderUtil.drawGradientBox(glowBB, new Color(0, 0, 0, 0), hole.getHoleColour());
            }
        });
    }

    /**
     * It works... if this can be done in a better way, I'll be glad to hear it.
     * @param pos The position to check
     * @return Whether the hole is mixed or not
     */
    public boolean isHoleMixed(BlockPos pos) {
        return (BlockUtil.getBlockAtPos(pos.north()) == Blocks.OBSIDIAN || BlockUtil.getBlockAtPos(pos.north()) == Blocks.BEDROCK) &&
                (BlockUtil.getBlockAtPos(pos.west()) == Blocks.OBSIDIAN || BlockUtil.getBlockAtPos(pos.west()) == Blocks.BEDROCK) &&
                (BlockUtil.getBlockAtPos(pos.east()) == Blocks.OBSIDIAN || BlockUtil.getBlockAtPos(pos.east()) == Blocks.BEDROCK) &&
                (BlockUtil.getBlockAtPos(pos.south()) == Blocks.OBSIDIAN || BlockUtil.getBlockAtPos(pos.south()) == Blocks.BEDROCK) &&
                BlockUtil.getBlockAtPos(pos) == Blocks.AIR && BlockUtil.getBlockAtPos(pos.up()) == Blocks.AIR && BlockUtil.getBlockAtPos(pos.up().up()) == Blocks.AIR && BlockUtil.getBlockAtPos(pos.down()) != Blocks.AIR;
    }

    public boolean isSurroundedByBlock(BlockPos pos, Block blockCheck) {
        return BlockUtil.getBlockAtPos(pos) == Blocks.AIR && BlockUtil.getBlockAtPos(pos.north()) == blockCheck && BlockUtil.getBlockAtPos(pos.south()) == blockCheck && BlockUtil.getBlockAtPos(pos.east()) == blockCheck && BlockUtil.getBlockAtPos(pos.west()) == blockCheck
                && BlockUtil.getBlockAtPos(pos.up()) == Blocks.AIR && BlockUtil.getBlockAtPos(pos.up().up()) == Blocks.AIR && BlockUtil.getBlockAtPos(pos.down()) != Blocks.AIR;
    }

    public enum HoleType {
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

    public class Hole {
        private final BlockPos holePosition;
        private final HoleType holeType;

        public Hole(BlockPos holePosition, HoleType type) {
            this.holePosition = holePosition;
            this.holeType = type;
        }

        /**
         * Gets the hole's position
         *
         * @return The hole's position
         */
        public BlockPos getHolePosition() {
            return holePosition;
        }

        /**
         * Gets the colour of the hole
         *
         * @return The colour of the hole
         */
        public Color getHoleColour() {
            switch (holeType) {
                case OBSIDIAN:
                    return obsidianColour.getValue();
                case MIXED:
                    return mixedColour.getValue();
                case BEDROCK:
                    return bedrockColour.getValue();
            }

            return Color.WHITE;
        }
    }
}
