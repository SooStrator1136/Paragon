package com.paragon.client.systems.module.impl.render;

import com.paragon.api.util.string.EnumFormatter;
import com.paragon.asm.mixins.accessor.IEntityRenderer;
import com.paragon.client.shader.shaders.DiagonalShader;
import com.paragon.client.shader.shaders.DiamondsShader;
import com.paragon.client.shader.shaders.FluidShader;
import com.paragon.client.shader.shaders.OutlineShader;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.ColourSetting;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class Shader extends Module {

    private final BooleanSetting passive = new BooleanSetting("Passive", "Apply shader to passive entities", true);
    private final BooleanSetting mobs = new BooleanSetting("Mobs", "Apply shader to hostile entities", true);
    private final BooleanSetting players = new BooleanSetting("Players", "Apply shader to player entities", true);
    private final BooleanSetting crystals = new BooleanSetting("Crystals", "Apply shader to crystals", true);
    private final BooleanSetting items = new BooleanSetting("Items", "Apply shader to items", true);

    private final BooleanSetting chests = new BooleanSetting("Chests", "Apply shader to chests", true);
    private final BooleanSetting shulkers = new BooleanSetting("Shulkers", "Apply shader to shulkers", true);
    private final BooleanSetting enderChests = new BooleanSetting("Ender Chests", "Apply shader to ender chests", true);

    private final ModeSetting<ShaderType> shaderType = new ModeSetting<>("Shader Type", "The shader to use", ShaderType.DIAMONDS);

    // DIAMONDS
    private final NumberSetting diamondSpacing = (NumberSetting) new NumberSetting("Spacing", "The spacing between diamonds", 4, 1, 16, 0.5f)
            .setParentSetting(shaderType).setVisiblity(() -> shaderType.getCurrentMode().equals(ShaderType.DIAMONDS));

    private final NumberSetting diamondSize = (NumberSetting) new NumberSetting("Size", "The size of the diamonds", 1, 0.1f, 10, 0.1f)
            .setParentSetting(shaderType).setVisiblity(() -> shaderType.getCurrentMode().equals(ShaderType.DIAMONDS));

    // OUTLINE
    private final NumberSetting outlineWidth = (NumberSetting) new NumberSetting("Width", "The width of the outline", 1, 1, 5, 0.5f)
            .setParentSetting(shaderType).setVisiblity(() -> shaderType.getCurrentMode().equals(ShaderType.OUTLINE));

    private final BooleanSetting outlineFill = (BooleanSetting) new BooleanSetting("Fill", "Fill the outline", true)
            .setParentSetting(shaderType).setVisiblity(() -> shaderType.getCurrentMode().equals(ShaderType.OUTLINE));

    // DIAGONAL
    private final NumberSetting diagonalSpacing = (NumberSetting) new NumberSetting("Spacing", "The spacing between lines", 4, 1, 16, 0.5f)
            .setParentSetting(shaderType).setVisiblity(() -> shaderType.getCurrentMode().equals(ShaderType.DIAGONAL));

    private final NumberSetting diagonalWidth = (NumberSetting) new NumberSetting("Width", "The width of the lines", 1, 1, 16, 0.5f)
            .setParentSetting(shaderType).setVisiblity(() -> shaderType.getCurrentMode().equals(ShaderType.DIAGONAL));

    private final ColourSetting colour = new ColourSetting("Colour", "The colour of the shader", new Color(185, 17, 255));

    private final OutlineShader outlineShader = new OutlineShader();
    private final DiagonalShader diagonalShader = new DiagonalShader();
    private final DiamondsShader diamondsShader = new DiamondsShader();
    private final FluidShader fluidShader = new FluidShader();

    private Framebuffer framebuffer;
    private float lastScaleFactor, lastScaleWidth, lastScaleHeight;

    public Shader() {
        super("Shader", ModuleCategory.RENDER, "Apply a shader to entities and storages");
        this.addSettings(passive, mobs, players, crystals, items, chests, shulkers, enderChests, shaderType, colour);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType().equals(RenderGameOverlayEvent.ElementType.CROSSHAIRS)) {
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
            for (Entity entity : mc.world.loadedEntityList) {
                if (entity != null && entity != mc.player && isEntityValid(entity)) {
                    mc.getRenderManager().renderEntityStatic(entity, event.getPartialTicks(), false);
                }
            }

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
            switch (shaderType.getCurrentMode()) {
                case DIAMONDS:
                    diamondsShader.setColor(colour.getColour());
                    diamondsShader.setSpacing(diamondSpacing.getValue());
                    diamondsShader.setSize(diamondSize.getValue());
                    diamondsShader.startShader();
                    break;
                case OUTLINE:
                    outlineShader.setColour(colour.getColour());
                    outlineShader.setWidth(outlineWidth.getValue());
                    outlineShader.setFill(outlineFill.isEnabled() ? 1 : 0);
                    outlineShader.setOutline(1);
                    outlineShader.startShader();
                    break;
                case DIAGONAL:
                    diagonalShader.setColour(colour.getColour());
                    diagonalShader.setWidth(diagonalWidth.getValue());
                    diagonalShader.setSpacing(diagonalSpacing.getValue());
                    diagonalShader.startShader();
                    break;
                case FLUID:
                    fluidShader.setTime(fluidShader.getTime() + 0.01);
                    fluidShader.startShader();
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

    private boolean isEntityValid(Entity entityIn) {
        return entityIn instanceof EntityOtherPlayerMP && players.isEnabled() || entityIn instanceof EntityLiving && !(entityIn instanceof EntityMob) && passive.isEnabled() || entityIn instanceof EntityMob && mobs.isEnabled() || entityIn instanceof EntityEnderCrystal && crystals.isEnabled() || entityIn instanceof EntityItem && items.isEnabled();
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

        return false;
    }

    @Override
    public String getArrayListInfo() {
        return " " + EnumFormatter.getFormattedText(shaderType.getCurrentMode());
    }

    public enum ShaderType {
        /**
         * Outline shader
         */
        OUTLINE,

        /**
         * Diagonal shader
         */
        DIAGONAL,

        /**
         * Diamonds shader
         */
        DIAMONDS,

        /**
         * Fluid Shader
         */
        FLUID
    }

}
