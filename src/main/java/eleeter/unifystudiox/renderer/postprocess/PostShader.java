package eleeter.unifystudiox.renderer.postprocess;

import static org.lwjgl.opengl.GL11C.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
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
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUseProgram;

import eleeter.unifystudiox.util.log.AniLogger;

class PostShader
{
    private final int programId;

    PostShader(String effectName, String vertSrc, String fragSrc)
    {
        int vert = compile(effectName, GL_VERTEX_SHADER, vertSrc);
        int frag = compile(effectName, GL_FRAGMENT_SHADER, fragSrc);

        this.programId = glCreateProgram();
        glAttachShader(this.programId, vert);
        glAttachShader(this.programId, frag);
        glLinkProgram(this.programId);

        glDetachShader(this.programId, vert);
        glDeleteShader(vert);
        glDetachShader(this.programId, frag);
        glDeleteShader(frag);

        if (glGetProgrami(this.programId, GL_LINK_STATUS) == GL_FALSE)
        {
            throw new IllegalStateException("[" + effectName + "] Shader link failed:\n" + glGetProgramInfoLog(this.programId));
        }
    }

    private static int compile(String effectName, int type, String src)
    {
        int id = glCreateShader(type);
        glShaderSource(id, src);
        glCompileShader(id);

        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE)
        {
            String log = glGetShaderInfoLog(id);
            glDeleteShader(id);
            AniLogger.error("PostShader", "[" + effectName + "] " + log);
            throw new IllegalStateException("[" + effectName + "] Shader compile failed.");
        }

        return id;
    }

    void bind()
    {
        glUseProgram(this.programId);
    }

    void unbind()
    {
        glUseProgram(0);
    }

    void setInt(String name, int val)
    {
        glUniform1i(loc(name), val);
    }

    void setFloat(String name, float val)
    {
        glUniform1f(loc(name), val);
    }

    void setVec2(String name, float x, float y)
    {
        glUniform2f(loc(name), x, y);
    }

    private int loc(String name)
    {
        return glGetUniformLocation(this.programId, name);
    }

    void dispose()
    {
        glUseProgram(0);
        glDeleteProgram(this.programId);
    }
}
