package eleeter.unifystudiox.graphics.gfx;

import eleeter.unifystudiox.graphics.Vao;
import eleeter.unifystudiox.graphics.VertexBuffer;

public class MeshHandle
{
    private final Vao vao;
    private final VertexBuffer vertexBuffer;
    private final VertexBuffer indexBuffer;
    private final int vertexCount;
    private final int indexCount;
    private final boolean isIndexed;

    public MeshHandle(Vao vao, VertexBuffer vertexBuffer, VertexBuffer indexBuffer, int vertexCount, int indexCount, boolean isIndexed)
    {
        this.vao = vao;
        this.vertexBuffer = vertexBuffer;
        this.indexBuffer = indexBuffer;
        this.vertexCount = vertexCount;
        this.indexCount = indexCount;
        this.isIndexed = isIndexed;
    }

    public int getVertexCount()
    {
        return this.vertexCount;
    }

    public int getIndexCount()
    {
        return this.indexCount;
    }

    public boolean isIndexed()
    {
        return this.isIndexed;
    }

    public Vao vao()
    {
        return this.vao;
    }

    public void destroy()
    {
        this.vao.destroy();
        
        if (this.vertexBuffer != null)
        {
            this.vertexBuffer.destroy();
        }
        
        if (this.indexBuffer != null)
        {
            this.indexBuffer.destroy();
        }
    }
}
