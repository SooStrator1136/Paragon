package com.paragon.client.shader.shaders;

import com.paragon.client.shader.Shader;

import java.awt.*;

import static org.lwjgl.opengl.GL20.*;

public class SmoothShader extends Shader {

    private int colourType = 1;
    private float width = 1F;
    private int outline = 0;

    public SmoothShader() {
        super("/assets/paragon/glsl/shaders/smooth.frag");
    }

    public void setColor(int colourType) {
        this.colourType = colourType;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setOutline(int outline) {
        this.outline = outline;
    }

    @Override
    public void setupUniforms() {
        setupUniform("texture");
        setupUniform("texelSize");
        setupUniform("playerRotation");
        setupUniform("colour");
        setupUniform("radius");
        setupUniform("outline");
    }

    @Override
    public void updateUniforms() {
        glUniform1i(getUniform("texture"), 0);
        glUniform2f(getUniform("texelSize"), 1F / mc.displayWidth, 1F / mc.displayHeight);
        glUniform2f(getUniform("playerRotation"), mc.player.rotationYaw, mc.player.rotationPitch);
        glUniform1i(getUniform("colour"), colourType);
        glUniform1f(getUniform("radius"), width);
        glUniform1i(getUniform("outline"), outline);
    }
}
