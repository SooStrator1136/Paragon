package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.render.tileentity.RenderTileEntityEvent;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.OutlineUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.world.BlockUtil;
import com.paragon.asm.mixins.accessor.IEntityRenderer;
import com.paragon.client.shader.shaders.DiagonalShader;
import com.paragon.client.shader.shaders.OutlineShader;
import com.paragon.client.shader.shaders.SmoothShader;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.ColourSetting;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

@SuppressWarnings("unchecked")
public class StorageESP extends Module {

    private final BooleanSetting chests = new BooleanSetting("Chests", "Highlight chests", true);
    private final BooleanSetting shulkers = new BooleanSetting("Shulkers", "Highlight shulker boxes", true);
    private final BooleanSetting enderChests = new BooleanSetting("Ender Chests", "Highlight Ender Chests", true);

    private final ModeSetting<Mode> mode = new ModeSetting<>("Mode", "How to highlight the block", Mode.SHADER);
    private final NumberSetting lineWidth = new NumberSetting("Line Width", "How thick to render the outlines", 1, 0.1f, 8, 0.1f);

    // Shader settings
    private final ModeSetting<FragShader> shader = (ModeSetting<FragShader>) new ModeSetting<>("Shader", "The shader to use", FragShader.OUTLINE)
            .setParentSetting(mode).setVisiblity(() -> mode.getCurrentMode().equals(Mode.SHADER));

    // Outline shader
    private final BooleanSetting outline = (BooleanSetting) new BooleanSetting("Outline", "Outline the fill", true)
            .setParentSetting(mode).setVisiblity(() -> mode.getCurrentMode().equals(Mode.SHADER) && shader.getCurrentMode().equals(FragShader.SMOOTH) || mode.getCurrentMode().equals(Mode.BOX));

    // Smooth shader
    private final ModeSetting<SmoothShaderColour> smoothShaderColour = (ModeSetting<SmoothShaderColour>) new ModeSetting<>("Smooth Colour", "The colour to use for the smooth shader", SmoothShaderColour.POSITION)
            .setParentSetting(mode).setVisiblity(() -> mode.getCurrentMode().equals(Mode.SHADER) && shader.getCurrentMode().equals(FragShader.SMOOTH));

    private final BooleanSetting fill = (BooleanSetting) new BooleanSetting("Fill", "Fill the outline", true)
            .setParentSetting(mode).setVisiblity(() -> mode.getCurrentMode().equals(Mode.SHADER) && shader.getCurrentMode().equals(FragShader.OUTLINE) || mode.getCurrentMode().equals(Mode.BOX));

    // Diagonal shader
    private final NumberSetting diagonalSpeed = (NumberSetting) new NumberSetting("Speed", "The speed at which the shader moves", 0.01f, 0.01f, 0.3f, 0.01f)
            .setParentSetting(mode).setVisiblity(() -> mode.getCurrentMode().equals(Mode.SHADER) && shader.getCurrentMode().equals(FragShader.DIAGONAL));

    // Colour setting
    private final ColourSetting colour = (ColourSetting) new ColourSetting("Colour", "The colour to highlight items in", new Color(185, 17, 255))
            .setVisiblity(() -> !mode.getCurrentMode().equals(Mode.SHADER) || shader.getCurrentMode().equals(FragShader.OUTLINE) || shader.getCurrentMode().equals(FragShader.DIAGONAL));

    private Framebuffer framebuffer;
    private float lastScaleFactor, lastScaleWidth, lastScaleHeight;

    // Shaders
    private final OutlineShader outlineShader = new OutlineShader();
    private final SmoothShader smoothShader = new SmoothShader();
    private final DiagonalShader diagonalShader = new DiagonalShader();

    public StorageESP() {
        super("StorageESP", ModuleCategory.RENDER, "Highlights storage blocks in the world");
        this.addSettings(chests, shulkers, enderChests, mode, lineWidth, colour);
    }

    @Override
    public void onRender3D() {
        if (mode.getCurrentMode().equals(Mode.BOX)) {
            mc.world.loadedTileEntityList.forEach(tileEntity -> {
               if (isStorageValid(tileEntity)) {
                   if (fill.isEnabled()) {
                       RenderUtil.drawFilledBox(BlockUtil.getBlockBox(tileEntity.getPos()), colour.getColour());
                   }

                   if (outline.isEnabled()) {
                       RenderUtil.drawBoundingBox(BlockUtil.getBlockBox(tileEntity.getPos()), lineWidth.getValue(), ColourUtil.integrateAlpha(colour.getColour(), 255));
                   }
               }
            });
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType().equals(RenderGameOverlayEvent.ElementType.HOTBAR) && mode.getCurrentMode().equals(Mode.SHADER)) {
            // Pretty much just taken from Cosmos, all credit goes to them (sorry linus!)
            // https://github.com/momentumdevelopment/cosmos/blob/main/src/main/java/cope/cosmos/client/features/modules/visual/ESPModule.java

            GlStateManager.enableAlpha();
            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();

            // Delete old framebuffer
            if (framebuffer != null) {
                framebuffer.framebufferClear();

                if (lastScaleFactor != event.getResolution().getScaleFactor()|| lastScaleWidth != event.getResolution().getScaledWidth() || lastScaleHeight != event.getResolution().getScaledHeight()) {
                    framebuffer.deleteFramebuffer();
                    framebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
                    framebuffer.framebufferClear();
                }

                lastScaleFactor = event.getResolution().getScaleFactor();
                lastScaleWidth = event.getResolution().getScaledWidth();
                lastScaleHeight = event.getResolution().getScaledHeight();
            } else {
                framebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
            }

            framebuffer.bindFramebuffer(false);
            boolean previousShadows = mc.gameSettings.entityShadows;
            mc.gameSettings.entityShadows = false;

            ((IEntityRenderer) mc.entityRenderer).setupCamera(event.getPartialTicks(), 0);

            for (TileEntity tileEntity : mc.world.loadedTileEntityList) {
                if (isStorageValid(tileEntity)) {
                    double x = mc.getRenderManager().viewerPosX;
                    double y = mc.getRenderManager().viewerPosY;
                    double z = mc.getRenderManager().viewerPosZ;

                    TileEntityRendererDispatcher.instance.render(tileEntity, tileEntity.getPos().getX() - x, tileEntity.getPos().getY() - y, tileEntity.getPos().getZ() - z, mc.getRenderPartialTicks());
                }
            }

            mc.gameSettings.entityShadows = previousShadows;
            GlStateManager.enableBlend();
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            framebuffer.unbindFramebuffer();
            mc.getFramebuffer().bindFramebuffer(true);
            mc.entityRenderer.disableLightmap();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.pushMatrix();

            // Render shaders
            switch (shader.getCurrentMode()) {
                case OUTLINE:
                    outlineShader.setColor(colour.getColour());
                    outlineShader.setWidth(lineWidth.getValue());
                    outlineShader.setFill(fill.isEnabled() ? 1 : 0);
                    outlineShader.startShader();
                    break;

                case SMOOTH:
                    smoothShader.setColor(smoothShaderColour.getCurrentMode().getType());
                    smoothShader.setWidth(lineWidth.getValue());
                    smoothShader.setOutline(outline.isEnabled() ? 1 : 0);
                    smoothShader.startShader();
                    break;

                case DIAGONAL:
                    diagonalShader.setColour(colour.getColour());
                    diagonalShader.setLineWidth(lineWidth.getValue());
                    diagonalShader.startShader();
                    diagonalShader.setTime(diagonalShader.getTime() + diagonalSpeed.getValue());
                    break;
            }

            mc.entityRenderer.setupOverlayRendering();

            glBindTexture(GL_TEXTURE_2D, framebuffer.framebufferTexture);
            glBegin(GL_QUADS);
            glTexCoord2d(0, 1);
            glVertex2d(0, 0);
            glTexCoord2d(0, 0);
            glVertex2d(0, event.getResolution().getScaledHeight());
            glTexCoord2d(1, 0);
            glVertex2d(event.getResolution().getScaledWidth(), event.getResolution().getScaledHeight());
            glTexCoord2d(1, 1);
            glVertex2d(event.getResolution().getScaledWidth(), 0);
            glEnd();

            // Stop drawing shader
            glUseProgram(0);
            glPopMatrix();

            mc.entityRenderer.enableLightmap();

            GlStateManager.popMatrix();
            GlStateManager.popAttrib();
        }
    }

    @Listener
    public void onTileEntityRender(RenderTileEntityEvent event) {
        if (mode.getCurrentMode().equals(Mode.OUTLINE) && isStorageValid(event.getTileEntityIn())) {
            TileEntity tileEntityIn = event.getTileEntityIn();
            float partialTicks = event.getPartialTicks();
            BlockPos blockpos = tileEntityIn.getPos();

            event.getTileEntityRendererDispatcher().render(tileEntityIn, (double)blockpos.getX() - event.getStaticPlayerX(), (double)blockpos.getY() - event.getStaticPlayerY(), (double)blockpos.getZ() - event.getStaticPlayerZ(), partialTicks);
            OutlineUtil.renderOne(lineWidth.getValue());
            event.getTileEntityRendererDispatcher().render(tileEntityIn, (double)blockpos.getX() - event.getStaticPlayerX(), (double)blockpos.getY() - event.getStaticPlayerY(), (double)blockpos.getZ() - event.getStaticPlayerZ(), partialTicks);
            OutlineUtil.renderTwo();
            event.getTileEntityRendererDispatcher().render(tileEntityIn, (double)blockpos.getX() - event.getStaticPlayerX(), (double)blockpos.getY() - event.getStaticPlayerY(), (double)blockpos.getZ() - event.getStaticPlayerZ(), partialTicks);
            OutlineUtil.renderThree();
            OutlineUtil.renderFour(colour.getColour());
            event.getTileEntityRendererDispatcher().render(tileEntityIn, (double)blockpos.getX() - event.getStaticPlayerX(), (double)blockpos.getY() - event.getStaticPlayerY(), (double)blockpos.getZ() - event.getStaticPlayerZ(), partialTicks);
            OutlineUtil.renderFive();
        }
    }

    public boolean isStorageValid(TileEntity tileEntity) {
        if (tileEntity instanceof TileEntityChest) {
            return chests.isEnabled();
        }

        if (tileEntity instanceof TileEntityShulkerBox) {
            return shulkers.isEnabled();
        }

        if (tileEntity instanceof TileEntityEnderChest) {
            return enderChests.isEnabled();
        }

        return true;
    }

    public enum Mode {
        /**
         * Draws a box around the storage block
         */
        BOX,

        /**
         * Uses a shader
         */
        SHADER,

        /**
         * Uses GL Stencil to outline the storage block
         */
        OUTLINE
    }

    public enum FragShader {
        /**
         * Outline the entity
         */
        OUTLINE,

        /**
         * Draw a smooth fill and optional outline with colour based on the entity's position on the screen
         */
        SMOOTH,

        /**
         * Some weird diagonal line thing
         */
        DIAGONAL
    }

    public enum SmoothShaderColour {
        /**
         * Sets colour based on entity's position on the screen
         */
        POSITION(1),

        /**
         * Sets colour based on player's yaw
         */
        YAW(2),

        /**
         * Sets colour based on player's pitch
         */
        PITCH(3);

        private int type;

        SmoothShaderColour(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }

}
