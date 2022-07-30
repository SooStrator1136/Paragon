package com.paragon.client.shader.shaders;

import com.paragon.client.shader.Shader;

import java.awt.*;

import static org.lwjgl.opengl.GL20.*;

public class LiquidShader extends Shader {

    private Color colour = new Color(0, 0, 0);

    public LiquidShader() {
        super("/assets/paragon/glsl/shaders/liquid.frag");
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }

    @Override
    public void setupUniforms() {
        setupUniform("texture");
        setupUniform("resolution");
        setupUniform("time");
        setupUniform("colour");
    }

    @Override
    public void updateUniforms() {
        glUniform1i(getUniform("texture"), 0);
        glUniform2f(getUniform("resolution"), 1F / mc.displayWidth, 1F / mc.displayHeight);
        glUniform1f(getUniform("time"), (float) getTime());
        glUniform4f(getUniform("colour"), colour.getRed() / 255F, colour.getGreen() / 255F, colour.getBlue() / 255F, colour.getAlpha() / 255F);
    }

}
