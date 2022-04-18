package com.paragon.client.shader.shaders;

import com.paragon.client.shader.Shader;

import java.awt.*;

import static org.lwjgl.opengl.GL20.*;

public class DiagonalShader extends Shader {

    private Color lineColour;
    private float lineWidth;

    public DiagonalShader() {
        super("/assets/paragon/glsl/shaders/diagonal.frag");
    }

    public void setColour(Color colour) {
        lineColour = colour;
    }

    public void setLineWidth(float width) {
        lineWidth = width;
    }

    @Override
    public void setupUniforms() {
        setupUniform("texture");
        setupUniform("texelSize");
        setupUniform("resolution");
        setupUniform("time");

        setupUniform("colour");
        setupUniform("size");
    }

    @Override
    public void updateUniforms() {
        glUniform1i(getUniform("texture"), 0);
        glUniform2f(getUniform("texelSize"), 1F / mc.displayWidth, 1F / mc.displayHeight);
        glUniform2f(getUniform("resolution"), mc.displayWidth, mc.displayHeight);
        glUniform1f(getUniform("time"), (float) getTime());

        glUniform4f(getUniform("colour"), lineColour.getRed() / 255f, lineColour.getGreen() / 255f, lineColour.getBlue() / 255f, lineColour.getAlpha() / 255f);
        glUniform1f(getUniform("size"), lineWidth);
    }
}
