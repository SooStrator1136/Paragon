package com.paragon.client.systems.module.impl.render

import com.paragon.api.event.render.entity.RenderCrystalEvent
import com.paragon.api.event.render.entity.RenderEntityEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.RubiksCrystalUtil
import me.surge.animation.Easing
import me.wolfsurge.cerauno.listener.Listener
import net.minecraft.client.Minecraft
import net.minecraft.client.model.ModelRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11.*
import org.lwjgl.util.vector.Quaternion
import java.awt.Color
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.cos
import kotlin.math.sin

/**
 * Rubik's crystals based off of rebane2001's and olliem5's Rubiks crystals
 *
 * otherwise
 * @author Surge
 */
object Chams : Module("Chams", Category.RENDER, "Shows entities through walls") {

    private val mode = Setting("Mode", Mode.WIRE_MODEL)
        .setDescription("The render mode to use")

    private val width = Setting("Width", 1f, 0.1f, 3f, 0.1f)
        .setDescription("The width of the lines")
        .setVisibility { mode.value != Mode.MODEL }

    // Entity filters
    private val players = Setting("Players", true)
        .setDescription("Highlight players")

    private val mobs = Setting("Mobs", true)
        .setDescription("Highlight mobs")

    private val passives = Setting("Passives", true)
        .setDescription("Highlight passives")

    // Crystals
    private val crystals = Setting("Crystals", true)
        .setDescription("Highlight crystals")

    private val bounce = Setting("Bounce", false)
        .setDescription("Make the crystals bounce like they do in vanilla")

    private val scaleSetting = Setting("Scale", 0.6f, 0.0f, 1f, 0.01f)
        .setDescription("The scale of the crystal")
        .setParentSetting(crystals)

    private val rubiks = Setting("Rubik's Cube", false)
        .setDescription("Make end crystals look like Rubik's cubes")
        .setParentSetting(crystals)

    private val time = Setting("Time", 400f, 200f, 1000f, 1f)
        .setDescription("The time it takes for a side to rotate")
        .setParentSetting(crystals)
        .setVisibility { rubiks.value }

    private val cube = Setting("Cube", true)
        .setDescription("Render the crystal cube")
        .setParentSetting(crystals)

    private val glass = Setting("Glass", true)
        .setDescription("Render the glass")
        .setParentSetting(crystals)

    // Render settings
    private val texture = Setting("Texture", false)
        .setDescription("Render the entity's texture")

    private val lighting = Setting("Lighting", true)
        .setDescription("Disables lighting")

    private val blend = Setting("Blend", true)
        .setDescription("Enables blending")

    private val transparent = Setting("Transparent", true)
        .setDescription("Enables transparency on models")

    private val depth = Setting("Depth", true)
        .setDescription("Enables depth")

    private val walls = Setting("Walls", true)
        .setDescription("Render entities through walls")

    private val colour = Setting("Colour", Color(185, 17, 255, 85))
        .setDescription("The colour of the highlight")

    private var rotating = 0
    private var lastTime: Long = 0
    private var cubeModel: ModelRenderer? = null
    private const val CUBELET_SCALE = 0.4

    @Listener
    fun onRenderEntity(event: RenderEntityEvent) {
        // Check entity is valid
        if (!isEntityValid(event.entity)) {
            return
        }

        // Cancel model render
        if (!texture.value) {
            event.cancel()
        }

        glPushMatrix()
        glPushAttrib(GL_ALL_ATTRIB_BITS)

        // Enable transparency
        if (transparent.value) {
            GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL)
        }

        // Disable texture
        if (!texture.value) {
            glDisable(GL_TEXTURE_2D)
        }
        val originalBlend = glIsEnabled(GL_BLEND)

        // Enable blend
        if (blend.value) {
            glEnable(GL_BLEND)
        }

        // Disable lighting
        if (lighting.value) {
            glDisable(GL_LIGHTING)
        }

        // Remove depth
        if (depth.value) {
            glDepthMask(false)
        }

        // Remove depth
        if (walls.value) {
            glDisable(GL_DEPTH_TEST)
        }
        when (mode.value) {
            Mode.WIRE -> glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
            Mode.WIRE_MODEL, Mode.MODEL -> glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
        }

        // Anti aliasing for smooth lines
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)

        // Set line width
        glLineWidth(width.value)

        // Set colour
        glColor4f(
            colour.value.red / 255f,
            colour.value.green / 255f,
            colour.value.blue / 255f,
            colour.alpha / 255f
        )

        // Render model
        event.renderModel()

        // Re enable depth
        if (walls.value && mode.value != Mode.WIRE_MODEL) {
            glEnable(GL_DEPTH_TEST)
        }

        // Change polygon rendering mode
        if (mode.value == Mode.WIRE_MODEL) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
        }
        glColor4f(
            colour.value.red / 255f,
            colour.value.green / 255f,
            colour.value.blue / 255f,
            if (mode.value == Mode.MODEL) colour.alpha / 255f else 1f
        )

        // Render model
        event.renderModel()

        // Re enable depth
        if (walls.value && mode.value == Mode.WIRE_MODEL) {
            glEnable(GL_DEPTH_TEST)
        }

        // Enable lighting
        if (lighting.value) {
            glEnable(GL_LIGHTING)
        }

        // Enable depth
        if (depth.value) {
            glDepthMask(true)
        }

        // Enable blending
        if (!originalBlend) {
            glDisable(GL_BLEND)
        }

        // Enable texture
        if (!texture.value) {
            glEnable(GL_TEXTURE_2D)
        }

        // Reset colour
        GlStateManager.color(1f, 1f, 1f, 1f)
        if (transparent.value) {
            GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL)
        }
        glPopAttrib()
        glPopMatrix()
    }

    @Listener
    fun onRenderCrystal(event: RenderCrystalEvent) {
        if (crystals.value) {
            // Cancel vanilla crystal render
            event.cancel()
            glPushMatrix()
            glPushAttrib(GL_ALL_ATTRIB_BITS)

            // Enable transparency
            if (transparent.value) {
                GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL)
            }

            // Disable texture
            if (!texture.value) {
                glDisable(GL_TEXTURE_2D)
            }

            // Enable blend
            if (blend.value) {
                glEnable(GL_BLEND)
            }

            // Disable lighting
            if (lighting.value) {
                glDisable(GL_LIGHTING)
            }

            // Remove depth
            if (depth.value) {
                glDepthMask(false)
            }

            // Remove depth
            if (walls.value) {
                glDisable(GL_DEPTH_TEST)
            }
            when (mode.value) {
                Mode.WIRE -> glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
                Mode.WIRE_MODEL, Mode.MODEL -> glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
            }

            // Anti aliasing for smooth lines
            glEnable(GL_LINE_SMOOTH)
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)

            // Set line width
            glLineWidth(width.value)

            // Set colour
            glColor4f(
                colour.value.red / 255f,
                colour.value.green / 255f,
                colour.value.blue / 255f,
                colour.alpha / 255f
            )

            // Render crystal
            renderCrystal(event)

            // Re enable depth
            if (walls.value && mode.value != Mode.WIRE_MODEL) {
                glEnable(GL_DEPTH_TEST)
            }

            // Change polygon rendering mode
            if (mode.value == Mode.WIRE_MODEL) {
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
            }
            glColor4f(
                colour.value.red / 255f,
                colour.value.green / 255f,
                colour.value.blue / 255f,
                if (mode.value == Mode.MODEL) colour.alpha / 255f else 1f
            )
            if (mode.value == Mode.WIRE_MODEL) {
                renderCrystal(event)
            }

            // Re enable depth
            if (walls.value && mode.value == Mode.WIRE_MODEL) {
                glEnable(GL_DEPTH_TEST)
            }

            // Enable lighting
            if (lighting.value) {
                glEnable(GL_LIGHTING)
            }

            // Enable depth
            if (depth.value) {
                glDepthMask(true)
            }

            // Enable blending
            if (blend.value) {
                glDisable(GL_BLEND)
            }

            // Enable texture
            if (!texture.value) {
                glEnable(GL_TEXTURE_2D)
            }

            // Reset colour
            GlStateManager.color(1f, 1f, 1f, 1f)

            // Reset scale
            if (rubiks.value) {
                GlStateManager.scale(1 / 0.5f, 1 / 0.5f, 1 / 0.5f)
            } else {
                GlStateManager.scale(1 / scaleSetting.value, 1 / scaleSetting.value, 1 / scaleSetting.value)
            }

            // Enable transparency
            if (transparent.value) {
                GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL)
            }
            glPopAttrib()
            glPopMatrix()
        }
    }

    private fun renderCrystal(event: RenderCrystalEvent) {
        if (rubiks.value) {
            cubeModel = event.cube
            GlStateManager.pushMatrix()
            GlStateManager.scale(2.0f, 2.0f, 2.0f)
            GlStateManager.translate(0.0f, -0.5f, 0.0f)
            if (event.base != null) {
                event.base.render(event.scale)
            }
            GlStateManager.rotate(event.limbSwingAmount, 0.0f, 1.0f, 0.0f)
            if (event.base != null) {
                GlStateManager.translate(0.0f, 1.2f, 0.0f)
            } else {
                GlStateManager.translate(0.0f, 1.0f, 0.0f)
            }
            GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f)
            GlStateManager.scale(0.875f, 0.875f, 0.875f)
            GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f)
            GlStateManager.rotate(event.limbSwingAmount, 0.0f, 1.0f, 0.0f)
            if (glass.value) {
                event.glass.render(event.scale)
            }
            GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f)
            GlStateManager.scale(0.875f, 0.875f, 0.875f)
            GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f)
            GlStateManager.rotate(event.limbSwingAmount, 0.0f, 1.0f, 0.0f)
            if (glass.value) {
                event.glass.render(event.scale)
            }
            GlStateManager.scale(0.875f, 0.875f, 0.875f)
            GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f)
            GlStateManager.rotate(event.limbSwingAmount, 0.0f, 1.0f, 0.0f)

            // Scale cubelets
            GlStateManager.scale(CUBELET_SCALE, CUBELET_SCALE, CUBELET_SCALE)
            event.scale = (event.scale * (CUBELET_SCALE * 2)).toFloat()
            val currentTime = Minecraft.getSystemTime()
            if (currentTime - time.value > lastTime) {
                val currentSide = RubiksCrystalUtil.cubeSides[rotating]
                val cubletsTemp = arrayOf(
                    RubiksCrystalUtil.cubeletStatus[currentSide[0]],
                    RubiksCrystalUtil.cubeletStatus[currentSide[1]],
                    RubiksCrystalUtil.cubeletStatus[currentSide[2]],
                    RubiksCrystalUtil.cubeletStatus[currentSide[3]],
                    RubiksCrystalUtil.cubeletStatus[currentSide[4]],
                    RubiksCrystalUtil.cubeletStatus[currentSide[5]],
                    RubiksCrystalUtil.cubeletStatus[currentSide[6]],
                    RubiksCrystalUtil.cubeletStatus[currentSide[7]],
                    RubiksCrystalUtil.cubeletStatus[currentSide[8]]
                )

                //Rotation direction
                RubiksCrystalUtil.cubeletStatus[currentSide[0]] = cubletsTemp[6]
                RubiksCrystalUtil.cubeletStatus[currentSide[1]] = cubletsTemp[3]
                RubiksCrystalUtil.cubeletStatus[currentSide[2]] = cubletsTemp[0]
                RubiksCrystalUtil.cubeletStatus[currentSide[3]] = cubletsTemp[7]
                RubiksCrystalUtil.cubeletStatus[currentSide[4]] = cubletsTemp[4]
                RubiksCrystalUtil.cubeletStatus[currentSide[5]] = cubletsTemp[1]
                RubiksCrystalUtil.cubeletStatus[currentSide[6]] = cubletsTemp[8]
                RubiksCrystalUtil.cubeletStatus[currentSide[7]] = cubletsTemp[5]
                RubiksCrystalUtil.cubeletStatus[currentSide[8]] = cubletsTemp[2]

                val trans = RubiksCrystalUtil.cubeSideTransforms[rotating]
                for (x in -1..1) {
                    for (y in -1..1) {
                        for (z in -1..1) {
                            if (x != 0 || y != 0 || z != 0) {
                                applyRotation(x, y, z, trans[0], trans[1], trans[2])
                            }
                        }
                    }
                }
                rotating = ThreadLocalRandom.current().nextInt(0, 5 + 1)
                lastTime = currentTime
            }
            for (x in -1..1) {
                for (y in -1..1) {
                    for (z in -1..1) {
                        if (x != 0 || y != 0 || z != 0) {
                            if (cube.value) {
                                drawCubeletStatic(event.scale, x, y, z)
                            }
                        }
                    }
                }
            }

            val trans = RubiksCrystalUtil.cubeSideTransforms[rotating]
            GlStateManager.pushMatrix()
            GlStateManager.translate(trans[0] * CUBELET_SCALE, trans[1] * CUBELET_SCALE, trans[2] * CUBELET_SCALE)
            val angle = Math.toRadians(
                Easing.EXPO_IN_OUT.ease(((currentTime - lastTime).toFloat() / time.value).toDouble())
            ).toFloat() * 90

            val xx = (trans[0] * sin((angle / 2).toDouble())).toFloat()
            val yy = (trans[1] * sin((angle / 2).toDouble())).toFloat()
            val zz = (trans[2] * sin((angle / 2).toDouble())).toFloat()
            val ww = cos((angle / 2).toDouble()).toFloat()

            val q = Quaternion(xx, yy, zz, ww)
            GlStateManager.rotate(q)

            for (x in -1..1) {
                for (y in -1..1) {
                    for (z in -1..1) {
                        if (x != 0 || y != 0 || z != 0) {
                            if (cube.value) {
                                drawCubeletRotating(event.scale, x, y, z)
                            }
                        }
                    }
                }
            }
        } else {
            GlStateManager.pushMatrix()
            GlStateManager.scale(2.0f, 2.0f, 2.0f)
            GlStateManager.translate(0.0f, -0.5f, 0.0f)

            if (event.base != null) {
                event.base.render(event.scale)
            }

            GlStateManager.rotate(event.limbSwingAmount, 0.0f, 1.0f, 0.0f)
            GlStateManager.translate(0.0f, 0.8f + if (bounce.value) event.ageInTicks else 0f, 0.0f)
            GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f)
            GlStateManager.pushMatrix()
            GlStateManager.scale(scaleSetting.value, scaleSetting.value, scaleSetting.value)

            if (glass.value) {
                event.glass.render(event.scale)
            }

            GlStateManager.popMatrix()
            GlStateManager.scale(0.875f, 0.875f, 0.875f)
            GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f)
            GlStateManager.rotate(event.limbSwingAmount, 0.0f, 1.0f, 0.0f)
            GlStateManager.pushMatrix()
            GlStateManager.scale(scaleSetting.value, scaleSetting.value, scaleSetting.value)

            if (glass.value) {
                event.glass.render(event.scale)
            }

            GlStateManager.popMatrix()
            GlStateManager.scale(0.875f, 0.875f, 0.875f)
            GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f)
            GlStateManager.rotate(event.limbSwingAmount, 0.0f, 1.0f, 0.0f)
            GlStateManager.pushMatrix()
            GlStateManager.scale(scaleSetting.value, scaleSetting.value, scaleSetting.value)

            if (cube.value) {
                event.cube.render(event.scale)
            }
        }
        GlStateManager.popMatrix()
        GlStateManager.popMatrix()
    }

    private fun drawCubeletStatic(scale: Float, x: Int, y: Int, z: Int) {
        val id = RubiksCrystalUtil.cubeletLookup[x + 1][y + 1][z + 1]
        if (Arrays.stream(RubiksCrystalUtil.cubeSides[rotating]).anyMatch { i: Int -> i == id }) {
            return
        }
        drawCubelet(scale, x, y, z, id)
    }

    private fun drawCubeletRotating(scale: Float, x: Int, y: Int, z: Int) {
        val id = RubiksCrystalUtil.cubeletLookup[x + 1][y + 1][z + 1]
        if (Arrays.stream(RubiksCrystalUtil.cubeSides[rotating]).noneMatch { i: Int -> i == id }) {
            return
        }
        val transform = RubiksCrystalUtil.cubeSideTransforms[rotating]
        drawCubelet(scale, x - transform[0], y - transform[1], z - transform[2], id)
    }

    private fun applyRotation(x: Int, y: Int, z: Int, rX: Int, rY: Int, rZ: Int) {
        val id = RubiksCrystalUtil.cubeletLookup[x + 1][y + 1][z + 1]
        if (Arrays.stream(RubiksCrystalUtil.cubeSides[rotating]).noneMatch { i: Int -> i == id }) {
            return
        }
        val angle = Math.toRadians(90.0).toFloat()
        val xx = (rX * sin((angle / 2).toDouble())).toFloat()
        val yy = (rY * sin((angle / 2).toDouble())).toFloat()
        val zz = (rZ * sin((angle / 2).toDouble())).toFloat()
        val ww = cos((angle / 2).toDouble()).toFloat()
        RubiksCrystalUtil.cubeletStatus[id] =
            Quaternion.mul(Quaternion(xx, yy, zz, ww), RubiksCrystalUtil.cubeletStatus[id], null)
    }

    @Suppress("ReplaceNotNullAssertionWithElvisReturn")
    private fun drawCubelet(scale: Float, x: Int, y: Int, z: Int, id: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x * CUBELET_SCALE, y * CUBELET_SCALE, z * CUBELET_SCALE)
        GlStateManager.pushMatrix()
        GlStateManager.rotate(RubiksCrystalUtil.cubeletStatus[id])
        if (cube.value) {
            cubeModel!!.render(scale)
        }
        GlStateManager.popMatrix()
        GlStateManager.popMatrix()
    }

    /**
     * Checks if an entity is valid
     *
     * @param entityIn The entity to check
     * @return Is the entity valid
     */
    private fun isEntityValid(entityIn: Entity): Boolean {
        return entityIn is EntityPlayer && entityIn !== minecraft.player && players.value || entityIn is EntityLiving && entityIn !is EntityMob && passives.value || entityIn is EntityMob && mobs.value
    }

    enum class Mode {
        /**
         * Renders the model
         */
        MODEL,

        /**
         * Outlines the model
         */
        WIRE,

        /**
         * Renders and outlines the model
         */
        WIRE_MODEL
    }

}