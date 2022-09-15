package com.paragon.impl.module.render

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.impl.module.Category
import com.paragon.util.entity.EntityUtil.isEntityAllowed
import com.paragon.util.entity.EntityUtil.isMonster
import com.paragon.util.entity.EntityUtil.isPassive
import com.paragon.util.render.RenderUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.MathHelper
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import java.util.function.Consumer

/**
 * @author Surge
 */
object Tracers : Module("Tracers", Category.RENDER, "Draws lines to entities in the world") {

    private val passive = Setting("Passives", true) describedBy "Draws lines to passive entities"

    private val passiveColour = Setting(
        "Colour", Color(0, 255, 0, 180)
    ) describedBy "The colour to render the passive tracers in" subOf passive visibleWhen { distance.value != Distance.COLOUR }

    private val mobs = Setting("Mobs", true) describedBy "Draws lines to monsters"

    private val mobColour = Setting(
        "Colour", Color(255, 0, 0, 180)
    ) describedBy "The colour to render the mob tracers in" subOf mobs visibleWhen { distance.value != Distance.COLOUR }

    private val players = Setting(
        "Players", true
    ) describedBy "Draws lines to players"

    private val playerColour = Setting(
        "Colour", Color(255, 255, 255, 180)
    ) describedBy "The colour to render the player tracers in" subOf players visibleWhen { distance.value != Distance.COLOUR }

    private val crystals = Setting(
        "Crystals", true
    ) describedBy "Draws lines to ender crystals"

    private val crystalColour = Setting(
        "Colour", Color(200, 0, 200, 180)
    ) describedBy "The colour to render the ender crystal tracers in" subOf crystals visibleWhen { distance.value != Distance.COLOUR }

    private val distance = Setting(
        "Distance", Distance.ALPHA
    ) describedBy "Change the colour depending on the distance to the entity"

    private val distanceDivider = Setting(
        "Divider", 1500f, 0f, 5000f, 100f
    ) describedBy "The divider to use when calculating the distance factor" subOf distance

    private val lineWidth = Setting("LineWidth", 0.5f, 0.1f, 2f, 0.1f) describedBy "How thick to render the lines"

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent?) {
        minecraft.world.loadedEntityList.forEach(Consumer { entity: Entity ->
            if (entity.isEntityAllowed(
                    players.value, mobs.value, passive.value
                ) && entity !== minecraft.player || entity is EntityEnderCrystal && crystals.value
            ) {
                RenderUtil.drawTracer(entity, lineWidth.value, getColourByEntity(entity))
            }
        })
    }

    /**
     * Gets the entity's colour
     *
     * @param entityIn The entity
     * @return The entity's colour
     */
    private fun getColourByEntity(entityIn: Entity): Color {
        var colour = passiveColour.value

        if (entityIn.isPassive()) {
            colour = passiveColour.value
        }
        else if (entityIn.isMonster()) {
            colour = mobColour.value
        }
        else if (entityIn is EntityPlayer) {
            colour = playerColour.value
        }
        else if (entityIn is EntityEnderCrystal) {
            colour = crystalColour.value
        }

        if (distance.value != Distance.OFF) {
            val factor = MathHelper.clamp(
                (minecraft.player.getDistanceSq(entityIn) / distanceDivider.value).toFloat(), 0f, 1f
            )

            colour = if (distance.value == Distance.COLOUR) {
                Color(1f - factor, factor, 0f).integrateAlpha(colour.alpha.toFloat())
            }
            else {
                colour.integrateAlpha(255f * (1 - factor))
            }
        }

        return colour
    }

    enum class Distance {
        /**
         * Red -> Green depending on distance
         */
        COLOUR,

        /**
         * Transparency based on distance
         */
        ALPHA,

        /**
         * Don't do anything
         */
        OFF
    }

}