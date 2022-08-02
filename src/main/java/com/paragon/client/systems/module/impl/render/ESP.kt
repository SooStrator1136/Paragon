package com.paragon.client.systems.module.impl.render

import com.paragon.api.event.client.SettingUpdateEvent
import com.paragon.api.event.render.ShaderColourEvent
import com.paragon.api.event.render.entity.RenderEntityEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.Wrapper
import com.paragon.api.util.entity.EntityUtil
import com.paragon.api.util.render.OutlineUtil.renderFive
import com.paragon.api.util.render.OutlineUtil.renderFour
import com.paragon.api.util.render.OutlineUtil.renderOne
import com.paragon.api.util.render.OutlineUtil.renderThree
import com.paragon.api.util.render.OutlineUtil.renderTwo
import com.paragon.api.util.render.RenderUtil.drawBoundingBox
import com.paragon.api.util.string.StringUtil
import com.paragon.asm.mixins.accessor.IEntityRenderer
import com.paragon.asm.mixins.accessor.IRenderGlobal
import com.paragon.asm.mixins.accessor.IShaderGroup
import com.paragon.client.shader.shaders.OutlineShader
import me.wolfsurge.cerauno.listener.Listener
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.shader.Framebuffer
import net.minecraft.client.shader.Shader
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import java.awt.Color
import java.util.function.Consumer

/**
 * @author Surge, with shader stuff from Cosmos (first time using shaders / glsl lel)
 */
@SideOnly(Side.CLIENT)
object ESP : Module("ESP", Category.RENDER, "Highlights entities in the world") {

    // Entity filters
    private val passive = Setting("Passives", true)
        .setDescription("Highlight passive entities")

    private val mobs = Setting("Mobs", true)
        .setDescription("Highlight mobs")

    private val players = Setting("Players", true)
        .setDescription("Highlight player entities")

    private val items = Setting("Items", true)
        .setDescription("Highlight items")

    private val crystals = Setting("Crystals", true)
        .setDescription("Highlight crystals")

    // Render settings
    private val mode = Setting("Mode", Mode.SHADER)
        .setDescription("How to render the entities")

    private val lineWidth = Setting("LineWidth", 1f, 0.1f, 3f, 0.1f)
        .setDescription("How thick to render the outlines")

    // Outline shader
    private val outline = Setting("Outline", true)
        .setDescription("Outline the fill")
        .setParentSetting(mode)
        .setVisibility { mode.value == Mode.SHADER }

    private val fill = Setting("Fill", true)
        .setDescription("Fill the outline")
        .setParentSetting(mode)
        .setVisibility { mode.value == Mode.SHADER }

    private val colour = Setting("Colour", Color(185, 17, 255))
        .setDescription("The colour to highlight items in")

    // Shaders
    private val outlineShader = OutlineShader()
    private var framebuffer: Framebuffer? = null
    private var lastScaleFactor = 0f
    private var lastScaleWidth = 0f
    private var lastScaleHeight = 0f

    override fun onDisable() {
        if (nullCheck()) {
            return
        }
        for (e in Wrapper.mc.world.loadedEntityList) {
            e.isGlowing = false
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Pre) {
        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR && mode.value == Mode.SHADER) {
            // Pretty much just taken from Cosmos, all credit goes to them (sorry linus!)
            // https://github.com/momentumdevelopment/cosmos/blob/main/src/main/java/cope/cosmos/client/features/modules/visual/ESPModule.java
            GlStateManager.enableAlpha()
            GlStateManager.pushMatrix()
            GlStateManager.pushAttrib()

            // Delete old framebuffer
            if (framebuffer != null) {
                framebuffer!!.framebufferClear()
                if (lastScaleFactor != event.resolution.scaleFactor.toFloat() || lastScaleWidth != event.resolution.scaledWidth.toFloat() || lastScaleHeight != event.resolution.scaledHeight.toFloat()) {
                    framebuffer!!.deleteFramebuffer()
                    framebuffer = Framebuffer(Wrapper.mc.displayWidth, Wrapper.mc.displayHeight, true)
                    framebuffer!!.framebufferClear()
                }
                lastScaleFactor = event.resolution.scaleFactor.toFloat()
                lastScaleWidth = event.resolution.scaledWidth.toFloat()
                lastScaleHeight = event.resolution.scaledHeight.toFloat()
            } else {
                framebuffer = Framebuffer(Wrapper.mc.displayWidth, Wrapper.mc.displayHeight, true)
            }
            framebuffer!!.bindFramebuffer(false)
            val previousShadows = Wrapper.mc.gameSettings.entityShadows
            Wrapper.mc.gameSettings.entityShadows = false
            (Wrapper.mc.entityRenderer as IEntityRenderer).setupCamera(event.partialTicks, 0)
            for (entity in Wrapper.mc.world.loadedEntityList) {
                if (entity != null && entity !== Wrapper.mc.player && isEntityValid(entity)) {
                    Wrapper.mc.renderManager.renderEntityStatic(entity, event.partialTicks, false)
                }
            }
            Wrapper.mc.gameSettings.entityShadows = previousShadows
            GlStateManager.enableBlend()
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            framebuffer!!.unbindFramebuffer()
            Wrapper.mc.framebuffer.bindFramebuffer(true)
            Wrapper.mc.entityRenderer.disableLightmap()
            RenderHelper.disableStandardItemLighting()
            GlStateManager.pushMatrix()

            // Render shader
            outlineShader.setColour(colour.value)
            outlineShader.setWidth(lineWidth.value)
            outlineShader.setFill(if (fill.value) 1 else 0)
            outlineShader.setOutline(if (outline.value) 1 else 0)
            outlineShader.startShader()
            Wrapper.mc.entityRenderer.setupOverlayRendering()
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebuffer!!.framebufferTexture)
            GL11.glBegin(GL11.GL_QUADS)
            GL11.glTexCoord2d(0.0, 1.0)
            GL11.glVertex2d(0.0, 0.0)
            GL11.glTexCoord2d(0.0, 0.0)
            GL11.glVertex2d(0.0, event.resolution.scaledHeight.toDouble())
            GL11.glTexCoord2d(1.0, 0.0)
            GL11.glVertex2d(event.resolution.scaledWidth.toDouble(), event.resolution.scaledHeight.toDouble())
            GL11.glTexCoord2d(1.0, 1.0)
            GL11.glVertex2d(event.resolution.scaledWidth.toDouble(), 0.0)
            GL11.glEnd()

            // Stop drawing shader
            GL20.glUseProgram(0)
            GL11.glPopMatrix()
            Wrapper.mc.entityRenderer.enableLightmap()
            GlStateManager.popMatrix()
            GlStateManager.popAttrib()
            Wrapper.mc.entityRenderer.setupOverlayRendering()
        }
    }

    @Listener
    fun onRenderEntity(event: RenderEntityEvent) {
        if (isEntityValid(event.entity) && mode.value == Mode.OUTLINE) {
            renderOne(lineWidth.value)
            event.renderModel()
            renderTwo()
            event.renderModel()
            renderThree()
            event.renderModel()
            renderFour(colour.value)
            event.renderModel()
            renderFive()
            event.renderModel()
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent?) {
        for (e in Wrapper.mc.world.loadedEntityList) {
            if (isEntityValid(e)) {
                espEntity(e)
            }
        }

        // Check glow
        if (mode.value == Mode.GLOW) {
            // Get shaders
            val shaders = ((Wrapper.mc.renderGlobal as IRenderGlobal).entityOutlineShader as IShaderGroup).listShaders
            shaders.forEach(Consumer { shader: Shader ->
                // Get line width
                val uniform = shader.shaderManager.getShaderUniform("Radius")
                uniform?.set(lineWidth.value)
            })
        }
    }

    @Listener
    fun onShaderColour(event: ShaderColourEvent) {
        if (mode.value == Mode.GLOW) {
            event.colour = colour.value
            event.cancel()
        }
    }

    @Listener
    fun onSettingUpdate(event: SettingUpdateEvent) {
        if (event.setting == mode) {
            for (entity in Wrapper.mc.world.loadedEntityList) {
                entity.isGlowing = false
            }
        }
    }

    /**
     * Highlights an entity
     *
     * @param entityIn The entity to highlight
     */
    fun espEntity(entityIn: Entity) {
        if (mode.value == Mode.BOX) {
            drawBoundingBox(EntityUtil.getEntityBox(entityIn), lineWidth.value, colour.value)
        } else if (mode.value == Mode.GLOW) {
            entityIn.isGlowing = true
        }
    }

    /**
     * Checks if an entity is valid
     *
     * @param entityIn The entity to check
     * @return Is the entity valid
     */
    private fun isEntityValid(entityIn: Entity): Boolean {
        return entityIn is EntityPlayer && entityIn !== Wrapper.mc.player && players.value || entityIn is EntityLiving && entityIn !is EntityMob && passive.value || entityIn is EntityMob && mobs.value || entityIn is EntityEnderCrystal && crystals.value || entityIn is EntityItem && items.value
    }

    override fun getData(): String {
        return StringUtil.getFormattedText(mode.value)
    }

    enum class Mode {
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
}