package com.paragon.client.shader.shaders;

import com.paragon.client.shader.Shader;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL40.glUniform1d;

public class FluidShader extends Shader {

    public FluidShader() {
        super("/assets/paragon/glsl/shaders/fluid.frag");
    }

    @Override
    public void setupUniforms() {
        setupUniform("texture");
        setupUniform("texelSize");
        setupUniform("time");
    }

    @Override
    public void updateUniforms() {
        glUniform1i(getUniform("texture"), 0);
        glUniform2f(getUniform("texelSize"), 1F / mc.displayWidth, 1F / mc.displayHeight);
        glUniform1f(getUniform("time"), (float) getTime());
    }
}
