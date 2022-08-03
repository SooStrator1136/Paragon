package com.paragon.client.systems.module.impl.render

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.entity.EntityUtil
import com.paragon.api.util.render.ColourUtil.integrateAlpha
import com.paragon.api.util.render.ColourUtil.setColour
import com.paragon.api.util.render.RenderUtil.drawBoundingBox
import com.paragon.api.util.render.RenderUtil.drawFilledBox
import com.paragon.asm.mixins.accessor.IMinecraft
import com.paragon.asm.mixins.accessor.IRenderManager
import net.minecraft.item.*
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object Trajectories : Module("Trajectories", Category.RENDER, "Shows where projectiles will land") {

    private val line = Setting("Line", true)
        .setDescription("Render a line to the projectile's destination")

    private val lineColour = Setting("LineColour", Color(185, 17, 255))
        .setDescription("The colour of the line")
        .setParentSetting(line)

    private val lineWidth = Setting("LineWidth", 1.0f, 0.1f, 3.0f, 0.1f)
        .setDescription("The width of the line")
        .setParentSetting(line)

    private val box = Setting("Box", true)
        .setDescription("Render a box at the projectile's destination")

    private val fill = Setting("Fill", true)
        .setDescription("Fill the box at the end of the line")
        .setParentSetting(box)

    private val outline = Setting("Outline", true)
        .setDescription("Outline the box at the end of the line")
        .setParentSetting(box)

    private val outlineWidth = Setting("OutlineWidth", 1.0f, 0.1f, 3.0f, 0.1f)
        .setDescription("The width of the outline")
        .setParentSetting(box)
        .setVisibility(outline::value)

    private val boxColour = Setting("BoxColour", Color(185, 17, 255, 130))
        .setDescription("The colour of the box at the end of the line")
        .setParentSetting(box)
        .setVisibility { fill.value || outline.value }

    private val bow = Setting("Bow", true)
        .setDescription("Draw the trajectory of the bow")

    private val snowball = Setting("Snowball", true)
        .setDescription("Draw the trajectory of snowballs")

    private val egg = Setting("Egg", true)
        .setDescription("Draw the trajectory of eggs")

    private val exp = Setting("EXP", true)
        .setDescription("Draw the trajectory of EXP bottles")

    private val potion = Setting("Potion", true)
        .setDescription("Draw the trajectory of splash potions")

    override fun onRender3D() {
        if (nullCheck()) {
            return
        }

        val stack = minecraft.player.heldItemMainhand

        // Check the item we are holding is a projectile (or a bow) and that projectile is enabled
        if (stack.item is ItemBow && bow.value || stack.item is ItemSnowball && snowball.value || stack.item is ItemEgg && egg.value || stack.item is ItemSplashPotion && potion.value || stack.item is ItemExpBottle && exp.value) {
            // If we are holding a bow, make sure we are charging
            if (stack.item is ItemBow && !minecraft.player.isHandActive) {
                return
            }

            // Original arrow position
            var position = Vec3d(
                minecraft.player.lastTickPosX + (minecraft.player.posX - minecraft.player.lastTickPosX) * (minecraft as IMinecraft).timer.renderPartialTicks - cos(
                    Math.toRadians(minecraft.player.rotationYaw.toDouble()).toFloat().toDouble()
                ) * 0.16f,
                minecraft.player.lastTickPosY + (minecraft.player.posY - minecraft.player.lastTickPosY) * (minecraft as IMinecraft).timer.renderPartialTicks + minecraft.player.getEyeHeight() - 0.15,
                minecraft.player.lastTickPosZ + (minecraft.player.posZ - minecraft.player.lastTickPosZ) * (minecraft as IMinecraft).timer.renderPartialTicks - sin(
                    Math.toRadians(minecraft.player.rotationYaw.toDouble()).toFloat().toDouble()
                ) * 0.16f
            )

            // Original arrow velocity
            var velocity = Vec3d(
                -sin(Math.toRadians(minecraft.player.rotationYaw.toDouble())) * cos(Math.toRadians(minecraft.player.rotationPitch.toDouble())) * if (stack.item is ItemBow) 1.0f else 0.4f,
                -sin(Math.toRadians(minecraft.player.rotationPitch.toDouble())) * if (stack.item is ItemBow) 1.0f else 0.4f,
                cos(Math.toRadians(minecraft.player.rotationYaw.toDouble())) * cos(Math.toRadians(minecraft.player.rotationPitch.toDouble())) * if (stack.item is ItemBow) 1.0f else 0.4f
            )

            // Motion factor
            val motion = sqrt(velocity.x * velocity.x + velocity.y * velocity.y + velocity.z * velocity.z)

            // New velocity
            velocity = Vec3d(velocity.x / motion, velocity.y / motion, velocity.z / motion)

            // If we are holding a bow
            velocity = if (stack.item is ItemBow) {
                // Get the charge power
                val power = MathHelper.clamp(
                    ((72000 - minecraft.player.itemInUseCount) / 20.0f * ((72000 - minecraft.player.itemInUseCount) / 20.0f) + (72000 - minecraft.player.itemInUseCount) / 20.0f * 2.0f) / 3.0f,
                    0f,
                    1f
                ) * 3

                // Set velocity
                Vec3d(velocity.x * power, velocity.y * power, velocity.z * power)
            } else {
                Vec3d(velocity.x * 1.5, velocity.y * 1.5, velocity.z * 1.5)
            }

            // Check we want to draw the line
            if (line.value) {
                // GL render 3D
                glPushMatrix()
                glDisable(GL_TEXTURE_2D)
                glEnable(GL_BLEND)
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
                glDisable(GL_DEPTH_TEST)
                glDepthMask(false)
                glEnable(GL_LINE_SMOOTH)

                // Set line width
                glLineWidth(lineWidth.value)
                setColour(lineColour.value.rgb)
                glBegin(GL_LINE_STRIP)

                // Add vertices to the line whilst we haven't hit a target
                for (i in 0..999) {
                    // Add vertex
                    glVertex3d(
                        position.x - (minecraft.renderManager as IRenderManager).renderX,
                        position.y - (minecraft.renderManager as IRenderManager).renderY,
                        position.z - (minecraft.renderManager as IRenderManager).renderZ
                    )

                    // Move position
                    position = Vec3d(
                        position.x + velocity.x * 0.1,
                        position.y + velocity.y * 0.1,
                        position.z + velocity.z * 0.1
                    )

                    velocity = Vec3d(
                        velocity.x,
                        velocity.y - (if (stack.item is ItemBow) 0.05 else if (stack.item is ItemPotion) 0.4 else if (stack.item is ItemExpBottle) 0.1 else 0.03) * 0.1,
                        velocity.z
                    )

                    // Check if we hit a target
                    val result = minecraft.world.rayTraceBlocks(
                        EntityUtil.getInterpolatedPosition(minecraft.player).add(
                            Vec3d(0.0, minecraft.player.getEyeHeight().toDouble(), 0.0)
                        ),
                        Vec3d(position.x, position.y, position.z)
                    )
                    if (result != null) {
                        break
                    }
                }

                // Stop adding vertices
                glEnd()

                // Disable GL render 3D
                glDisable(GL_BLEND)
                glEnable(GL_TEXTURE_2D)
                glEnable(GL_DEPTH_TEST)
                glDepthMask(true)
                glDisable(GL_LINE_SMOOTH)
                glPopMatrix()
            }

            // Check we want to draw the box
            if (box.value) {
                // Get highlight bb
                val bb = AxisAlignedBB(
                    position.x - (minecraft.renderManager as IRenderManager).renderX - 0.25,
                    position.y - (minecraft.renderManager as IRenderManager).renderY - 0.25,
                    position.z - (minecraft.renderManager as IRenderManager).renderZ - 0.25,
                    position.x - (minecraft.renderManager as IRenderManager).renderX + 0.25,
                    position.y - (minecraft.renderManager as IRenderManager).renderY + 0.25,
                    position.z - (minecraft.renderManager as IRenderManager).renderZ + 0.25
                )

                // Draw filled box
                if (fill.value) {
                    drawFilledBox(bb, boxColour.value)
                }

                // Draw outline box
                if (outline.value) {
                    drawBoundingBox(bb, outlineWidth.value, integrateAlpha(boxColour.value, 255f))
                }
            }
        }
    }
}