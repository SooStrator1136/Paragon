package com.paragon.client.systems.module.impl.render

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting

import com.paragon.api.util.Wrapper
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import com.paragon.api.util.entity.EntityUtil
import com.paragon.api.util.render.ColourUtil
import net.minecraft.entity.item.EntityEnderCrystal
import com.paragon.api.util.render.RenderUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.MathHelper
import java.awt.Color
import java.util.function.Consumer
import kotlin.math.min

/**
 * @author Surge
 */
class Tracers : Module("Tracers", Category.RENDER, "Draws lines to entities in the world") {
    private val passive = Setting<Boolean?>("Passives", true)
        .setDescription("Draws lines to passive entities")

    private val passiveColour = Setting("Colour", Color(0, 255, 0, 180))
        .setDescription("The colour to render the passive tracers in")
        .setParentSetting(passive)
        .setVisibility { distance.value != Distance.COLOUR }

    private val mobs = Setting<Boolean?>("Mobs", true)
        .setDescription("Draws lines to monsters")

    private val mobColour = Setting("Colour", Color(255, 0, 0, 180))
        .setDescription("The colour to render the mob tracers in")
        .setParentSetting(mobs)
        .setVisibility { distance.value != Distance.COLOUR }

    private val players = Setting<Boolean?>("Players", true)
        .setDescription("Draws lines to players")

    private val playerColour = Setting("Colour", Color(255, 255, 255, 180))
        .setDescription("The colour to render the player tracers in")
        .setParentSetting(players)
        .setVisibility { distance.value != Distance.COLOUR }

    private val crystals = Setting<Boolean?>("Crystals", true)
        .setDescription("Draws lines to ender crystals")

    private val crystalColour = Setting("Colour", Color(200, 0, 200, 180))
        .setDescription("The colour to render the ender crystal tracers in")
        .setParentSetting(crystals)
        .setVisibility { distance.value != Distance.COLOUR }

    private val distance = Setting("Distance", Distance.ALPHA)
        .setDescription("Change the colour depending on the distance to the entity")

    private val distanceDivider = Setting("Divider", 1500f, 0f, 5000f, 100f)
        .setDescription("The divider to use when calculating the distance factor")
        .setParentSetting(distance)

    private val lineWidth = Setting("LineWidth", 0.5f, 0.1f, 2f, 0.1f)
        .setDescription("How thick to render the lines")

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent?) {
        minecraft.world.loadedEntityList.forEach(Consumer { entity: Entity ->
            if (EntityUtil.isEntityAllowed(entity, players.value!!, mobs.value!!, passive.value!!) && entity !== minecraft.player || entity is EntityEnderCrystal && crystals.value!!) {
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
        var colour: Color = passiveColour.value

        if (EntityUtil.isPassive(entityIn)) {
            colour = passiveColour.value
        }

        if (EntityUtil.isMonster(entityIn)) {
            colour = mobColour.value
        }

        if (entityIn is EntityPlayer) {
            colour = playerColour.value
        }

        if (entityIn is EntityEnderCrystal) {
            colour = crystalColour.value
        }

        if (distance.value != Distance.OFF) {
            val factor: Float = MathHelper.clamp((minecraft.player.getDistanceSq(entityIn) / distanceDivider.value).toFloat(), 0f, 1f)

            colour = if (distance.value == Distance.COLOUR) {
                ColourUtil.integrateAlpha(Color(1f - factor, factor, 0f), colour.alpha.toFloat())
            } else {
                ColourUtil.integrateAlpha(colour, 255f * (1 - factor))
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