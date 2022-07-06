package com.paragon.client.shader;

import com.paragon.api.util.Wrapper;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.ARBShaderObjects;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

/**
 * @author Wolfsurge, Cosmos
 */
public class Shader implements Wrapper {

    private int program;
    private Map<String, Integer> uniforms;

    private double time;

    public Shader(String path) {
        int vertex = 0;
        int fragment = 0;

        try {
            InputStream vertStream = getClass().getResourceAsStream("/assets/paragon/glsl/vertex.vert");

            if (vertStream != null) {
                vertex = createShader(IOUtils.toString(vertStream, Charset.defaultCharset()), GL_VERTEX_SHADER);
                IOUtils.closeQuietly(vertStream);

                System.out.println(path + " Vertex shader loaded");
            }

            InputStream fragStream = getClass().getResourceAsStream(path);

            if (fragStream != null) {
                fragment = createShader(IOUtils.toString(fragStream, Charset.defaultCharset()), GL_FRAGMENT_SHADER);
                IOUtils.closeQuietly(fragStream);

                System.out.println(path + " Fragment shader loaded");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (vertex != 0 && fragment != 0) {
            program = ARBShaderObjects.glCreateProgramObjectARB();

            if (program != 0) {
                ARBShaderObjects.glAttachObjectARB(program, vertex);
                ARBShaderObjects.glAttachObjectARB(program, fragment);
                ARBShaderObjects.glLinkProgramARB(program);
                ARBShaderObjects.glValidateProgramARB(program);

                System.out.println(path + " Shader program loaded");
            }
        }
    }

    public void startShader() {
        glUseProgram(program);

        if (uniforms == null) {
            uniforms = new HashMap<>();
            setupUniforms();
        }

        updateUniforms();
    }

    public void setupUniforms() {
    }

    public void updateUniforms() {
    }

    private int createShader(String source, int type) {
        int shader = 0;

        try {
            shader = ARBShaderObjects.glCreateShaderObjectARB(type);

            if (shader != 0) {
                ARBShaderObjects.glShaderSourceARB(shader, source);
                ARBShaderObjects.glCompileShaderARB(shader);

                if (ARBShaderObjects.glGetObjectParameteriARB(shader, 35713) == 0) {
                    throw new RuntimeException("Error creating shader: " + ARBShaderObjects.glGetInfoLogARB(shader, ARBShaderObjects.glGetObjectParameteriARB(shader, 35716)));
                }

                return shader;
            } else {
                return 0;
            }
        } catch (Exception e) {
            ARBShaderObjects.glDeleteObjectARB(shader);
            e.printStackTrace();
            throw e;
        }
    }

    public void setupUniform(String name) {
        uniforms.put(name, glGetUniformLocation(program, name));
    }

    public int getUniform(String name) {
        return uniforms.get(name);
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

}
