package com.paragon.client.systems.module.impl.movement;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.world.BlockUtil;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Scaffold extends Module {

    public static Setting<Double> length = new Setting<>("Length", 1D, 1D, 5D, 1D)
            .setDescription("The length of the scaffold");

    // Render settings
    public static Setting<Boolean> render = new Setting<>("Render", true)
            .setDescription("Render the placement");

    public static Setting<Render> renderMode = new Setting<>("Mode", Render.BOTH)
            .setDescription("How to render placement")
            .setParentSetting(render);

    public static Setting<Float> renderOutlineWidth = new Setting<>("OutlineWidth", 0.5f, 0.1f, 2f, 0.1f)
            .setDescription("The width of the lines")
            .setParentSetting(render);

    public static Setting<Color> renderColour = new Setting<>("FillColour", new Color(185, 19, 255, 130))
            .setDescription( "The colour of the fill")
            .setParentSetting(render);

    public static Setting<Color> renderOutlineColour = new Setting<>("OutlineColour", new Color(185, 19, 255))
            .setParentSetting(render);

    private final List<BlockPos> blocks = new CopyOnWriteArrayList<>();

    public Scaffold() {
        super("Scaffold", Category.MOVEMENT, "Places blocks beneath you");
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        blocks.clear();

        BlockPos originPosition = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ).down();
        BlockPos offsetPosition;

        EnumFacing offsetFacing;

        for (int i = 0; i < length.getValue(); i++) {
            offsetPosition = originPosition.offset(EnumFacing.fromAngle(mc.player.rotationYaw));
            blocks.add(offsetPosition);
        }
    }

    @Override
    public void onRender3D() {
        if (render.getValue()) {
            blocks.forEach(block -> {
                // Render fill
                if (renderMode.getValue().equals(Render.FILL) || renderMode.getValue().equals(Render.BOTH)) {
                    RenderUtil.drawFilledBox(BlockUtil.getBlockBox(block), renderColour.getValue());
                }

                // Render outline
                if (renderMode.getValue().equals(Render.OUTLINE) || renderMode.getValue().equals(Render.BOTH)) {
                    RenderUtil.drawBoundingBox(BlockUtil.getBlockBox(block), renderOutlineWidth.getValue(), renderOutlineColour.getValue());
                }
            });
        }
    }

    public enum Render {
        /**
         * Render outline
         */
        OUTLINE,

        /**
         * Render fill
         */
        FILL,

        /**
         * Render both
         */
        BOTH
    }
}
