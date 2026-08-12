package eleeter.unifystudiox.renderer.geometry;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import eleeter.unifystudiox.graphics.Vao;
import eleeter.unifystudiox.graphics.VertexBuffer;
import eleeter.unifystudiox.graphics.api.IShaderProgram;
import eleeter.unifystudiox.graphics.buffer.GpuBufferUsage;
import eleeter.unifystudiox.graphics.layout.AttributeType;
import eleeter.unifystudiox.graphics.layout.BufferLayout;
import eleeter.unifystudiox.renderer.core.RenderBucket;
import eleeter.unifystudiox.renderer.core.RenderCommand;
import eleeter.unifystudiox.renderer.core.RenderContext;
import eleeter.unifystudiox.renderer.core.RenderPass;
import eleeter.unifystudiox.renderer.entities.EntityRenderer;
import eleeter.unifystudiox.scene.entity.gizmo.MeshEntity;

public class MeshRenderer implements EntityRenderer<MeshEntity>
{
    private static final BufferLayout POS_LAYOUT = BufferLayout.builder()
            .add(0, 3, AttributeType.FLOAT)
            .build();

    private static final BufferLayout NRM_LAYOUT = BufferLayout.builder()
            .add(1, 3, AttributeType.FLOAT)
            .build();

    private record GpuMesh(Vao vao, VertexBuffer posVbo, VertexBuffer normalVbo, VertexBuffer indexVbo, int indexCount)
    {
    }

    private final Map<String, GpuMesh> meshCache = new HashMap<>();

    @Override
    public Class<MeshEntity> getSupportedType()
    {
        return MeshEntity.class;
    }

    @Override
    public void submitCommands(MeshEntity entity, RenderContext ctx)
    {
        if (ctx.pass() == RenderPass.SHADOW_DEPTH)
        {
            return;
        }

        GpuMesh gpu = this.meshCache.computeIfAbsent(entity.getId(), id -> upload(entity));

        RenderCommand cmd = ctx.bucketManager().allocateCommand();
        cmd.sortKey = ((long) ctx.shader().hashCode() << 32) | (gpu.vao().getHandle());
        cmd.shader = ctx.shader();
        cmd.texture = null;
        cmd.vao = gpu.vao();
        cmd.count = gpu.indexCount();
        cmd.indexed = true;
        cmd.renderer = this;
        cmd.entity = entity;
        cmd.customId = 0;

        ctx.bucketManager().submit(RenderBucket.SOLID_3D, cmd);
    }

    @Override
    public void setupUniforms(IShaderProgram shader, MeshEntity entity, int customId, RenderContext context)
    {
        FloatBuffer buf = context.matrixBuffer();
        entity.getModelMatrix().get(buf);
        shader.setUniformMatrix4f("uModel", buf);
    }

    @Override
    public void cleanup()
    {
        for (GpuMesh gpu : this.meshCache.values())
        {
            gpu.posVbo().destroy();
            gpu.normalVbo().destroy();
            gpu.indexVbo().destroy();
            gpu.vao().destroy();
        }
        this.meshCache.clear();
    }

    private GpuMesh upload(MeshEntity entity)
    {
        VertexBuffer posVbo = new VertexBuffer(CUBE_POSITIONS, GpuBufferUsage.STATIC);
        VertexBuffer nrmVbo = new VertexBuffer(CUBE_NORMALS, GpuBufferUsage.STATIC);
        VertexBuffer idxVbo = new VertexBuffer(CUBE_INDICES, GpuBufferUsage.STATIC);

        Vao vao = Vao.builder()
                .bindVertexBuffer(posVbo, POS_LAYOUT)
                .bindVertexBuffer(nrmVbo, NRM_LAYOUT)
                .elementBuffer(idxVbo)
                .build();

        return new GpuMesh(vao, posVbo, nrmVbo, idxVbo, CUBE_INDICES.length);
    }

    private static final float[] CUBE_POSITIONS =
            {
                    // +Z  front
                    -0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F,
                    // -Z  back
                    -0.5F, -0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, -0.5F, -0.5F,
                    // +Y  top
                    -0.5F, 0.5F, -0.5F, -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, -0.5F,
                    // -Y  bottom
                    -0.5F, -0.5F, -0.5F, 0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, -0.5F, -0.5F, 0.5F,
                    // +X  right
                    0.5F, -0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, -0.5F, 0.5F,
                    // -X  left
                    -0.5F, -0.5F, -0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, -0.5F,
            };

    private static final float[] CUBE_NORMALS =
            {
                    // +Z
                    0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F,
                    // -Z
                    0.0F, 0.0F, -1.0F, 0.0F, 0.0F, -1.0F, 0.0F, 0.0F, -1.0F, 0.0F, 0.0F, -1.0F,
                    // +Y
                    0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F,
                    // -Y
                    0.0F, -1.0F, 0.0F, 0.0F, -1.0F, 0.0F, 0.0F, -1.0F, 0.0F, 0.0F, -1.0F, 0.0F,
                    // +X
                    1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F,
                    // -X
                    -1.0F, 0.0F, 0.0F, -1.0F, 0.0F, 0.0F, -1.0F, 0.0F, 0.0F, -1.0F, 0.0F, 0.0F,
            };

    private static final int[] CUBE_INDICES =
            {
                    0, 1, 2, 2, 3, 0,   // front
                    4, 5, 6, 6, 7, 4,   // back
                    8, 9, 10, 10, 11, 8,   // top
                    12, 13, 14, 14, 15, 12,   // bottom
                    16, 17, 18, 18, 19, 16,   // right
                    20, 21, 22, 22, 23, 20,   // left
            };
}
