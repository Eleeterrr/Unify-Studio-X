package eleeter.unifystudiox.amb;

import eleeter.unifystudiox.graphics.Vao;
import eleeter.unifystudiox.graphics.VertexBuffer;
import eleeter.unifystudiox.graphics.buffer.GpuBufferUsage;
import eleeter.unifystudiox.graphics.layout.AttributeType;
import eleeter.unifystudiox.graphics.layout.BufferLayout;

public class AmbGLMesh
{
    public static final BufferLayout LAYOUT = BufferLayout.builder()
            .add(0, 3, AttributeType.FLOAT)
            .add(2, 3, AttributeType.FLOAT)
            .add(1, 2, AttributeType.FLOAT)
            .add(4, 4, AttributeType.FLOAT)
            .add(3, 4, AttributeType.FLOAT)
            .build();

    private final Vao vao;
    private final VertexBuffer vbo;
    private final VertexBuffer ebo;
    private final int indexCount;

    public AmbGLMesh(AmbMesh mesh)
    {
        this.indexCount = mesh.indices.length;

        this.vbo = new VertexBuffer(mesh.vertexData, GpuBufferUsage.STATIC);
        this.ebo = new VertexBuffer(mesh.indices, GpuBufferUsage.STATIC);

        this.vao = Vao.builder()
                .bindVertexBuffer(this.vbo, LAYOUT)
                .elementBuffer(this.ebo)
                .build();
    }

    public void bind()
    {
        this.vao.bind();
    }

    public int getIndexCount()
    {
        return this.indexCount;
    }
    
    public Vao getVao() 
    {
        return this.vao;
    }

    public void cleanup()
    {
        this.vao.destroy();
        this.vbo.destroy();
        this.ebo.destroy();
    }
}
