package eleeter.unifystudiox.renderer.postprocess;

import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11C.glDrawArrays;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

public class FullscreenQuad
{
    private final int vao;

    public FullscreenQuad()
    {
        this.vao = glGenVertexArrays();
    }

    public void draw()
    {
        glBindVertexArray(this.vao);
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glBindVertexArray(0);
    }

    public void dispose()
    {
        glDeleteVertexArrays(this.vao);
    }
}
