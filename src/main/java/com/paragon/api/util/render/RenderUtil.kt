package com.paragon.api.util.render

import com.paragon.api.util.Wrapper
import com.paragon.api.util.entity.EntityUtil
import com.paragon.api.util.render.ColourUtil.setColour
import com.paragon.api.util.render.font.FontUtil.defaultFont
import com.paragon.client.systems.module.impl.client.ClientFont
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.awt.Toolkit
import kotlin.math.cos
import kotlin.math.sin

@SideOnly(Side.CLIENT)
object RenderUtil : Wrapper {
    private val tessellator = Tessellator.getInstance()
    private val bufferBuilder = tessellator.buffer

    /**
     * Draws a rectangle at the given coordinates
     *
     * @param x      The X (left) coord
     * @param y      The Y (top) coord
     * @param width  The width of the rectangle
     * @param height The height of the rectangle
     * @param colour The colour of the rectangle
     */
    @JvmStatic
    fun drawRect(x: Float, y: Float, width: Float, height: Float, colour: Int) {
        val c = (colour shr 24 and 255).toFloat() / 255.0f
        val c1 = (colour shr 16 and 255).toFloat() / 255.0f
        val c2 = (colour shr 8 and 255).toFloat() / 255.0f
        val c3 = (colour and 255).toFloat() / 255.0f
        GlStateManager.pushMatrix()
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        )
        GlStateManager.shadeModel(7425)
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos((x + width).toDouble(), y.toDouble(), 0.0).color(c1, c2, c3, c).endVertex()
        bufferbuilder.pos(x.toDouble(), y.toDouble(), 0.0).color(c1, c2, c3, c).endVertex()
        bufferbuilder.pos(x.toDouble(), (y + height).toDouble(), 0.0).color(c1, c2, c3, c).endVertex()
        bufferbuilder.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).color(c1, c2, c3, c).endVertex()
        tessellator.draw()
        GlStateManager.shadeModel(7424)
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.popMatrix()
    }

    fun drawHorizontalGradientRect(x: Float, y: Float, width: Float, height: Float, leftColour: Int, rightColour: Int) {
        val left = Color(leftColour)
        val right = Color(rightColour)
        val lA = left.alpha / 255.0f
        val lG = left.green / 255.0f
        val lB = left.blue / 255.0f
        val lR = left.red / 255.0f
        val rA = right.alpha / 255.0f
        val rG = right.green / 255.0f
        val rB = right.blue / 255.0f
        val rR = right.red / 255.0f
        GlStateManager.pushMatrix()
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        )
        GlStateManager.shadeModel(7425)
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos((x + width).toDouble(), y.toDouble(), 0.0).color(rR, rG, rB, rA).endVertex()
        bufferbuilder.pos(x.toDouble(), y.toDouble(), 0.0).color(lR, lG, lB, lA).endVertex()
        bufferbuilder.pos(x.toDouble(), (y + height).toDouble(), 0.0).color(lR, lG, lB, lA).endVertex()
        bufferbuilder.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).color(rR, rG, rB, rA).endVertex()
        tessellator.draw()
        GlStateManager.shadeModel(7424)
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.popMatrix()
    }

    fun drawVerticalGradientRect(x: Float, y: Float, width: Float, height: Float, topColour: Int, bottomColour: Int) {
        val top = Color(topColour)
        val bottom = Color(bottomColour)
        GlStateManager.pushMatrix()
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        )
        GlStateManager.shadeModel(7425)
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos((x + width).toDouble(), y.toDouble(), 0.0)
            .color(top.red / 255f, top.green / 255f, top.blue / 255f, top.alpha / 255f).endVertex()
        bufferbuilder.pos(x.toDouble(), y.toDouble(), 0.0)
            .color(top.red / 255f, top.green / 255f, top.blue / 255f, top.alpha / 255f).endVertex()
        bufferbuilder.pos(x.toDouble(), (y + height).toDouble(), 0.0)
            .color(bottom.red / 255f, bottom.green / 255f, bottom.blue / 255f, bottom.alpha / 255f).endVertex()
        bufferbuilder.pos((x + width).toDouble(), (y + height).toDouble(), 0.0)
            .color(bottom.red / 255f, bottom.green / 255f, bottom.blue / 255f, bottom.alpha / 255f).endVertex()
        tessellator.draw()
        GlStateManager.shadeModel(7424)
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.popMatrix()
    }

    @JvmStatic
    fun drawRoundedRect(x: Double, y: Double, width: Double, height: Double, tLeft: Double, tRight: Double, bLeft: Double, bRight: Double, colour: Int) {
        var x = x
        var y = y
        var width = width
        var height = height
        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)
        x *= 2.0
        y *= 2.0
        width *= 2.0
        height *= 2.0
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDepthMask(true)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST)
        glBegin(GL_POLYGON)
        setColour(colour)
        var i: Int
        i = 0
        while (i <= 90) {
            glVertex2d(
                x + tLeft + Math.sin(i * Math.PI / 180.0) * tLeft * -1.0,
                y + tLeft + Math.cos(i * Math.PI / 180.0) * tLeft * -1.0
            )
            i += 3
        }
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + bLeft + Math.sin(i * Math.PI / 180.0) * bLeft * -1.0,
                y + height - bLeft + Math.cos(i * Math.PI / 180.0) * bLeft * -1.0
            )
            i += 3
        }
        i = 0
        while (i <= 90) {
            glVertex2d(
                x + width - bRight + Math.sin(i * Math.PI / 180.0) * bRight,
                y + height - bRight + Math.cos(i * Math.PI / 180.0) * bRight
            )
            i += 3
        }
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + width - tRight + Math.sin(i * Math.PI / 180.0) * tRight,
                y + tRight + Math.cos(i * Math.PI / 180.0) * tRight
            )
            i += 3
        }
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE)
        glHint(GL_POLYGON_SMOOTH_HINT, GL_DONT_CARE)
        glScaled(2.0, 2.0, 2.0)
        glPopAttrib()
        glLineWidth(1f)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }

    fun drawRoundedOutline(x: Double, y: Double, width: Double, height: Double, tLeft: Double, tRight: Double, bLeft: Double, bRight: Double, lineWidth: Float, colour: Int) {
        var x = x
        var y = y
        var width = width
        var height = height
        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)
        x *= 2.0
        y *= 2.0
        width *= 2.0
        height *= 2.0
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDepthMask(true)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST)
        glLineWidth(lineWidth)
        glBegin(GL_LINE_STRIP)
        setColour(colour)
        var i = 0
        while (i <= 90) {
            glVertex2d(
                x + tLeft + Math.sin(i * Math.PI / 180.0) * tLeft * -1.0,
                y + tLeft + Math.cos(i * Math.PI / 180.0) * tLeft * -1.0
            )
            i += 3
        }
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + bLeft + Math.sin(i * Math.PI / 180.0) * bLeft * -1.0,
                y + height - bLeft + Math.cos(i * Math.PI / 180.0) * bLeft * -1.0
            )
            i += 3
        }
        i = 0
        while (i <= 90) {
            glVertex2d(
                x + width - bRight + Math.sin(i * Math.PI / 180.0) * bRight,
                y + height - bRight + Math.cos(i * Math.PI / 180.0) * bRight
            )
            i += 3
        }
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + width - tRight + Math.sin(i * Math.PI / 180.0) * tRight,
                y + tRight + Math.cos(i * Math.PI / 180.0) * tRight
            )
            i += 3
        }
        i = 0
        while (i <= 90) {
            glVertex2d(
                x + tLeft + sin(i * Math.PI / 180.0) * tLeft * -1.0,
                y + tLeft + cos(i * Math.PI / 180.0) * tLeft * -1.0
            )
            i += 3
        }
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE)
        glHint(GL_POLYGON_SMOOTH_HINT, GL_DONT_CARE)
        glScaled(2.0, 2.0, 2.0)
        glPopAttrib()
        glLineWidth(1f)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }

    @JvmStatic
    fun drawHorizontalGradientRoundedRect(x: Double, y: Double, width: Double, height: Double, tLeft: Double, tRight: Double, bLeft: Double, bRight: Double, left: Int, right: Int) {
        var x = x
        var y = y
        var width = width
        var height = height
        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)
        x *= 2.0
        y *= 2.0
        width *= 2.0
        height *= 2.0
        glDisable(GL_DEPTH_TEST)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDepthMask(true)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST)
        glBegin(GL_POLYGON)
        var i: Int
        setColour(left)
        i = 0
        while (i <= 90) {
            glVertex2d(
                x + tLeft + Math.sin(i * Math.PI / 180.0) * tLeft * -1.0,
                y + tLeft + Math.cos(i * Math.PI / 180.0) * tLeft * -1.0
            )
            i += 3
        }
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + bLeft + Math.sin(i * Math.PI / 180.0) * bLeft * -1.0,
                y + height - bLeft + Math.cos(i * Math.PI / 180.0) * bLeft * -1.0
            )
            i += 3
        }
        setColour(right)
        i = 0
        while (i <= 90) {
            glVertex2d(
                x + width - bRight + Math.sin(i * Math.PI / 180.0) * bRight,
                y + height - bRight + Math.cos(i * Math.PI / 180.0) * bRight
            )
            i += 3
        }
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + width - tRight + Math.sin(i * Math.PI / 180.0) * tRight,
                y + tRight + Math.cos(i * Math.PI / 180.0) * tRight
            )
            i += 3
        }
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE)
        glHint(GL_POLYGON_SMOOTH_HINT, GL_DONT_CARE)
        glScaled(2.0, 2.0, 2.0)
        glPopAttrib()
        glLineWidth(1f)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }

    @JvmStatic
    fun drawBorder(x: Float, y: Float, width: Float, height: Float, border: Float, colour: Int) {
        // Left
        drawRect(x - border, y, border, height, colour)

        // Right
        drawRect(x + width, y, border, height, colour)

        // Top
        drawRect(x - border, y - border, width + border * 2, border, colour)

        // Bottom
        drawRect(x - border, y + height, width + border * 2, border, colour)
    }

    /**
     * Starts scissoring a rect
     *
     * @param x      X coord
     * @param y      Y coord
     * @param width  Width of scissor
     * @param height Height of scissor
     */
    @JvmStatic
    fun pushScissor(x: Double, y: Double, width: Double, height: Double) {
        var width = width
        var height = height
        width = MathHelper.clamp(width, 0.0, width)
        height = MathHelper.clamp(height, 0.0, height)
        glPushAttrib(GL_SCISSOR_BIT)
        run {
            scissorRect(x, y, width, height)
            glEnable(GL_SCISSOR_TEST)
        }
    }

    /**
     * Disables scissor
     */
    @JvmStatic
    fun popScissor() {
        glDisable(GL_SCISSOR_TEST)
        glPopAttrib()
    }

    /**
     * Scissors a rect
     *
     * @param x      X coord
     * @param y      Y coord
     * @param width  Width of scissor
     * @param height Height of scissor
     */
    fun scissorRect(x: Double, y: Double, width: Double, height: Double) {
        var x = x
        var y = y
        var width = width
        var height = height
        val sr = ScaledResolution(minecraft)
        val scale = sr.scaleFactor.toDouble()
        y = sr.scaledHeight - y
        x *= scale
        y *= scale
        width *= scale
        height *= scale
        glScissor(x.toInt(), (y - height).toInt(), width.toInt(), height.toInt())
    }

    /**
     * Draws a line from one pos to another
     *
     * @param x1           Start X
     * @param y1           Start Y
     * @param z1           Start Z
     * @param x2           End X
     * @param y2           End Y
     * @param z2           End Z
     * @param color        The colour of the line
     * @param disableDepth Disable GL depth
     * @param lineWidth    Width of the line
     */
    fun drawLine3D(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double, color: Int, disableDepth: Boolean, lineWidth: Float) {
        // Enable render 3D
        if (disableDepth) {
            glDepthMask(false)
            glDisable(GL_DEPTH_TEST)
        }
        glDisable(GL_ALPHA_TEST)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        glLineWidth(0.1f)

        // Colour line
        setColour(color)

        // Set line width
        glLineWidth(lineWidth)
        glBegin(GL_CURRENT_BIT)

        // Draw line
        glVertex3d(x1, y1, z1)
        glVertex3d(x2, y2, z2)
        glEnd()

        // Disable render 3D
        if (disableDepth) {
            glDepthMask(true)
            glEnable(GL_DEPTH_TEST)
        }
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glEnable(GL_ALPHA_TEST)
        glDisable(GL_LINE_SMOOTH)

        // Reset colour
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
    }

    /**
     * Draws a tracer to a given entity
     *
     * @param e         The entity to draw a line to
     * @param lineWidth The width of the line
     * @param col       The colour of the line
     */
    fun drawTracer(e: Entity, lineWidth: Float, col: Color) {
        val vec = EntityUtil.getInterpolatedPosition(e)
        val x = vec.x - minecraft.renderManager.viewerPosX
        val y = vec.y - minecraft.renderManager.viewerPosY
        val z = vec.z - minecraft.renderManager.viewerPosZ
        val eyes =
            Vec3d(0.0, 0.0, 1.0).rotatePitch(-Math.toRadians(minecraft.player.rotationPitch.toDouble()).toFloat())
                .rotateYaw(
                    -Math.toRadians(
                        minecraft.player.rotationYaw.toDouble()
                    ).toFloat()
                )
        if (col.alpha == 0) return
        drawLine3D(
            eyes.x,
            eyes.y + minecraft.player.getEyeHeight(),
            eyes.z,
            x,
            y + e.height / 2,
            z,
            col.rgb,
            true,
            lineWidth
        )
    }

    /**
     * Draws a bounding box around an AABB
     *
     * @param axisAlignedBB The AABB
     * @param lineThickness The line width
     * @param colour        The colour of the outline
     */
    @JvmStatic
    fun drawBoundingBox(axisAlignedBB: AxisAlignedBB?, lineThickness: Float, colour: Color) {
        glBlendFunc(770, 771)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glLineWidth(lineThickness)
        RenderGlobal.drawSelectionBoundingBox(
            axisAlignedBB,
            colour.red / 255f,
            colour.green / 255f,
            colour.blue / 255f,
            colour.alpha / 255f
        )
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
    }

    /**
     * Draws a filled box around an AABB
     *
     * @param axisAlignedBB The AABB
     * @param colour        The colour of the outline
     */
    @JvmStatic
    fun drawFilledBox(axisAlignedBB: AxisAlignedBB?, colour: Color) {
        glBlendFunc(770, 771)
        glEnable(GL_BLEND)
        glLineWidth(1f)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        RenderGlobal.renderFilledBox(
            axisAlignedBB,
            colour.red / 255f,
            colour.green / 255f,
            colour.blue / 255f,
            colour.alpha / 255f
        )
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
    }

    fun drawGradientBox(axisAlignedBB: AxisAlignedBB, top: Color, bottom: Color) {
        glBlendFunc(770, 771)
        glEnable(GL_BLEND)
        glLineWidth(1f)
        glColor4d(0.0, 1.0, 0.0, 0.15)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glColor4d(0.0, 0.0, 1.0, 0.5)
        glPushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.enableDepth()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ZERO, GL_ONE)
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        GlStateManager.disableCull()
        GlStateManager.disableAlpha()
        GlStateManager.shadeModel(GL_SMOOTH)
        bufferBuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
        addGradientBoxVertices(bufferBuilder, axisAlignedBB, bottom, top)
        tessellator.draw()
        GlStateManager.enableCull()
        GlStateManager.enableAlpha()
        GlStateManager.shadeModel(GL_FLAT)
        glDisable(GL_LINE_SMOOTH)
        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
    }

    fun addGradientBoxVertices(builder: BufferBuilder, bb: AxisAlignedBB, topColour: Color, bottomColour: Color) {
        val minX = bb.minX
        val minY = bb.minY
        val minZ = bb.minZ
        val maxX = bb.maxX
        val maxY = bb.maxY
        val maxZ = bb.maxZ
        val red = topColour.red / 255f
        val green = topColour.green / 255f
        val blue = topColour.blue / 255f
        val alpha = topColour.alpha / 255f
        val red1 = bottomColour.red / 255f
        val green1 = bottomColour.green / 255f
        val blue1 = bottomColour.blue / 255f
        val alpha1 = bottomColour.alpha / 255f
        builder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(minX, maxY, minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(minX, maxY, maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(maxX, maxY, maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(maxX, maxY, minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(minX, maxY, minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(maxX, maxY, minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(maxX, maxY, minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(maxX, maxY, maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(maxX, maxY, maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(minX, maxY, maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(minX, maxY, maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(minX, maxY, minZ).color(red1, green1, blue1, alpha1).endVertex()
    }

    @JvmStatic
    fun drawNametagText(text: String?, location: Vec3d, textColour: Int) {
        GlStateManager.pushMatrix()
        // Translate
        val scale = 0.02666667f
        GlStateManager.translate(
            location.x - minecraft.renderManager.viewerPosX,
            location.y - minecraft.renderManager.viewerPosY,
            location.z - minecraft.renderManager.viewerPosZ
        )
        GlStateManager.rotate(-minecraft.player.rotationYaw, 0f, 1f, 0f)

        // Rotate based on the view
        GlStateManager.rotate(minecraft.player.rotationPitch, if (minecraft.gameSettings.thirdPersonView == 2) -1f else 1.toFloat(), 0f, 0f)
        GlStateManager.scale(-scale, -scale, scale)
        GlStateManager.disableDepth()
        GlStateManager.translate(-(getStringWidth(text) / 2), 0f, 0f)
        renderText(text, 0f, 0f, textColour)
        GlStateManager.enableDepth()
        GlStateManager.popMatrix()
    }

    @JvmStatic
    fun renderItemStack(itemStack: ItemStack?, x: Float, y: Float, overlay: Boolean) {
        val renderItem = minecraft.renderItem
        GlStateManager.enableDepth()
        renderItem.zLevel = 200f
        renderItem.renderItemAndEffectIntoGUI(itemStack, x.toInt(), y.toInt())

        if (overlay) {
            renderItem.renderItemOverlays(minecraft.fontRenderer, itemStack, x.toInt(), y.toInt())
        }

        renderItem.zLevel = 0f
        GlStateManager.enableTexture2D()
        GlStateManager.disableLighting()
        GlStateManager.enableDepth()
    }

    @JvmStatic
    fun drawModalRectWithCustomSizedTexture(x: Float, y: Float, u: Float, v: Float, width: Float, height: Float, textureWidth: Float, textureHeight: Float) {
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        val f = 1.0f / textureWidth
        val f1 = 1.0f / textureHeight
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX)
        bufferbuilder.pos(x.toDouble(), (y + height).toDouble(), 0.0)
            .tex((u * f).toDouble(), ((v + height) * f1).toDouble()).endVertex()
        bufferbuilder.pos((x + width).toDouble(), (y + height).toDouble(), 0.0)
            .tex(((u + width) * f).toDouble(), ((v + height) * f1).toDouble()).endVertex()
        bufferbuilder.pos((x + width).toDouble(), y.toDouble(), 0.0)
            .tex(((u + width) * f).toDouble(), (v * f1).toDouble()).endVertex()
        bufferbuilder.pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * f1).toDouble()).endVertex()
        tessellator.draw()
    }

    @JvmStatic
    val screenWidth: Float
        get() = Toolkit.getDefaultToolkit().screenSize.width / 2f
    
    @JvmStatic
    val screenHeight: Float
        get() = Toolkit.getDefaultToolkit().screenSize.height / 2f

    fun renderText(text: String?, x: Float, y: Float, colour: Int) {
        if (ClientFont.isEnabled) {
            defaultFont.drawString(text!!, x, y - 3.5f, colour, false)
            return
        }
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, colour)
    }

    fun getStringWidth(text: String?): Float {
        return if (ClientFont.isEnabled) {
            defaultFont.getStringWidth(text!!).toFloat()
        } else Minecraft.getMinecraft().fontRenderer.getStringWidth(text).toFloat()
    }
}