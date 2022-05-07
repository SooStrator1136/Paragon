package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.render.world.BlockHighlightEvent;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.world.BlockUtil;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;

import java.awt.*;

/**
 * @author Wolfsurge
 */
public class BlockHighlight extends Module {

    private final Setting<RenderMode> renderMode = new Setting<>("Render Mode", RenderMode.BOTH)
            .setDescription("How to highlight the block");

    private final Setting<Float> lineWidth = new Setting<>("Line Width", 1f, 0.1f, 1.5f, 0.1f)
            .setDescription("The width of the outline")
            .setVisibility(() -> !renderMode.getValue().equals(RenderMode.FILL));

    private final Setting<Color> colour = new Setting<>("Colour", new Color(185, 19, 211))
            .setDescription("What colour to render the block");

    public BlockHighlight() {
        super("BlockHighlight", ModuleCategory.RENDER, "Highlights the block you are looking at");
        this.addSettings(renderMode, lineWidth, colour);
    }

    @Listener
    public void onBlockHighlight(BlockHighlightEvent event) {
        event.cancel();
    }

    @Override
    public void onRender3D() {
        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
            // Get bounding box
            AxisAlignedBB bb = BlockUtil.getBlockBox(mc.objectMouseOver.getBlockPos());

            // Draw fill
            if (!renderMode.getValue().equals(RenderMode.OUTLINE)) {
                RenderUtil.drawFilledBox(bb, ColourUtil.integrateAlpha(colour.getValue(), 180));
            }

            // Draw outline
            if (!renderMode.getValue().equals(RenderMode.FILL)) {
                RenderUtil.drawBoundingBox(bb, lineWidth.getValue(), colour.getValue());
            }
        }
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
        BOTH
    }
}
