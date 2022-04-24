package com.paragon.client.shader.shaders;

import com.paragon.client.shader.Shader;

import java.awt.*;

import static org.lwjgl.opengl.GL20.*;

public class DiamondsShader extends Shader {

    private Color colour;
    private float size;
    private float spacing;

    public DiamondsShader() {
        super("/assets/paragon/glsl/shaders/diamonds.frag");
    }

    public void setColor(Color colour) {
        this.colour = colour;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public void setSpacing(float spacing) {
        this.spacing = spacing;
    }

    @Override
    public void setupUniforms() {
        setupUniform("texture");
        setupUniform("texelSize");
        setupUniform("resolution");
        setupUniform("time");

        setupUniform("size");
        setupUniform("spacing");
        setupUniform("colour");
    }

    @Override
    public void updateUniforms() {
        glUniform1i(getUniform("texture"), 0);
        glUniform2f(getUniform("texelSize"), 1F / mc.displayWidth, 1F / mc.displayHeight);
        glUniform2f(getUniform("resolution"), mc.displayWidth, mc.displayHeight);
        glUniform1f(getUniform("time"), (float) getTime());

        glUniform4f(getUniform("colour"), colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue() / 255f, colour.getAlpha() / 255f);
        glUniform1f(getUniform("size"), size);
        glUniform1f(getUniform("spacing"), spacing);
    }
}
