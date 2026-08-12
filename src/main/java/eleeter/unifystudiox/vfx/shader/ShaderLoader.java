package eleeter.unifystudiox.vfx.shader;

import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Compiles and links GLSL shader programs from classpath resources.
 * Shader compilation errors are logged and thrown — never silently ignored.
 */
public final class ShaderLoader
{

    private ShaderLoader()
    {
        /* Utility class — no instances. */
    }

    /**
     * Loads, compiles, and links a vertex + fragment shader pair from classpath resources.
     *
     * @param vertPath classpath path to the .vert file (e.g. "/shaders/vfx/sprite.vert")
     * @param fragPath classpath path to the .frag file
     * @return the linked OpenGL program ID
     */
    public static int load(String vertPath, String fragPath)
    {
        int vertId = compile(vertPath, GL_VERTEX_SHADER);
        int fragId = compile(fragPath, GL_FRAGMENT_SHADER);

        int program = glCreateProgram();
        if (program == 0)
        {
            throw new RuntimeException("[ShaderLoader] Could not create program for: " + vertPath);
        }

        glAttachShader(program, vertId);
        glAttachShader(program, fragId);
        glLinkProgram(program);

        if (glGetProgrami(program, GL_LINK_STATUS) == 0)
        {
            String log = glGetProgramInfoLog(program);
            throw new RuntimeException("[ShaderLoader] Link error (" + vertPath + "):\n" + log);
        }

        glDetachShader(program, vertId);
        glDeleteShader(vertId);
        glDetachShader(program, fragId);
        glDeleteShader(fragId);

        return program;
    }

    private static int compile(String classpathPath, int shaderType)
    {
        String source = loadText(classpathPath);
        int id = glCreateShader(shaderType);
        glShaderSource(id, source);
        glCompileShader(id);

        if (glGetShaderi(id, GL_COMPILE_STATUS) == 0)
        {
            String log = glGetShaderInfoLog(id);
            glDeleteShader(id);
            throw new RuntimeException("[ShaderLoader] Compile error (" + classpathPath + "):\n" + log);
        }

        return id;
    }

    private static String loadText(String path)
    {
        try (InputStream stream = ShaderLoader.class.getResourceAsStream(path))
        {
            if (stream == null)
            {
                throw new IOException("Classpath resource not found: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e)
        {
            throw new RuntimeException("[ShaderLoader] Failed to read shader: " + path, e);
        }
    }
}
