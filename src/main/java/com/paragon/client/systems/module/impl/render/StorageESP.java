package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.render.tileentity.RenderTileEntityEvent;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.OutlineUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.string.StringUtil;
import com.paragon.api.util.world.BlockUtil;
import com.paragon.asm.mixins.accessor.IEntityRenderer;
import com.paragon.client.shader.shaders.OutlineShader;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import com.paragon.api.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

@SideOnly(Side.CLIENT)
public class StorageESP extends Module {

    public static StorageESP INSTANCE;

    public static Setting<Boolean> chests = new Setting<>("Chests", true)
            .setDescription("Highlight chests");

    public static Setting<Boolean> shulkers = new Setting<>("Shulkers", true)
            .setDescription("Highlight shulker boxes");

    public static Setting<Boolean> enderChests = new Setting<>("EnderChests", true)
            .setDescription("Highlight Ender Chests");

    // Render settings
    public static Setting<Mode> mode = new Setting<>("Mode", Mode.SHADER)
            .setDescription("How to render the entities");

    public static Setting<Float> lineWidth = new Setting<>("LineWidth", 1f, 0.1f, 8f, 0.1f)
            .setDescription("How thick to render the outlines");

    // Outline shader
    public static Setting<Boolean> outline = new Setting<>("Outline", true)
            .setDescription("Outline the fill")
            .setParentSetting(mode)
            .setVisibility(() -> mode.getValue().equals(Mode.SHADER));

    public static Setting<Boolean> fill = new Setting<>("Fill", true)
            .setDescription("Fill the outline")
            .setParentSetting(mode)
            .setVisibility(() -> mode.getValue().equals(Mode.SHADER));

    public static Setting<Color> colour = new Setting<>("Colour", new Color(185, 17, 255))
            .setDescription("The colour to highlight items in");

    // Shaders
    private final OutlineShader outlineShader = new OutlineShader();
    private Framebuffer framebuffer;
    private float lastScaleFactor, lastScaleWidth, lastScaleHeight;

    public StorageESP() {
        super("StorageESP", Category.RENDER, "Highlights storage blocks in the world");

        INSTANCE = this;
    }

    @Override
    public void onRender3D() {
        if (mode.getValue().equals(Mode.BOX)) {
            mc.world.loadedTileEntityList.forEach(tileEntity -> {
                if (isStorageValid(tileEntity)) {
                    if (fill.getValue()) {
                        RenderUtil.drawFilledBox(BlockUtil.getBlockBox(tileEntity.getPos()), colour.getValue());
                    }

                    if (outline.getValue()) {
                        RenderUtil.drawBoundingBox(BlockUtil.getBlockBox(tileEntity.getPos()), lineWidth.getValue(), ColourUtil.integrateAlpha(colour.getValue(), 255));
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType().equals(RenderGameOverlayEvent.ElementType.HOTBAR) && mode.getValue().equals(Mode.SHADER)) {
            // Pretty much just taken from Cosmos, all credit goes to them (sorry linus!)
            // https://github.com/momentumdevelopment/cosmos/blob/main/src/main/java/cope/cosmos/client/features/modules/visual/ESPModule.java

            GlStateManager.enableAlpha();
            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();

            // Delete old framebuffer
            if (framebuffer != null) {
                framebuffer.framebufferClear();

                if (lastScaleFactor != event.getResolution().getScaleFactor() || lastScaleWidth != event.getResolution().getScaledWidth() || lastScaleHeight != event.getResolution().getScaledHeight()) {
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

            // Render shader
            outlineShader.setColour(colour.getValue());
            outlineShader.setWidth(lineWidth.getValue());
            outlineShader.setFill(fill.getValue() ? 1 : 0);
            outlineShader.setOutline(outline.getValue() ? 1 : 0);
            outlineShader.startShader();

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

            mc.entityRenderer.setupOverlayRendering();
        }
    }

    @Listener
    public void onTileEntityRender(RenderTileEntityEvent event) {
        if (mode.getValue().equals(Mode.OUTLINE) && isStorageValid(event.getTileEntityIn())) {
            TileEntity tileEntityIn = event.getTileEntityIn();
            float partialTicks = event.getPartialTicks();
            BlockPos blockpos = tileEntityIn.getPos();

            event.getTileEntityRendererDispatcher().render(tileEntityIn, (double) blockpos.getX() - event.getStaticPlayerX(), (double) blockpos.getY() - event.getStaticPlayerY(), (double) blockpos.getZ() - event.getStaticPlayerZ(), partialTicks);
            OutlineUtil.renderOne(lineWidth.getValue());
            event.getTileEntityRendererDispatcher().render(tileEntityIn, (double) blockpos.getX() - event.getStaticPlayerX(), (double) blockpos.getY() - event.getStaticPlayerY(), (double) blockpos.getZ() - event.getStaticPlayerZ(), partialTicks);
            OutlineUtil.renderTwo();
            event.getTileEntityRendererDispatcher().render(tileEntityIn, (double) blockpos.getX() - event.getStaticPlayerX(), (double) blockpos.getY() - event.getStaticPlayerY(), (double) blockpos.getZ() - event.getStaticPlayerZ(), partialTicks);
            OutlineUtil.renderThree();
            OutlineUtil.renderFour(colour.getValue());
            event.getTileEntityRendererDispatcher().render(tileEntityIn, (double) blockpos.getX() - event.getStaticPlayerX(), (double) blockpos.getY() - event.getStaticPlayerY(), (double) blockpos.getZ() - event.getStaticPlayerZ(), partialTicks);
            OutlineUtil.renderFive();
        }
    }

    public boolean isStorageValid(TileEntity tileEntity) {
        if (tileEntity instanceof TileEntityChest) {
            return chests.getValue();
        }

        if (tileEntity instanceof TileEntityShulkerBox) {
            return shulkers.getValue();
        }

        if (tileEntity instanceof TileEntityEnderChest) {
            return enderChests.getValue();
        }

        return false;
    }

    @Override
    public String getData() {
        return StringUtil.getFormattedText(mode.getValue());
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

}
