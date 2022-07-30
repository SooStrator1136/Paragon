package com.paragon.client.shader.shaders;

import com.paragon.client.shader.Shader;

import java.awt.*;

import static org.lwjgl.opengl.GL20.*;

public class DiagonalShader extends Shader {

    private Color lineColour;
    private float lineWidth;
    private float spacing;

    public DiagonalShader() {
        super("/assets/paragon/glsl/shaders/diagonal.frag");
    }

    public void setColour(Color colour) {
        lineColour = colour;
    }

    public void setWidth(float width) {
        lineWidth = width;
    }

    public void setSpacing(float spacing) {
        this.spacing = spacing;
    }

    @Override
    public void setupUniforms() {
        setupUniform("texture");
        setupUniform("time");

        setupUniform("colour");
        setupUniform("size");
        setupUniform("spacing");
    }

    @Override
    public void updateUniforms() {
        glUniform1i(getUniform("texture"), 0);
        glUniform1f(getUniform("time"), (float) getTime());

        glUniform4f(getUniform("colour"), lineColour.getRed() / 255f, lineColour.getGreen() / 255f, lineColour.getBlue() / 255f, lineColour.getAlpha() / 255f);
        glUniform1f(getUniform("size"), lineWidth);
        glUniform1f(getUniform("spacing"), spacing);
    }

}
