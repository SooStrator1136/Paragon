package com.paragon.client.systems.module.impl.render

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.entity.EntityUtil
import com.paragon.api.util.player.PlayerUtil
import com.paragon.api.util.render.ColourUtil
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author SooStrator1136
 */
object ChinaHat : Module("ChinaHat", Category.RENDER, "-69420 social credit :((") {

    // Colours
    private val topColour = Setting("TopColour", Color(185, 17, 255, 180))
        .setDescription("The top colour of the hat")

    private val bottomColour = Setting("BottomColour", Color(185, 17, 255, 180))
        .setDescription("The bottom colour of the hat")

    // Settings
    private val firstPerson = Setting("FirstPerson", false)
        .setDescription("Render the hat in first person")

    private val others = Setting("Others", true)
        .setDescription("Render the hat on other players")

    override fun onRender3D() {
        mc.world.playerEntities.forEach {
            // We don't want to render the hat
            if (it === mc.player && !firstPerson.getValue() && mc.gameSettings.thirdPersonView == 0 || !others.getValue() && it !== mc.player) return

            // Render the hat
            renderHat(it)
        }
    }

    private fun renderHat(player: EntityPlayer) {
        GL11.glPushMatrix()
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_POINT_SMOOTH)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glShadeModel(GL11.GL_SMOOTH)
        GlStateManager.disableCull()
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP)

        // Get the vector to start drawing the hat
        val vec = EntityUtil.getInterpolatedPosition(player).add(
            Vec3d(
                -mc.renderManager.viewerPosX,
                -mc.renderManager.viewerPosY + player.getEyeHeight() + 0.5 + if (player.isSneaking) -0.2 else 0.0,
                -mc.renderManager.viewerPosZ
            )
        )

        // Change vec if elytra flying
        if (player.isElytraFlying) vec.add(Vec3d(PlayerUtil.forward(2.0)[0], -0.8, PlayerUtil.forward(2.0)[2]))

        var i = 0.0
        while (i < Math.PI * 2) {
            i += Math.PI * 4 / 128 // There is no classic for loop in kt

            // Set bottom colour
            ColourUtil.setColour(bottomColour.getValue().rgb)

            // Add bottom point
            GL11.glVertex3d(vec.x + 0.65 * cos(i), vec.y - 0.25, vec.z + 0.65 * sin(i))

            // Set top colour
            ColourUtil.setColour(topColour.getValue().rgb)

            // Add top point
            GL11.glVertex3d(vec.x, vec.y, vec.z)
        }

        GL11.glEnd()
        GL11.glShadeModel(GL11.GL_FLAT)
        GL11.glDepthMask(true)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GlStateManager.enableCull()
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_POINT_SMOOTH)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glPopMatrix()
    }

}