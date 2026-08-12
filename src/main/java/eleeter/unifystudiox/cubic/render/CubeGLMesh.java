package eleeter.unifystudiox.cubic.render;

import static org.lwjgl.opengl.ARBDirectStateAccess.glCreateBuffers;
import static org.lwjgl.opengl.ARBDirectStateAccess.glCreateVertexArrays;
import static org.lwjgl.opengl.ARBDirectStateAccess.glEnableVertexArrayAttrib;
import static org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferData;
import static org.lwjgl.opengl.ARBDirectStateAccess.glVertexArrayAttribBinding;
import static org.lwjgl.opengl.ARBDirectStateAccess.glVertexArrayAttribFormat;
import static org.lwjgl.opengl.ARBDirectStateAccess.glVertexArrayElementBuffer;
import static org.lwjgl.opengl.ARBDirectStateAccess.glVertexArrayVertexBuffer;
import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15C.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15C.glDeleteBuffers;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.system.MemoryUtil;

public class CubeGLMesh
{
    private static final int STRIDE = 16 * Float.BYTES;

    private final int vao;
    private final int vbo;
    private final int ebo;
    private final int indexCount;

    public CubeGLMesh(float[] vertexData, int[] indices)
    {
        this.indexCount = indices.length;

        this.vao = glCreateVertexArrays();
        this.vbo = glCreateBuffers();
        this.ebo = glCreateBuffers();

        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertexData.length);
        vertexBuffer.put(vertexData).flip();

        IntBuffer indexBuffer = MemoryUtil.memAllocInt(indices.length);
        indexBuffer.put(indices).flip();

        try
        {
            glNamedBufferData(this.vbo, vertexBuffer, GL_STATIC_DRAW);
            glNamedBufferData(this.ebo, indexBuffer, GL_STATIC_DRAW);
        } finally
        {
            MemoryUtil.memFree(vertexBuffer);
            MemoryUtil.memFree(indexBuffer);
        }

        glVertexArrayVertexBuffer(this.vao, 0, this.vbo, 0, STRIDE);
        glVertexArrayElementBuffer(this.vao, this.ebo);

        glEnableVertexArrayAttrib(this.vao, 0);
        glVertexArrayAttribFormat(this.vao, 0, 3, GL_FLOAT, false, 0);
        glVertexArrayAttribBinding(this.vao, 0, 0);

        glEnableVertexArrayAttrib(this.vao, 1);
        glVertexArrayAttribFormat(this.vao, 1, 2, GL_FLOAT, false, 3 * Float.BYTES);
        glVertexArrayAttribBinding(this.vao, 1, 0);

        glEnableVertexArrayAttrib(this.vao, 2);
        glVertexArrayAttribFormat(this.vao, 2, 3, GL_FLOAT, false, 5 * Float.BYTES);
        glVertexArrayAttribBinding(this.vao, 2, 0);

        glEnableVertexArrayAttrib(this.vao, 3);
        glVertexArrayAttribFormat(this.vao, 3, 4, GL_FLOAT, false, 8 * Float.BYTES);
        glVertexArrayAttribBinding(this.vao, 3, 0);

        glEnableVertexArrayAttrib(this.vao, 4);
        glVertexArrayAttribFormat(this.vao, 4, 4, GL_FLOAT, false, 12 * Float.BYTES);
        glVertexArrayAttribBinding(this.vao, 4, 0);
    }

    public void bind()
    {
        glBindVertexArray(this.vao);
    }

    public int getIndexCount()
    {
        return this.indexCount;
    }

    public int getVaoId()
    {
        return this.vao;
    }

    public void cleanup()
    {
        glDeleteVertexArrays(this.vao);
        glDeleteBuffers(this.vbo);
        glDeleteBuffers(this.ebo);
    }
}
