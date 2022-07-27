package com.paragon.api.util.render;

import com.paragon.api.util.Wrapper;
import com.paragon.asm.mixins.accessor.IShaderGroup;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Surge
 * @since 27/07/22
 */
public class BlurUtil implements Wrapper {

    private static int lastScale = -1;
    private static int lastScaleWidth = -1;
    private static int lastScaleHeight = -1;

    private static Framebuffer framebuffer = null;
    private static ShaderGroup blurShader = null;

    private static void checkScale(int scaleFactor, int widthFactor, int heightFactor) {
        if (lastScale != scaleFactor || lastScaleWidth != widthFactor || lastScaleHeight != heightFactor || framebuffer == null || blurShader == null) {
            try {
                blurShader = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), new ResourceLocation("shaders/post/blur.json"));
                blurShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
                framebuffer = ((IShaderGroup) blurShader).getMainFramebuffer();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        lastScale = scaleFactor;
        lastScaleWidth = widthFactor;
        lastScaleHeight = heightFactor;
    }

    private static void updateUniforms(int intensity) {
        ((IShaderGroup) blurShader).getListShaders().get(0).getShaderManager().getShaderUniform("Radius").set(intensity);
        ((IShaderGroup) blurShader).getListShaders().get(1).getShaderManager().getShaderUniform("Radius").set(intensity);
        ((IShaderGroup) blurShader).getListShaders().get(0).getShaderManager().getShaderUniform("BlurDir").set(2, 1);
        ((IShaderGroup) blurShader).getListShaders().get(1).getShaderManager().getShaderUniform("BlurDir").set(1, 1);
    }

    public static void blur(int x, int y, int width, int height, int intensity) {
        ScaledResolution resolution = new ScaledResolution(mc);

        int currentScale = resolution.getScaleFactor();

        checkScale(currentScale, resolution.getScaledWidth(), resolution.getScaledHeight());

        if (OpenGlHelper.isFramebufferEnabled()) {
            RenderUtil.pushScissor(x, y + 1, width, height - 1);

            updateUniforms(intensity);

            framebuffer.bindFramebuffer(true);
            blurShader.render(mc.getRenderPartialTicks());
            mc.getFramebuffer().bindFramebuffer(true);

            RenderUtil.popScissor();

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ZERO, GL_ONE);

            framebuffer.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false);

            GlStateManager.disableBlend();

            glScalef(currentScale, currentScale, 0);
        }
    }

}