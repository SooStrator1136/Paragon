package com.paragon.client.systems.module.impl.render;

import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.world.BlockUtil;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.ColourSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Wolfsurge
 */
public class HoleESP extends Module {

    // Hole filters and colours
    private final BooleanSetting obsidian = new BooleanSetting("Obsidian", "Highlight obsidian holes", true);
    private final ColourSetting obsidianColour = (ColourSetting) new ColourSetting("Colour", "The colour for obsidian holes", ColourUtil.integrateAlpha(Color.RED, 130)).setParentSetting(obsidian);

    private final BooleanSetting mixed = new BooleanSetting("Mixed", "Highlight mixed holes (holes that are a mix of obsidian and bedrock)", true);
    private final ColourSetting mixedColour = (ColourSetting) new ColourSetting("Colour", "The colour for mixed holes", ColourUtil.integrateAlpha(Color.ORANGE, 130)).setParentSetting(mixed);

    private final BooleanSetting bedrock = new BooleanSetting("Bedrock", "Highlight bedrock holes", true);
    private final ColourSetting bedrockColour = (ColourSetting) new ColourSetting("Colour", "The colour for bedrock holes", ColourUtil.integrateAlpha(Color.GREEN, 130)).setParentSetting(bedrock);

    private final NumberSetting range = new NumberSetting("Range", "The range to search for holes", 5, 2, 20, 1);

    // Render settings
    private final BooleanSetting fill = new BooleanSetting("Fill", "Fill the hole with a colour", true);
    private final NumberSetting fillHeight = (NumberSetting) new NumberSetting("Height", "How tall the fill is", 0, 0, 2, 0.01f).setParentSetting(fill);

    private final BooleanSetting outline = new BooleanSetting("Outline", "Outline the hole", true);
    private final NumberSetting outlineWidth = (NumberSetting) new NumberSetting("Width", "The width of the outlines", 1, 1, 3, 1).setParentSetting(outline);
    private final NumberSetting outlineHeight = (NumberSetting) new NumberSetting("Height", "How tall the outline is", 0, 0, 2, 0.01f).setParentSetting(outline);

    private final BooleanSetting glow = new BooleanSetting("Gradient", "Renders a glow effect above the box", true);
    private final NumberSetting glowHeight = (NumberSetting) new NumberSetting("Height", "How tall the glow is", 1, 0, 2, 0.01f).setParentSetting(glow);

    private final BooleanSetting hideCurrent = new BooleanSetting("Hide Current", "Doesn't render the hole if you are standing in it", false);

    // List of holes to render
    private final ArrayList<Hole> holes = new ArrayList<>();

    public HoleESP() {
        super("HoleESP", ModuleCategory.RENDER, "Highlights holes to stand in when crystalling");
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
            if (hideCurrent.isEnabled() && blockPos.equals(new BlockPos((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ))) {
                return;
            }

            if (isSurroundedByBlock(blockPos, Blocks.OBSIDIAN) && obsidian.isEnabled()) {
                holes.add(new Hole(blockPos, HoleType.OBSIDIAN));
            } else if (isSurroundedByBlock(blockPos, Blocks.BEDROCK) && bedrock.isEnabled()) {
                holes.add(new Hole(blockPos, HoleType.BEDROCK));
            } else if (isHoleMixed(blockPos) && mixed.isEnabled()) {
                holes.add(new Hole(blockPos, HoleType.MIXED));
            }
        });
    }

    @Override
    public void onRender3D() {
        holes.forEach(hole -> {
            AxisAlignedBB blockBB = BlockUtil.getBlockBox(hole.getHolePosition());

            if (fill.isEnabled()) {
                AxisAlignedBB fillBB = new AxisAlignedBB(blockBB.minX, blockBB.minY, blockBB.minZ, blockBB.maxX, blockBB.minY + fillHeight.getValue(), blockBB.maxZ);
                RenderUtil.drawFilledBox(fillBB, hole.getHoleColour());
            }

            if (outline.isEnabled()) {
                AxisAlignedBB outlineBB = new AxisAlignedBB(blockBB.minX, blockBB.minY, blockBB.minZ, blockBB.maxX, blockBB.minY + outlineHeight.getValue(), blockBB.maxZ);
                RenderUtil.drawBoundingBox(outlineBB, outlineWidth.getValue(), ColourUtil.integrateAlpha(hole.getHoleColour(), 255));
            }

            if (glow.isEnabled()) {
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
                    return obsidianColour.getColour();
                case MIXED:
                    return mixedColour.getColour();
                case BEDROCK:
                    return bedrockColour.getColour();
            }

            return Color.WHITE;
        }
    }
}
