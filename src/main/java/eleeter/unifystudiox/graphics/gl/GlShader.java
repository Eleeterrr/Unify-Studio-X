package eleeter.unifystudiox.graphics.gl;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL20C;

public class GlShader
{
    private GlShader()
    {
    }

    public static int createShader(int type)
    {
        return GL20C.glCreateShader(type);
    }

    public static void shaderSource(int shader, CharSequence string)
    {
        GL20C.glShaderSource(shader, string);
    }

    public static void compileShader(int shader)
    {
        GL20C.glCompileShader(shader);
    }

    public static int getShaderi(int shader, int pname)
    {
        return GL20C.glGetShaderi(shader, pname);
    }

    public static String getShaderInfoLog(int shader)
    {
        return GL20C.glGetShaderInfoLog(shader);
    }

    public static void deleteShader(int shader)
    {
        GL20C.glDeleteShader(shader);
    }

    public static int createProgram()
    {
        return GL20C.glCreateProgram();
    }

    public static void attachShader(int program, int shader)
    {
        GL20C.glAttachShader(program, shader);
    }

    public static void detachShader(int program, int shader)
    {
        GL20C.glDetachShader(program, shader);
    }

    public static void linkProgram(int program)
    {
        GL20C.glLinkProgram(program);
    }

    public static void validateProgram(int program)
    {
        GL20C.glValidateProgram(program);
    }

    public static int getProgrami(int program, int pname)
    {
        return GL20C.glGetProgrami(program, pname);
    }

    public static String getProgramInfoLog(int program)
    {
        return GL20C.glGetProgramInfoLog(program);
    }

    public static void deleteProgram(int program)
    {
        GL20C.glDeleteProgram(program);
    }

    public static void useProgram(int program)
    {
        GL20C.glUseProgram(program);
    }

    public static int getUniformLocation(int program, CharSequence name)
    {
        return GL20C.glGetUniformLocation(program, name);
    }

    public static void uniform1i(int location, int v0)
    {
        GL20C.glUniform1i(location, v0);
    }

    public static void uniform1f(int location, float v0)
    {
        GL20C.glUniform1f(location, v0);
    }

    public static void uniform3f(int location, float v0, float v1, float v2)
    {
        GL20C.glUniform3f(location, v0, v1, v2);
    }

    public static void uniform4f(int location, float v0, float v1, float v2, float v3)
    {
        GL20C.glUniform4f(location, v0, v1, v2, v3);
    }

    public static void uniformMatrix4fv(int location, boolean transpose, FloatBuffer value)
    {
        GL20C.glUniformMatrix4fv(location, transpose, value);
    }

    public static void uniformMatrix4fv(int location, boolean transpose, float[] value)
    {
        GL20C.glUniformMatrix4fv(location, transpose, value);
    }
}
