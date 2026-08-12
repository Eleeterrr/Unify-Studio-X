package eleeter.unifystudiox.graphics.gfx;

import eleeter.unifystudiox.graphics.Vao;
import eleeter.unifystudiox.graphics.VertexBuffer;
import eleeter.unifystudiox.graphics.buffer.GpuBufferUsage;
import eleeter.unifystudiox.graphics.layout.BufferLayout;

public class MeshBuilder
{
    private float[] vertices;
    private BufferLayout layout;
    private int[] indices;
    private boolean isDynamic;

    private MeshBuilder()
    {
        this.isDynamic = false;
    }

    public static MeshBuilder create()
    {
        return new MeshBuilder();
    }

    public MeshBuilder vertices(float[] vertices, BufferLayout layout)
    {
        this.vertices = vertices;
        this.layout = layout;
        return this;
    }

    public MeshBuilder indices(int[] indices)
    {
        this.indices = indices;
        return this;
    }

    public MeshBuilder dynamic()
    {
        this.isDynamic = true;
        return this;
    }

    public MeshHandle build()
    {
        if (this.vertices == null || this.layout == null)
        {
            throw new IllegalStateException("Vertices and layout must be provided.");
        }

        GpuBufferUsage usage = this.isDynamic ? GpuBufferUsage.DYNAMIC : GpuBufferUsage.STATIC;

        VertexBuffer vertexBuffer = new VertexBuffer(this.vertices, usage);
        VertexBuffer indexBuffer = null;

        Vao.Builder vaoBuilder = Vao.builder().bindVertexBuffer(vertexBuffer, this.layout);

        boolean isIndexed = false;
        int indexCount = 0;

        if (this.indices != null && this.indices.length > 0)
        {
            indexBuffer = new VertexBuffer(this.indices, usage);
            vaoBuilder.elementBuffer(indexBuffer);
            isIndexed = true;
            indexCount = this.indices.length;
        }

        Vao vao = vaoBuilder.build();
        
        int strideInBytes = this.layout.getStride();
        int vertexCount = (this.vertices.length * Float.BYTES) / strideInBytes;

        return new MeshHandle(vao, vertexBuffer, indexBuffer, vertexCount, indexCount, isIndexed);
    }
}
