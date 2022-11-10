package com.paragon.impl.module.render

import com.paragon.impl.event.client.SettingUpdateEvent
import com.paragon.impl.event.render.ShaderColourEvent
import com.paragon.impl.event.render.entity.RenderEntityEvent
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category
import com.paragon.mixins.accessor.IEntityRenderer
import com.paragon.mixins.accessor.IRenderGlobal
import com.paragon.mixins.accessor.IShaderGroup
import com.paragon.util.anyNull
import com.paragon.util.entity.EntityUtil
import com.paragon.util.entity.EntityUtil.isMonster
import com.paragon.util.entity.EntityUtil.isPassive
import com.paragon.util.render.OutlineUtil.renderFive
import com.paragon.util.render.OutlineUtil.renderFour
import com.paragon.util.render.OutlineUtil.renderOne
import com.paragon.util.render.OutlineUtil.renderThree
import com.paragon.util.render.OutlineUtil.renderTwo
import com.paragon.util.render.builder.BoxRenderMode
import com.paragon.util.render.builder.RenderBuilder
import com.paragon.util.render.shader.shaders.OutlineShader
import com.paragon.util.string.StringUtil
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.shader.Framebuffer
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

/**
 * @author Surge, with shader stuff from Cosmos (first time using shaders / glsl lel)
 */
@SideOnly(Side.CLIENT)
object ESP : Module("ESP", Category.RENDER, "Highlights entities in the world") {

    // Entity filters
    private val passive = Setting("Passives", true) describedBy "Highlight passive entities"
    private val passiveColour = Setting("Colour", Color.GREEN) describedBy "The colour to highlight passives in" subOf passive

    private val mobs = Setting("Mobs", true) describedBy "Highlight mobs"
    private val mobColour = Setting("Colour", Color.RED) describedBy "The colour to highlight mobs in" subOf mobs

    private val players = Setting("Players", true) describedBy "Highlight player entities"
    private val playerColour = Setting("Colour", Color(185, 17, 255)) describedBy "The colour to highlight players in" subOf players

    private val items = Setting("Items", true) describedBy "Highlight items"
    private val itemColour = Setting("Colour", Color.WHITE) describedBy "The colour to highlight items in" subOf items

    private val crystals = Setting("Crystals", true) describedBy "Highlight crystals"
    private val crystalColour = Setting("Colour", Color.MAGENTA) describedBy "The colour to highlight crystals in" subOf crystals

    // Render settings
    private val mode = Setting("Mode", Mode.SHADER) describedBy "How to render the entities"
    private val lineWidth = Setting("LineWidth", 1f, 0.1f, 3f, 0.1f) describedBy "How thick to render the outlines"

    // Box setting
    private val boxMode = Setting("Box", BoxRenderMode.BOTH) describedBy "How to render the box" subOf mode visibleWhen { mode.value == Mode.BOX }

    // Outline shader
    private val outline = Setting("Outline", true) describedBy "Outline the fill" subOf mode visibleWhen { mode.value == Mode.SHADER }
    private val fill = Setting("Fill", true) describedBy "Fill the outline" subOf mode visibleWhen { mode.value == Mode.SHADER }

    // Shaders
    private val passiveShader = OutlineShader()
    private val mobShader = OutlineShader()
    private val playerShader = OutlineShader()
    private val itemShader = OutlineShader()
    private val crystalShader = OutlineShader()

    private var frameBuffer: Framebuffer? = null
    private var lastScaleFactor = 0f
    private var lastScaleWidth = 0f
    private var lastScaleHeight = 0f

    override fun onDisable() {
        if (minecraft.anyNull) {
            return
        }

        for (e in minecraft.world.loadedEntityList) {
            e.isGlowing = false
        }
    }

    @Suppress("ReplaceNotNullAssertionWithElvisReturn")
    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Pre) {
        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR && mode.value == Mode.SHADER) {
            // Pretty much just taken from Cosmos, all credit goes to them (sorry linus!)
            // https://github.com/momentumdevelopment/cosmos/blob/main/src/main/java/cope/cosmos/client/features/modules/visual/ESPModule.java

            // Delete old framebuffer
            if (frameBuffer != null) {
                frameBuffer!!.framebufferClear()

                if (lastScaleFactor != event.resolution.scaleFactor.toFloat() || lastScaleWidth != event.resolution.scaledWidth.toFloat() || lastScaleHeight != event.resolution.scaledHeight.toFloat()) {
                    frameBuffer!!.deleteFramebuffer()
                    frameBuffer = Framebuffer(minecraft.displayWidth, minecraft.displayHeight, true)
                    frameBuffer!!.framebufferClear()
                }

                lastScaleFactor = event.resolution.scaleFactor.toFloat()
                lastScaleWidth = event.resolution.scaledWidth.toFloat()
                lastScaleHeight = event.resolution.scaledHeight.toFloat()
            } else {
                frameBuffer = Framebuffer(minecraft.displayWidth, minecraft.displayHeight, true)
            }

            if (passive.value) {
                renderShader(minecraft.world.loadedEntityList.filter { it.isPassive() }, passiveShader, passiveColour.value, event.resolution, event.partialTicks)
            }

            if (mobs.value) {
                renderShader(minecraft.world.loadedEntityList.filter { it.isMonster() }, mobShader, mobColour.value, event.resolution, event.partialTicks)
            }

            if (players.value) {
                renderShader(minecraft.world.loadedEntityList.filter { it is EntityOtherPlayerMP && it != minecraft.player}, playerShader, playerColour.value, event.resolution, event.partialTicks)
            }

            if (items.value) {
                renderShader(minecraft.world.loadedEntityList.filterIsInstance<EntityItem>(), itemShader, itemColour.value, event.resolution, event.partialTicks)
            }

            if (crystals.value) {
                renderShader(minecraft.world.loadedEntityList.filterIsInstance<EntityEnderCrystal>(), crystalShader, crystalColour.value, event.resolution, event.partialTicks)
            }
        }
    }

    private fun renderShader(entities: List<Entity>, shader: OutlineShader, colour: Color, resolution: ScaledResolution, partialTicks: Float) {
        GlStateManager.enableAlpha()
        GlStateManager.pushMatrix()
        GlStateManager.pushAttrib()

        frameBuffer!!.bindFramebuffer(false)
        val previousShadows = minecraft.gameSettings.entityShadows
        minecraft.gameSettings.entityShadows = false
        (minecraft.entityRenderer as IEntityRenderer).hookSetupCameraTransform(partialTicks, 0)

        for (entity in entities) {
            minecraft.renderManager.renderEntityStatic(entity, partialTicks, false)
        }

        minecraft.gameSettings.entityShadows = previousShadows
        GlStateManager.enableBlend()
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        frameBuffer!!.unbindFramebuffer()
        minecraft.framebuffer.bindFramebuffer(true)
        minecraft.entityRenderer.disableLightmap()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.pushMatrix()

        // Render shader
        shader.setColour(colour)
        shader.setWidth(lineWidth.value)
        shader.setFill(if (fill.value) 1 else 0)
        shader.setOutline(if (outline.value) 1 else 0)
        shader.startShader()

        minecraft.entityRenderer.setupOverlayRendering()

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, frameBuffer!!.framebufferTexture)
        GL11.glBegin(GL11.GL_QUADS)
        GL11.glTexCoord2d(0.0, 1.0)
        GL11.glVertex2d(0.0, 0.0)
        GL11.glTexCoord2d(0.0, 0.0)
        GL11.glVertex2d(0.0, resolution.scaledHeight.toDouble())
        GL11.glTexCoord2d(1.0, 0.0)
        GL11.glVertex2d(resolution.scaledWidth.toDouble(), resolution.scaledHeight.toDouble())
        GL11.glTexCoord2d(1.0, 1.0)
        GL11.glVertex2d(resolution.scaledWidth.toDouble(), 0.0)
        GL11.glEnd()

        // Stop drawing shader
        GL20.glUseProgram(0)
        GL11.glPopMatrix()
        minecraft.entityRenderer.enableLightmap()
        GlStateManager.popMatrix()
        GlStateManager.popAttrib()
        minecraft.entityRenderer.setupOverlayRendering()
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
            renderFour(getEntityColour(event.entity))
            event.renderModel()
            renderFive()
            event.renderModel()
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent?) {
        for (e in minecraft.world.loadedEntityList) {
            if (isEntityValid(e)) {
                espEntity(e)
            }
        }

        // Check glow
        if (mode.value == Mode.GLOW) {
            // Get shaders
            val shaders = ((minecraft.renderGlobal as IRenderGlobal).hookGetEntityOutlineShader() as IShaderGroup).hookGetListShaders()
            shaders.forEach {
                // Get line width
                val uniform = (it ?: return@forEach).shaderManager.getShaderUniform("Radius")
                uniform?.set(lineWidth.value)
            }
        }
    }

    @Listener
    fun onShaderColour(event: ShaderColourEvent) {
        if (mode.value == Mode.GLOW) {
            event.colour = getEntityColour(event.entity)
            event.cancel()
        }
    }

    @Listener
    fun onSettingUpdate(event: SettingUpdateEvent) {
        if (event.setting == mode) {
            for (entity in minecraft.world.loadedEntityList) {
                entity.isGlowing = false
            }
        }
    }

    /**
     * Highlights an entity
     *
     * @param entityIn The entity to highlight
     */
    private fun espEntity(entityIn: Entity) {
        if (mode.value == Mode.BOX) {
            RenderBuilder()
                .boundingBox(EntityUtil.getEntityBox(entityIn))
                .inner(getEntityColour(entityIn))
                .outer(getEntityColour(entityIn).integrateAlpha(255f))
                .type(boxMode.value)
                .start()
                .blend(true).depth(true).texture(true).lineWidth(lineWidth.value)
                .build(false)

        }
        else if (mode.value == Mode.GLOW) {
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
        return entityIn is EntityPlayer && entityIn !== minecraft.player && players.value || entityIn is EntityLiving && entityIn !is EntityMob && passive.value || entityIn is EntityMob && mobs.value || entityIn is EntityEnderCrystal && crystals.value || entityIn is EntityItem && items.value
    }

    private fun getEntityColour(entityIn: Entity): Color {
        return when {
            entityIn.isPassive() -> passiveColour.value
            entityIn.isMonster() -> mobColour.value
            entityIn is EntityOtherPlayerMP -> playerColour.value
            entityIn is EntityItem -> itemColour.value
            entityIn is EntityEnderCrystal -> crystalColour.value
            else -> return Color(0, 0, 0)
        }
    }

    override fun getData(): String = StringUtil.getFormattedText(mode.value)

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