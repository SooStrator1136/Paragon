package com.paragon.client.shader.shaders;

import com.paragon.client.shader.Shader;

import java.awt.*;

import static org.lwjgl.opengl.GL20.*;

public class OutlineShader extends Shader {

    private Color colour = new Color(0, 0, 0);
    private float width = 1F;
    private int fill = 0;

    public OutlineShader() {
        super("/assets/paragon/glsl/shaders/outline.frag");
    }

    public void setColor(Color colour) {
        this.colour = colour;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setFill(int fill) {
        this.fill = fill;
    }

    @Override
    public void setupUniforms() {
        setupUniform("texture");
        setupUniform("texelSize");
        setupUniform("colour");
        setupUniform("divider");
        setupUniform("radius");
        setupUniform("fill");
        setupUniform("maxSample");
    }

    @Override
    public void updateUniforms() {
        glUniform1i(getUniform("texture"), 0);
        glUniform2f(getUniform("texelSize"), 1F / mc.displayWidth, 1F / mc.displayHeight);
        glUniform4f(getUniform("colour"), colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue() / 255f, colour.getAlpha() / 255f);
        glUniform1f(getUniform("radius"), width);
        glUniform1i(getUniform("fill"), fill);
    }
}
