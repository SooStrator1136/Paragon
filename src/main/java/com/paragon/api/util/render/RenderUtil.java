package com.paragon.api.util.render;

import com.paragon.Paragon;
import com.paragon.api.util.Wrapper;
import com.paragon.api.util.entity.EntityUtil;
import com.paragon.client.systems.module.impl.client.ClientFont;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@SideOnly(Side.CLIENT)
public class RenderUtil implements Wrapper {

    private static Tessellator tessellator = Tessellator.getInstance();
    private static BufferBuilder bufferBuilder = tessellator.getBuffer();

    /**
     * Draws a rectangle at the given coordinates
     *
     * @param x      The X (left) coord
     * @param y      The Y (top) coord
     * @param width  The width of the rectangle
     * @param height The height of the rectangle
     * @param colour The colour of the rectangle
     */
    public static void drawRect(float x, float y, float width, float height, int colour) {
        float c = (float) (colour >> 24 & 255) / 255.0F;
        float c1 = (float) (colour >> 16 & 255) / 255.0F;
        float c2 = (float) (colour >> 8 & 255) / 255.0F;
        float c3 = (float) (colour & 255) / 255.0F;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x + width, y, 0).color(c1, c2, c3, c).endVertex();
        bufferbuilder.pos(x, y, 0).color(c1, c2, c3, c).endVertex();
        bufferbuilder.pos(x, y + height, 0).color(c1, c2, c3, c).endVertex();
        bufferbuilder.pos(x + width, y + height, 0).color(c1, c2, c3, c).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(7424);
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    public static void drawHorizontalGradientRect(float x, float y, float width, float height, int leftColour, int rightColour) {
        Color left = new Color(leftColour);
        Color right = new Color(rightColour);

        float lA = left.getAlpha() / 255.0F;
        float lG = left.getGreen() / 255.0F;
        float lB = left.getBlue() / 255.0F;
        float lR = left.getRed() / 255.0F;

        float rA = right.getAlpha() / 255.0F;
        float rG = right.getGreen() / 255.0F;
        float rB = right.getBlue() / 255.0F;
        float rR = right.getRed() / 255.0F;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

        bufferbuilder.pos(x + width, y, 0).color(rR, rG, rB, rA).endVertex();

        bufferbuilder.pos(x, y, 0).color(lR, lG, lB, lA).endVertex();
        bufferbuilder.pos(x, y + height, 0).color(lR, lG, lB, lA).endVertex();

        bufferbuilder.pos(x + width, y + height, 0).color(rR, rG, rB, rA).endVertex();

        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    public static void drawVerticalGradientRect(float x, float y, float width, float height, int topColour, int bottomColour) {
        Color top = new Color(topColour);
        Color bottom = new Color(bottomColour);

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

        bufferbuilder.pos(x + width, y, 0).color(top.getRed() / 255f, top.getGreen() / 255f, top.getBlue() / 255f, top.getAlpha() / 255f).endVertex();
        bufferbuilder.pos(x, y, 0).color(top.getRed() / 255f, top.getGreen() / 255f, top.getBlue() / 255f, top.getAlpha() / 255f).endVertex();

        bufferbuilder.pos(x, y + height, 0).color(bottom.getRed() / 255f, bottom.getGreen() / 255f, bottom.getBlue() / 255f, bottom.getAlpha() / 255f).endVertex();
        bufferbuilder.pos(x + width, y + height, 0).color(bottom.getRed() / 255f, bottom.getGreen() / 255f, bottom.getBlue() / 255f, bottom.getAlpha() / 255f).endVertex();

        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    public static void drawRoundedRect(double x, double y, double width, double height, double tLeft, double tRight, double bLeft, double bRight, int colour) {
        glPushAttrib(0);
        glScaled(0.5D, 0.5D, 0.5D);
        x *= 2.0D;
        y *= 2.0D;
        width *= 2.0D;
        height *= 2.0D;

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(true);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);

        glBegin(GL_POLYGON);
        ColourUtil.setColour(colour);
        int i;

        for (i = 0; i <= 90; i += 3) {
            glVertex2d(x + tLeft + Math.sin(i * Math.PI / 180.0D) * tLeft * -1.0D, y + tLeft + Math.cos(i * Math.PI / 180.0D) * tLeft * -1.0D);
        }

        for (i = 90; i <= 180; i += 3) {
            glVertex2d(x + bLeft + Math.sin(i * Math.PI / 180.0D) * bLeft * -1.0D, y + height - bLeft + Math.cos(i * Math.PI / 180.0D) * bLeft * -1.0D);
        }

        for (i = 0; i <= 90; i += 3) {
            glVertex2d(x + width - bRight + Math.sin(i * Math.PI / 180.0D) * bRight, y + height - bRight + Math.cos(i * Math.PI / 180.0D) * bRight);
        }

        for (i = 90; i <= 180; i += 3) {
            glVertex2d(x + width - tRight + Math.sin(i * Math.PI / 180.0D) * tRight, y + tRight + Math.cos(i * Math.PI / 180.0D) * tRight);
        }

        glEnd();

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);
        glHint(GL_POLYGON_SMOOTH_HINT, GL_DONT_CARE);

        glScaled(2.0D, 2.0D, 2.0D);
        glPopAttrib();
        glLineWidth(1);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawHorizontalRoundedRect(double x, double y, double width, double height, double tLeft, double tRight, double bLeft, double bRight, int left, int right) {
        glPushAttrib(0);
        glScaled(0.5D, 0.5D, 0.5D);
        x *= 2.0D;
        y *= 2.0D;
        width *= 2.0D;
        height *= 2.0D;

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_TEXTURE_2D);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(true);

        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);

        glBegin(GL_POLYGON);

        int i;

        ColourUtil.setColour(left);
        for (i = 0; i <= 90; i += 3) {
            glVertex2d(x + tLeft + Math.sin(i * Math.PI / 180.0D) * tLeft * -1.0D, y + tLeft + Math.cos(i * Math.PI / 180.0D) * tLeft * -1.0D);
        }

        for (i = 90; i <= 180; i += 3) {
            glVertex2d(x + bLeft + Math.sin(i * Math.PI / 180.0D) * bLeft * -1.0D, y + height - bLeft + Math.cos(i * Math.PI / 180.0D) * bLeft * -1.0D);
        }

        ColourUtil.setColour(right);
        for (i = 0; i <= 90; i += 3) {
            glVertex2d(x + width - bRight + Math.sin(i * Math.PI / 180.0D) * bRight, y + height - bRight + Math.cos(i * Math.PI / 180.0D) * bRight);
        }

        for (i = 90; i <= 180; i += 3) {
            glVertex2d(x + width - tRight + Math.sin(i * Math.PI / 180.0D) * tRight, y + tRight + Math.cos(i * Math.PI / 180.0D) * tRight);
        }

        glEnd();

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);

        glDisable(GL_LINE_SMOOTH);

        glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);
        glHint(GL_POLYGON_SMOOTH_HINT, GL_DONT_CARE);

        glScaled(2.0D, 2.0D, 2.0D);
        glPopAttrib();
        glLineWidth(1);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawBorder(float x, float y, float width, float height, float border, int colour) {
        // Left
        drawRect(x - border, y, border, height, colour);

        // Right
        drawRect(x + width, y, border, height, colour);

        // Top
        drawRect(x - border, y - border, width + (border * 2), border, colour);

        // Bottom
        drawRect(x - border, y + height, width + (border * 2), border, colour);
    }

    /**
     * Starts scissoring a rect
     *
     * @param x      X coord
     * @param y      Y coord
     * @param width  Width of scissor
     * @param height Height of scissor
     */
    public static void startGlScissor(double x, double y, double width, double height) {
        glPushAttrib(GL_SCISSOR_BIT);
        {
            scissorRect(x, y, width, height);
            glEnable(GL_SCISSOR_TEST);
        }
    }

    /**
     * Disables scissor
     */
    public static void endGlScissor() {
        glDisable(GL_SCISSOR_TEST);
        glPopAttrib();
    }

    /**
     * Scissors a rect
     *
     * @param x      X coord
     * @param y      Y coord
     * @param width  Width of scissor
     * @param height Height of scissor
     */
    public static void scissorRect(double x, double y, double width, double height) {
        ScaledResolution sr = new ScaledResolution(mc);
        final double scale = sr.getScaleFactor();

        y = sr.getScaledHeight() - y;

        x *= scale;
        y *= scale;
        width *= scale;
        height *= scale;

        glScissor((int) x, (int) (y - height), (int) width, (int) height);
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
    public static void drawLine3D(double x1, double y1, double z1, double x2, double y2, double z2, int color, boolean disableDepth, float lineWidth) {
        // Enable render 3D
        if (disableDepth) {
            glDepthMask(false);
            glDisable(GL_DEPTH_TEST);
        }
        glDisable(GL_ALPHA_TEST);
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glLineWidth(0.1F);

        // Colour line
        ColourUtil.setColour(color);

        // Set line width
        glLineWidth(lineWidth);
        glBegin(GL_CURRENT_BIT);

        // Draw line
        glVertex3d(x1, y1, z1);
        glVertex3d(x2, y2, z2);

        glEnd();

        // Disable render 3D
        if (disableDepth) {
            glDepthMask(true);
            glEnable(GL_DEPTH_TEST);
        }
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glEnable(GL_ALPHA_TEST);
        glDisable(GL_LINE_SMOOTH);

        // Reset colour
        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Draws a tracer to a given entity
     *
     * @param e         The entity to draw a line to
     * @param lineWidth The width of the line
     * @param col       The colour of the line
     */
    public static void drawTracer(Entity e, float lineWidth, Color col) {
        Vec3d vec = EntityUtil.getInterpolatedPosition(e);
        double x = vec.x - mc.getRenderManager().viewerPosX;
        double y = vec.y - mc.getRenderManager().viewerPosY;
        double z = vec.z - mc.getRenderManager().viewerPosZ;

        Vec3d eyes = (new Vec3d(0.0D, 0.0D, 1.0D)).rotatePitch(-((float) Math.toRadians(mc.player.rotationPitch))).rotateYaw(-((float) Math.toRadians(mc.player.rotationYaw)));

        if (col.getAlpha() == 0) return;

        drawLine3D(eyes.x, eyes.y + mc.player.getEyeHeight(), eyes.z, x, y + (e.height / 2), z, col.getRGB(), true, lineWidth);
    }

    /**
     * Draws a bounding box around an AABB
     *
     * @param axisAlignedBB The AABB
     * @param lineThickness The line width
     * @param colour        The colour of the outline
     */
    public static void drawBoundingBox(AxisAlignedBB axisAlignedBB, float lineThickness, Color colour) {
        glBlendFunc(770, 771);
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        glLineWidth(lineThickness);
        RenderGlobal.drawSelectionBoundingBox(axisAlignedBB, colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue() / 255f, colour.getAlpha() / 255f);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDisable(GL_BLEND);
    }

    /**
     * Draws a filled box around an AABB
     *
     * @param axisAlignedBB The AABB
     * @param colour        The colour of the outline
     */
    public static void drawFilledBox(AxisAlignedBB axisAlignedBB, Color colour) {
        glBlendFunc(770, 771);
        glEnable(GL_BLEND);
        glLineWidth(1);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        RenderGlobal.renderFilledBox(axisAlignedBB, colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue() / 255f, colour.getAlpha() / 255f);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDisable(GL_BLEND);
    }

    public static void drawGradientBox(AxisAlignedBB axisAlignedBB, Color top, Color bottom) {
        glBlendFunc(770, 771);
        glEnable(GL_BLEND);
        glLineWidth(1);
        glColor4d(0, 1, 0, 0.15F);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        glColor4d(0, 0, 1, 0.5F);

        glPushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ZERO, GL_ONE);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(GL_SMOOTH);

        bufferBuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        addGradientBoxVertices(bufferBuilder, axisAlignedBB, bottom, top);
        tessellator.draw();

        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        GlStateManager.shadeModel(GL_FLAT);
        glDisable(GL_LINE_SMOOTH);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDisable(GL_BLEND);
    }

    public static void addGradientBoxVertices(BufferBuilder builder, AxisAlignedBB bb, Color topColour, Color bottomColour) {
        double minX = bb.minX;
        double minY = bb.minY;
        double minZ = bb.minZ;
        double maxX = bb.maxX;
        double maxY = bb.maxY;
        double maxZ = bb.maxZ;

        float red = topColour.getRed() / 255f;
        float green = topColour.getGreen() / 255f;
        float blue = topColour.getBlue() / 255f;
        float alpha = topColour.getAlpha() / 255f;

        float red1 = bottomColour.getRed() / 255f;
        float green1 = bottomColour.getGreen() / 255f;
        float blue1 = bottomColour.getBlue() / 255f;
        float alpha1 = bottomColour.getAlpha() / 255f;

        builder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
        builder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
        builder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        builder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        builder.pos(minX, maxY, minZ).color(red1, green1, blue1, alpha1).endVertex();
        builder.pos(minX, maxY, maxZ).color(red1, green1, blue1, alpha1).endVertex();
        builder.pos(maxX, maxY, maxZ).color(red1, green1, blue1, alpha1).endVertex();
        builder.pos(maxX, maxY, minZ).color(red1, green1, blue1, alpha1).endVertex();
        builder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
        builder.pos(minX, maxY, minZ).color(red1, green1, blue1, alpha1).endVertex();
        builder.pos(maxX, maxY, minZ).color(red1, green1, blue1, alpha1).endVertex();
        builder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
        builder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
        builder.pos(maxX, maxY, minZ).color(red1, green1, blue1, alpha1).endVertex();
        builder.pos(maxX, maxY, maxZ).color(red1, green1, blue1, alpha1).endVertex();
        builder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        builder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        builder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        builder.pos(maxX, maxY, maxZ).color(red1, green1, blue1, alpha1).endVertex();
        builder.pos(minX, maxY, maxZ).color(red1, green1, blue1, alpha1).endVertex();
        builder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
        builder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        builder.pos(minX, maxY, maxZ).color(red1, green1, blue1, alpha1).endVertex();
        builder.pos(minX, maxY, minZ).color(red1, green1, blue1, alpha1).endVertex();
    }

    public static void drawNametagText(String text, Vec3d location, int textColour) {
        GlStateManager.pushMatrix();
        // Translate
        float scale = 0.02666667f;

        GlStateManager.translate(location.x - mc.getRenderManager().viewerPosX, location.y - mc.getRenderManager().viewerPosY, location.z - mc.getRenderManager().viewerPosZ);
        GlStateManager.rotate(-mc.player.rotationYaw, 0, 1, 0);

        // Rotate based on the view
        GlStateManager.rotate(mc.player.rotationPitch, (mc.gameSettings.thirdPersonView == 2) ? -1 : 1, 0, 0);
        GlStateManager.scale(-scale, -scale, scale);

        GlStateManager.disableDepth();
        GlStateManager.translate(-(getStringWidth(text) / 2), 0, 0);

        renderText(text, 0, 0, textColour);

        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    public static void renderItemStack(ItemStack itemStack, float x, float y, boolean overlay) {
        RenderItem renderItem = mc.getRenderItem();

        GlStateManager.enableDepth();

        renderItem.zLevel = 200;
        renderItem.renderItemAndEffectIntoGUI(itemStack, (int) x, (int) y);

        if (overlay) {
            renderItem.renderItemOverlays(mc.fontRenderer, itemStack, (int) x, (int) y);
        }

        renderItem.zLevel = 0;

        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
    }

    public static void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, (y + height), 0.0D).tex((u * f), ((v + height) * f1)).endVertex();
        bufferbuilder.pos((x + width), (y + height), 0.0D).tex(((u + width) * f), ((v + height) * f1)).endVertex();
        bufferbuilder.pos((x + width), y, 0.0D).tex(((u + width) * f), (v * f1)).endVertex();
        bufferbuilder.pos(x, y, 0.0D).tex((u * f), (v * f1)).endVertex();
        tessellator.draw();
    }

    public static float getScreenWidth() {
        return Toolkit.getDefaultToolkit().getScreenSize().width / 2f;
    }

    public static float getScreenHeight() {
        return Toolkit.getDefaultToolkit().getScreenSize().height / 2f;
    }

    public static void renderText(String text, float x, float y, int colour) {
        if (ClientFont.INSTANCE.isEnabled()) {
            Paragon.INSTANCE.getFontManager().getFontRenderer().drawString(text, x, y - 3.5f, colour, false);
            return;
        }

        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, colour);
    }

    public static float getStringWidth(String text) {
        if (ClientFont.INSTANCE.isEnabled()) {
            return Paragon.INSTANCE.getFontManager().getFontRenderer().getStringWidth(text);
        }

        return Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
    }

}