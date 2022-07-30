package com.paragon.client.shader.shaders;

import com.paragon.client.shader.Shader;

import java.awt.*;

import static org.lwjgl.opengl.GL20.*;

public class SmokeShader extends Shader {

    private Color colour = new Color(0, 0, 0);

    public SmokeShader() {
        super("/assets/paragon/glsl/shaders/smoke.frag");
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }

    @Override
    public void setupUniforms() {
        setupUniform("texture");
        setupUniform("resolution");
        setupUniform("time");
        setupUniform("col");
    }

    @Override
    public void updateUniforms() {
        glUniform1i(getUniform("texture"), 0);
        glUniform2f(getUniform("resolution"), 1F / mc.displayWidth, 1F / mc.displayHeight);
        glUniform1f(getUniform("time"), (float) getTime());
        glUniform4f(getUniform("col"), colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue() / 255f, colour.getAlpha() / 255f);
    }

}
