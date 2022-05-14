package com.paragon.client.systems.module.impl.render;

import com.paragon.api.util.string.EnumFormatter;
import com.paragon.asm.mixins.accessor.IEntityRenderer;
import com.paragon.client.shader.shaders.*;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class Shader extends Module {

    private final Setting<Boolean> passive = new Setting<>("Passive", true)
            .setDescription("Apply shader to passive entities");

    private final Setting<Boolean> mobs = new Setting<>("Mobs", true)
            .setDescription("Apply shader to hostile entities");

    private final Setting<Boolean> players = new Setting<>("Players", true)
            .setDescription("Apply shader to player entities");

    private final Setting<Boolean> crystals = new Setting<>("Crystals", true)
            .setDescription("Apply shader to crystals");

    private final Setting<Boolean> items = new Setting<>("Items", true)
            .setDescription("Apply shader to items");

    private final Setting<Boolean> chests = new Setting<>("Chests", true)
            .setDescription("Apply shader to chests");

    private final Setting<Boolean> shulkers = new Setting<>("Shulkers", true)
            .setDescription("Apply shader to shulkers");

    private final Setting<Boolean> enderChests = new Setting<>("Ender Chests", true)
            .setDescription("Apply shader to ender chests");

    private final Setting<ShaderType> shaderType = new Setting<>("Shader Type", ShaderType.DIAMONDS)
            .setDescription("The shader to use");

    // DIAMONDS
    private final Setting<Float> diamondSpacing = new Setting<>("Spacing", 4f, 1f, 16f, 0.5f)
            .setDescription("The spacing between diamonds")
            .setParentSetting(shaderType)
            .setVisibility(() -> shaderType.getValue().equals(ShaderType.DIAMONDS));

    private final Setting<Float> diamondSize = new Setting<>("Size", 1f, 0.1f, 10f, 0.1f)
            .setDescription("The size of the diamonds")
            .setParentSetting(shaderType)
            .setVisibility(() -> shaderType.getValue().equals(ShaderType.DIAMONDS));

    // OUTLINE
    private final Setting<Float> outlineWidth = new Setting<>("Width", 1f, 1f, 5f, 0.5f)
            .setParentSetting(shaderType)
            .setVisibility(() -> shaderType.getValue().equals(ShaderType.OUTLINE));

    private final Setting<Boolean> outlineFill = new Setting<>("Fill", true)
            .setDescription("Fill the outline")
            .setParentSetting(shaderType)
            .setVisibility(() -> shaderType.getValue().equals(ShaderType.OUTLINE));

    // DIAGONAL
    private final Setting<Float> diagonalSpacing = new Setting<>("Spacing", 4f, 1f, 16f, 0.5f)
            .setDescription("The spacing between lines")
            .setParentSetting(shaderType)
            .setVisibility(() -> shaderType.getValue().equals(ShaderType.DIAGONAL));

    private final Setting<Float> diagonalWidth = new Setting<>("Width", 1f, 1f, 16f, 0.5f)
            .setDescription("The width of the lines")
            .setParentSetting(shaderType)
            .setVisibility(() -> shaderType.getValue().equals(ShaderType.DIAGONAL));

    // Colour
    private final Setting<Color> colour = new Setting<>("Colour", new Color(185, 17, 255))
            .setDescription("The colour of the shader");

    private final OutlineShader outlineShader = new OutlineShader();
    private final DiagonalShader diagonalShader = new DiagonalShader();
    private final DiamondsShader diamondsShader = new DiamondsShader();
    private final FluidShader fluidShader = new FluidShader();
    private final LiquidShader liquidShader = new LiquidShader();
    private final SmokeShader smokeShader = new SmokeShader();

    private Framebuffer framebuffer;
    private float lastScaleFactor, lastScaleWidth, lastScaleHeight;

    public Shader() {
        super("Shader", Category.RENDER, "Apply a shader to entities and storages");
        this.addSettings(passive, mobs, players, crystals, items, chests, shulkers, enderChests, shaderType, colour);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType().equals(RenderGameOverlayEvent.ElementType.HOTBAR)) {
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
            switch (shaderType.getValue()) {
                case DIAMONDS:
                    diamondsShader.setColor(colour.getValue());
                    diamondsShader.setSpacing(diamondSpacing.getValue());
                    diamondsShader.setSize(diamondSize.getValue());
                    diamondsShader.startShader();
                    break;

                case OUTLINE:
                    outlineShader.setColour(colour.getValue());
                    outlineShader.setWidth(outlineWidth.getValue());
                    outlineShader.setFill(outlineFill.getValue() ? 1 : 0);
                    outlineShader.setOutline(1);
                    outlineShader.startShader();
                    break;

                case DIAGONAL:
                    diagonalShader.setColour(colour.getValue());
                    diagonalShader.setWidth(diagonalWidth.getValue());
                    diagonalShader.setSpacing(diagonalSpacing.getValue());
                    diagonalShader.startShader();
                    break;

                case FLUID:
                    fluidShader.setTime(fluidShader.getTime() + 0.01);
                    fluidShader.startShader();
                    break;

                case LIQUID:
                    liquidShader.setTime(liquidShader.getTime() + 0.01);
                    liquidShader.setColour(colour.getValue());
                    liquidShader.startShader();
                    break;

                case SMOKE:
                    smokeShader.setTime(smokeShader.getTime() + 0.001f);
                    smokeShader.setColour(colour.getValue());
                    smokeShader.startShader();
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

            mc.entityRenderer.setupOverlayRendering();
        }
    }

    private boolean isEntityValid(Entity entityIn) {
        return entityIn instanceof EntityPlayer && players.getValue() ||
                entityIn instanceof EntityLiving && !(entityIn instanceof EntityMob) && passive.getValue() ||
                entityIn instanceof EntityMob && mobs.getValue() ||
                entityIn instanceof EntityEnderCrystal && crystals.getValue() ||
                entityIn instanceof EntityItem && items.getValue();
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
    public String getArrayListInfo() {
        return " " + EnumFormatter.getFormattedText(shaderType.getValue());
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
        FLUID,

        /**
         * Liquid shader
         */
        LIQUID,

        /**
         * Smoke shader
         */
        SMOKE
    }

}
