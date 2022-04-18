package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.client.SettingUpdateEvent;
import com.paragon.api.event.render.ShaderColourEvent;
import com.paragon.api.event.render.entity.RenderEntityEvent;
import com.paragon.api.util.entity.EntityUtil;
import com.paragon.api.util.render.OutlineUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.asm.mixins.accessor.IEntityRenderer;
import com.paragon.asm.mixins.accessor.IRenderGlobal;
import com.paragon.asm.mixins.accessor.IShaderGroup;
import com.paragon.client.shader.shaders.OutlineShader;
import com.paragon.client.shader.shaders.SmoothShader;
import com.paragon.client.shader.shaders.DiagonalShader;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.*;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 * @author Wolfsurge, with shader stuff from Cosmos (first time using shaders / glsl lel)
 */
@SuppressWarnings("unchecked")
public class ESP extends Module {

    /* Entity settings */
    private final BooleanSetting passive = new BooleanSetting("Passives", "Highlight passive entities", true);
    private final BooleanSetting mobs = new BooleanSetting("Mobs", "Highlight mobs", true);
    private final BooleanSetting players = new BooleanSetting("Players", "Highlight player entities", true);
    private final BooleanSetting items = new BooleanSetting("Items", "Highlight items", true);
    private final BooleanSetting crystals = new BooleanSetting("Crystals", "Highlight end crystals", true);

    // Render settings
    private final ModeSetting<Mode> mode = new ModeSetting<>("Mode", "How to render the entities", Mode.SHADER);
    private final NumberSetting lineWidth = new NumberSetting("Line Width", "How thick to render the outlines", 2, 0.1f, 8, 0.1f);

    // Shader settings
    private final ModeSetting<FragShader> shader = (ModeSetting<FragShader>) new ModeSetting<>("Shader", "The shader to use", FragShader.OUTLINE)
            .setParentSetting(mode).setVisiblity(() -> mode.getCurrentMode().equals(Mode.SHADER));

    // Outline shader
    private final BooleanSetting outline = (BooleanSetting) new BooleanSetting("Outline", "Outline the fill", true)
            .setParentSetting(mode).setVisiblity(() -> mode.getCurrentMode().equals(Mode.SHADER) && shader.getCurrentMode().equals(FragShader.SMOOTH));

    private final BooleanSetting fill = (BooleanSetting) new BooleanSetting("Fill", "Fill the outline", true)
            .setParentSetting(mode).setVisiblity(() -> mode.getCurrentMode().equals(Mode.SHADER) && shader.getCurrentMode().equals(FragShader.OUTLINE));

    // Smooth shader
    private final ModeSetting<SmoothShaderColour> smoothShaderColour = (ModeSetting<SmoothShaderColour>) new ModeSetting<>("Smooth Colour", "The colour to use for the smooth shader", SmoothShaderColour.POSITION)
            .setParentSetting(mode).setVisiblity(() -> mode.getCurrentMode().equals(Mode.SHADER) && shader.getCurrentMode().equals(FragShader.SMOOTH));

    // Diagonal shader
    private final NumberSetting diagonalSpeed = (NumberSetting) new NumberSetting("Speed", "The speed at which the shader moves", 0.01f, 0.01f, 0.3f, 0.01f)
            .setParentSetting(mode).setVisiblity(() -> mode.getCurrentMode().equals(Mode.SHADER) && shader.getCurrentMode().equals(FragShader.DIAGONAL));

    private final ColourSetting colour = (ColourSetting) new ColourSetting("Colour", "The colour to highlight items in", new Color(185, 17, 255))
            .setVisiblity(() -> !mode.getCurrentMode().equals(Mode.SHADER) || shader.getCurrentMode().equals(FragShader.OUTLINE) || shader.getCurrentMode().equals(FragShader.DIAGONAL));

    private Framebuffer framebuffer;
    private float lastScaleFactor, lastScaleWidth, lastScaleHeight;

    // Shaders
    private final OutlineShader outlineShader = new OutlineShader();
    private final SmoothShader smoothShader = new SmoothShader();
    private final DiagonalShader diagonalShader = new DiagonalShader();

    public ESP() {
        super("ESP", ModuleCategory.RENDER, "Highlights entities in the world");
        this.addSettings(passive, mobs, players, items, crystals, mode, lineWidth, colour);
    }

    @Override
    public void onDisable() {
        if (nullCheck()) {
            return;
        }

        for(Entity e : mc.world.loadedEntityList) {
            e.setGlowing(false);
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
            for (Entity entity : mc.world.loadedEntityList) {
                if (entity != null && entity != mc.player && isEntityValid(entity)) {
                    mc.getRenderManager().renderEntityStatic(entity, event.getPartialTicks(), false);
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
    public void onRenderEntity(RenderEntityEvent event) {
        if(isEntityValid(event.getEntity()) && mode.getCurrentMode() == Mode.OUTLINE) {
            OutlineUtil.renderOne(lineWidth.getValue());
            event.renderModel();
            OutlineUtil.renderTwo();
            event.renderModel();
            OutlineUtil.renderThree();
            event.renderModel();
            OutlineUtil.renderFour(colour.getColour());
            event.renderModel();
            OutlineUtil.renderFive();
            event.renderModel();
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        for(Entity e : mc.world.loadedEntityList) {
            if(isEntityValid(e)) {
                espEntity(e);
            }
        }

        // Check glow
        if (mode.getCurrentMode().equals(Mode.GLOW)) {
            // Get shaders
            List<Shader> shaders = ((IShaderGroup) ((IRenderGlobal) mc.renderGlobal).getEntityOutlineShader()).getListShaders();

            shaders.forEach(shader -> {
                // Get line width
                ShaderUniform uniform = shader.getShaderManager().getShaderUniform("Radius");

                if (uniform != null) {
                    // Set line width
                    uniform.set(lineWidth.getValue());
                }
            });
        }
    }

    @Listener
    public void onShaderColour(ShaderColourEvent event) {
        if (mode.getCurrentMode().equals(Mode.GLOW)) {
            event.setColour(colour.getColour());
            event.cancel();
        }
    }

    @Listener
    public void onSettingUpdate(SettingUpdateEvent event) {
        if (event.getSetting() == mode) {
            for (Entity entity : mc.world.loadedEntityList) {
                entity.setGlowing(false);
            }
        }
    }

    /**
     * Highlights an entity
     * @param entityIn The entity to highlight
     */
    public void espEntity(Entity entityIn) {
        if (mode.getCurrentMode() == Mode.BOX) {
            RenderUtil.drawBoundingBox(EntityUtil.getEntityBox(entityIn), lineWidth.getValue(), colour.getColour());
        } else if (mode.getCurrentMode() == Mode.GLOW) {
            entityIn.setGlowing(true);
        }
    }

    /**
     * Checks if an entity is valid
     * @param entityIn The entity to check
     * @return Is the entity valid
     */
    private boolean isEntityValid(Entity entityIn) {
        return entityIn instanceof EntityOtherPlayerMP && players.isEnabled() || entityIn instanceof EntityLiving && !(entityIn instanceof EntityMob) && passive.isEnabled() || entityIn instanceof EntityMob && mobs.isEnabled() || entityIn instanceof EntityEnderCrystal && crystals.isEnabled() || entityIn instanceof EntityItem && items.isEnabled();
    }

    public enum Mode {
        /**
         * Outline the entity
         */
        OUTLINE,

        /**
         * Apply vanilla glow shader
         */
        GLOW,

        /**
         * Draw a box
         */
        BOX,

        /**
         * Draw with shader
         */
        SHADER
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
