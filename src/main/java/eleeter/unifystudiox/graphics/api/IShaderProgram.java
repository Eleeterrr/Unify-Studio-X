package eleeter.unifystudiox.graphics.api;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;

public interface IShaderProgram
{
    void bind();

    void unbind();

    int getId();

    void setUniform(String name, int value);

    void setUniform(String name, Matrix4f value, FloatBuffer buffer);

    void setUniform(String name, float value);

    void setUniform(String name, float x, float y);

    void setUniform(String name, float x, float y, float z);

    void setUniform(String name, float x, float y, float z, float w);

    void setUniform(String name, boolean value);

    void setUniformMatrix4f(String name, FloatBuffer buffer);

    void setUniformMatrix4fArray(String name, FloatBuffer buffer);

    void setUniformMatrix4fv(String name, float[] matrices);

    void cleanup();
}
