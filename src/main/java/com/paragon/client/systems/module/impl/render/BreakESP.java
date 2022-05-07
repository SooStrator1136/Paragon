package com.paragon.client.systems.module.impl.render;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.world.BlockUtil;
import com.paragon.asm.mixins.accessor.IRenderGlobal;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

/**
 * @author Wolfsurge
 */
public class BreakESP extends Module {

    // Render settings
    private final Setting<RenderMode> renderMode = new Setting<>("Render Mode", RenderMode.BOTH)
            .setDescription("How to render the highlight");

    private final Setting<Float> lineWidth = new Setting<>("Line Width", 1.0f, 0.1f, 3f, 0.1f)
            .setDescription("The width of the outline")
            .setVisibility(() -> !renderMode.getValue().equals(RenderMode.FILL));

    // Other settings
    private final Setting<Float> range = new Setting<>("Range", 20f, 1f, 50f, 1f)
            .setDescription("The maximum distance a highlighted block can be");

    private final Setting<Boolean> percent = new Setting<>("Percent", true)
            .setDescription("Show the percentage of how much the block has been broken");

    public BreakESP() {
        super("BreakESP", ModuleCategory.RENDER, "Highlights blocks that are currently being broken");
        this.addSettings(renderMode, lineWidth, range, percent);
    }

    @Override
    public void onRender3D() {
        // Iterate through all blocks being broken
        ((IRenderGlobal) mc.renderGlobal).getDamagedBlocks().forEach((pos, progress) -> {
            if (progress != null) {
                // Get the block being broken
                BlockPos blockPos = progress.getPosition();

                // Don't care about air
                if (BlockUtil.getBlockAtPos(blockPos) == Blocks.AIR) {
                    return;
                }

                // Check block is within range
                if (blockPos.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ) <= range.getValue()) {
                    // Block damage. Clamping this as it can go above 8 for other players, breaking the colour and throwing an exception
                    int damage = MathHelper.clamp(progress.getPartialBlockDamage(), 0, 8);

                    // Block bounding box
                    AxisAlignedBB bb = BlockUtil.getBlockBox(blockPos);

                    // Render values
                    double x = bb.minX + (bb.maxX - bb.minX) / 2;
                    double y = bb.minY + (bb.maxY - bb.minY) / 2;
                    double z = bb.minZ + (bb.maxZ - bb.minZ) / 2;

                    double sizeX = damage * ((bb.maxX - x) / 8);
                    double sizeY = damage * ((bb.maxY - y) / 8);
                    double sizeZ = damage * ((bb.maxZ - z) / 8);

                    // The bounding box we will highlight
                    AxisAlignedBB highlightBB = new AxisAlignedBB(x - sizeX, y - sizeY, z - sizeZ, x + sizeX, y + sizeY, z + sizeZ);

                    // The colour factor (for a transition between red and green (looks cool))
                    int colour = damage * 255 / 8;

                    // Draw the highlight
                    switch (renderMode.getValue()) {
                        case FILL:
                            RenderUtil.drawFilledBox(highlightBB, new Color(255 - colour, colour, 0, 150));
                            break;

                        case OUTLINE:
                            RenderUtil.drawBoundingBox(highlightBB, lineWidth.getValue(), new Color(255 - colour, colour, 0, 255));
                            break;

                        case BOTH:
                            RenderUtil.drawFilledBox(highlightBB, new Color(255 - colour, colour, 0, 150));
                            RenderUtil.drawBoundingBox(highlightBB, lineWidth.getValue(), new Color(255 - colour, colour, 0, 255));
                            break;
                    }

                    // Draw the percentage
                    if (percent.getValue()) {
                        RenderUtil.drawNametagText(damage * 100 / 8 + "%", new Vec3d(blockPos.getX() + 0.5f, blockPos.getY() + 0.5f, blockPos.getZ() + 0.5f), -1);
                    }
                }
            }
        });
    }

    public enum RenderMode {
        /**
         * Fill the block
         */
        FILL,

        /**
         * Outline the block
         */
        OUTLINE,

        /**
         * Fill and outline the block
         */
        BOTH,

        /**
         * No render
         */
        NONE
    }
}
