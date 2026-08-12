package eleeter.unifystudiox.graphics.gl;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VALIDATE_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glValidateProgram;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;

import eleeter.unifystudiox.graphics.api.IShaderProgram;
import eleeter.unifystudiox.util.log.AniLogger;

public class GLShaderProgram implements IShaderProgram
{

    private final int programId;
    private final Set<String> reportedMissing = new HashSet<>();
    private int vertId;


    private GLShaderProgram(int programId)
    {
        this.programId = programId;
    }


    public void bind()
    {
        glUseProgram(this.programId);
    }

    public void unbind()
    {
        glUseProgram(0);
    }

    public int getId()
    {
        return this.programId;
    }


    public void setUniform(String name, int value)
    {
        glUniform1i(location(name), value);
    }


    /**
     * Upload a 4×4 matrix.
     */
    public void setUniform(String name, Matrix4f value, FloatBuffer buffer)
    {
        value.get(buffer); /* Copies matrix data into the FloatBuffer */
        glUniformMatrix4fv(location(name), false, buffer);
    }

    public void setUniform(String name, float value)
    {
        glUniform1f(location(name), value);
    }

    public void setUniform(String name, float x, float y)
    {
        GL20.glUniform2f(location(name), x, y);
    }

    public void setUniform(String name, float x, float y, float z)
    {
        glUniform3f(location(name), x, y, z);
    }

    public void setUniform(String name, float x, float y, float z, float w)
    {
        glUniform4f(location(name), x, y, z, w);
    }

    public void setUniform(String name, boolean value)
    {
        glUniform1i(location(name), value ? 1 : 0);
    }

    /**
     * Upload a 4×4 column-major matrix.
     */
    public void setUniformMatrix4f(String name, FloatBuffer buffer)
    {
        glUniformMatrix4fv(location(name), false, buffer);
    }

    /**
     * Upload an array of 4×4 matrices.
     */
    public void setUniformMatrix4fArray(String name, FloatBuffer buffer)
    {
        glUniformMatrix4fv(location(name), false, buffer);
    }

    /**
     * Upload an array of 4×4 matrices from a raw float array.
     */
    public void setUniformMatrix4fv(String name, float[] matrices)
    {
        glUniformMatrix4fv(location(name), false, matrices);
    }


    public void cleanup()
    {
        unbind();
        if (this.programId != 0)
        {
            glDeleteProgram(this.programId);
        }
        this.reportedMissing.clear();
    }


    public static Builder builder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private String vertexPath;
        private String fragmentPath;
        private String geometryPath;   // optional

        public Builder vertex(String classpath)
        {
            this.vertexPath = classpath;
            return this;
        }

        public Builder fragment(String classpath)
        {
            this.fragmentPath = classpath;
            return this;
        }

        public Builder geometry(String classpath)
        {
            this.geometryPath = classpath;
            return this;
        }

        public GLShaderProgram build()
        {
            int programId = glCreateProgram();
            if (programId == 0)
            {
                throw new RuntimeException("Could not create shader program");
            }

            int vertId = compile(this.vertexPath, GL_VERTEX_SHADER);
            int fragId = compile(this.fragmentPath, GL_FRAGMENT_SHADER);
            int geomId = (geometryPath != null) ? compile(geometryPath, GL_GEOMETRY_SHADER) : 0;

            glAttachShader(programId, vertId);
            glAttachShader(programId, fragId);
            if (geomId != 0)
            {
                glAttachShader(programId, geomId);
            }

            glLinkProgram(programId);
            if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE)
            {
                throw new RuntimeException("Shader link error:\n" + glGetProgramInfoLog(programId));
            }

            glDetachShader(programId, vertId);
            glDeleteShader(vertId);
            glDetachShader(programId, fragId);
            glDeleteShader(fragId);
            if (geomId != 0)
            {
                glDetachShader(programId, geomId);
                glDeleteShader(geomId);
            }

            glValidateProgram(programId);
            if (glGetProgrami(programId, GL_VALIDATE_STATUS) == GL_FALSE)
            {
                System.err.println("Shader validation warning:\n" + glGetProgramInfoLog(programId));
            }

            return new GLShaderProgram(programId);
        }

        private static int compile(String classpathResource, int shaderType)
        {
            String src = loadResource(classpathResource);
            int id = glCreateShader(shaderType);
            glShaderSource(id, src);
            glCompileShader(id);
            if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE)
            {
                throw new RuntimeException("Shader compile error [" + classpathResource + "]:\n" + glGetShaderInfoLog(id));
            }
            return id;
        }

        private static String loadResource(String path)
        {
            try (InputStream stream = GLShaderProgram.class.getResourceAsStream(path))
            {
                if (stream == null)
                {
                    throw new IOException("Resource not found: " + path);
                }
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e)
            {
                throw new RuntimeException("Failed to load shader: " + path, e);
            }
        }
    }


    private int location(String name)
    {
        int loc = glGetUniformLocation(this.programId, name);
        if (loc == -1)
        {
            if (this.reportedMissing.add(name))
            {
                AniLogger.warn("ShaderProgram", "Uniform '" + name + "' not found (program " +
                        this.programId + ")");
            }
        }
        return loc;
    }


}
