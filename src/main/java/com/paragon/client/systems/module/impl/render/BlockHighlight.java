package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.render.world.BlockHighlightEvent;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.world.BlockUtil;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.ColourSetting;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;

import java.awt.*;

public class BlockHighlight extends Module {

    private ModeSetting<RenderMode> renderMode = new ModeSetting<>("Render Mode", "How to highlight the block", RenderMode.BOTH);
    private NumberSetting lineWidth = (NumberSetting) new NumberSetting("Line Width", "The width of the outline", 1, 0.1f, 1.5f, 0.1f).setVisiblity(() -> renderMode.getCurrentMode() == RenderMode.OUTLINE || renderMode.getCurrentMode() == RenderMode.BOTH);
    private ColourSetting colour = new ColourSetting("Colour", "What colour to render the block", new Color(185, 19, 211));

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
            if (renderMode.getCurrentMode() == RenderMode.FILL || renderMode.getCurrentMode() == RenderMode.BOTH) {
                RenderUtil.drawFilledBox(bb, ColourUtil.integrateAlpha(colour.getColour(), 180));
            }

            // Draw outline
            if (renderMode.getCurrentMode() == RenderMode.OUTLINE || renderMode.getCurrentMode() == RenderMode.BOTH) {
                RenderUtil.drawBoundingBox(bb, lineWidth.getValue(), colour.getColour());
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
