package eleeter.unifystudiox.renderer;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.graphics.Vao;
import eleeter.unifystudiox.graphics.VertexBuffer;
import eleeter.unifystudiox.graphics.api.IShaderProgram;
import eleeter.unifystudiox.graphics.api.PrimitiveType;
import eleeter.unifystudiox.graphics.buffer.GpuBufferUsage;
import eleeter.unifystudiox.graphics.gfx.PipelineState;
import eleeter.unifystudiox.graphics.layout.AttributeType;
import eleeter.unifystudiox.graphics.layout.BufferLayout;
import eleeter.unifystudiox.graphics.text.render.MeshData;
import eleeter.unifystudiox.renderer.core.RenderBucket;
import eleeter.unifystudiox.renderer.core.RenderCommand;
import eleeter.unifystudiox.renderer.core.RenderContext;
import eleeter.unifystudiox.renderer.core.RenderPass;
import eleeter.unifystudiox.renderer.entities.EntityRenderer;
import eleeter.unifystudiox.scene.entity.LabelEntity;


public class LabelRenderer implements EntityRenderer<LabelEntity>
{
    private static final BufferLayout LABEL_LAYOUT = BufferLayout.builder()
            .add(0, 3, AttributeType.FLOAT)
            .add(1, 2, AttributeType.FLOAT)
            .build();

    private IShaderProgram labelShader;

    private final Map<LabelEntity, LabelGpuMesh> gpuMeshes = new HashMap<>();

    private final Matrix4f billboardModel = new Matrix4f();


    @Override
    public Class<LabelEntity> getSupportedType()
    {
        return LabelEntity.class;
    }


    @Override
    public void submitCommands(LabelEntity entity, RenderContext context)
    {
        if (context.pass() == RenderPass.SHADOW_DEPTH)
            return;

        if (entity.getFont() == null)
            return;
        String fontKey = resolveFontKey(entity);
        if (fontKey == null)
            return;

        TextureGL atlas = FontManager.getAtlas(fontKey);
        if (atlas == null)
            return;

        LabelGpuMesh gpuMesh = getOrBuildGpuMesh(entity);
        if (gpuMesh == null)
            return;

        ensureShader(context);

        RenderCommand cmd = context.bucketManager().allocateCommand();
        cmd.sortKey = ((long) this.labelShader.hashCode() << 32) | atlas.hashCode();
        cmd.shader = this.labelShader;
        cmd.texture = atlas;
        cmd.vao = gpuMesh.vao;
        cmd.count = gpuMesh.indexCount;
        cmd.indexed = true;
        cmd.primitiveType = PrimitiveType.TRIANGLES;
        cmd.state = PipelineState.TRANSPARENT;
        cmd.renderer = this;
        cmd.entity = entity;
        cmd.customId = 0;
        
        context.bucketManager().submit(RenderBucket.WORLD_2D, cmd);
    }

    @Override
    public void setupUniforms(IShaderProgram shader, LabelEntity entity, int customId, RenderContext context)
    {

        shader.setUniformMatrix4f("uView",       context.viewMatrix().get(context.matrixBuffer()));
        shader.setUniformMatrix4f("uProjection", context.projectionMatrix().get(context.matrixBuffer()));

        Matrix4f model = buildModelMatrix(entity, context.viewMatrix());
        shader.setUniformMatrix4f("uModel", model.get(context.matrixBuffer()));

        shader.setUniform("uMsdfAtlas",  0);
        shader.setUniform("uPxRange",    entity.getFont().getDistanceRange());
        shader.setUniform("uTextColor",
                entity.getColor().x, entity.getColor().y,
                entity.getColor().z, entity.getColor().w);
    }




    @Override
    public void cleanup()
    {
        gpuMeshes.values().forEach(LabelGpuMesh::destroy);
        gpuMeshes.clear();

        if (this.labelShader != null)
        {
            this.labelShader.cleanup();
            this.labelShader = null;
        }
    }


    private void ensureShader(RenderContext context)
    {
        if (this.labelShader == null)
        {
            this.labelShader = context.backend().createShaderProgram(
                    "/shaders/msdf_label.vert",
                    "/shaders/msdf_label.frag",
                    null
            );
        }
    }


    private String resolveFontKey(LabelEntity entity)
    {
        return FontManager.keyForFont(entity.getFont());
    }

    private LabelGpuMesh getOrBuildGpuMesh(LabelEntity entity)
    {
        LabelGpuMesh existing = gpuMeshes.get(entity);

        if (entity.isMeshDirty() || existing == null)
        {
            MeshData meshData = entity.getMeshData();
            if (meshData == null || meshData.vertices.length == 0)
            {
                if (existing != null)
                {
                    existing.destroy();
                    gpuMeshes.remove(entity);
                }
                return null;
            }

            if (existing != null)
                existing.destroy();

            LabelGpuMesh fresh = LabelGpuMesh.upload(meshData, LABEL_LAYOUT);
            gpuMeshes.put(entity, fresh);
            return fresh;
        }

        return existing;
    }

    private Matrix4f buildModelMatrix(LabelEntity entity, Matrix4f view)
    {
        Matrix4f base = entity.getModelMatrix();

        float halfW = entity.getLayoutWidth() * 0.5f;

        if (!entity.isBillboard())
        {
            return new Matrix4f(base).translate(-halfW, 0f, 0f);
        }

        Vector3f pos = base.getTranslation(new Vector3f());
        Vector3f scale = new Vector3f(new Vector3f(base.m00(), base.m01(), base.m02()).length(), new Vector3f(base.m10(), base.m11(), base.m12()).length(), new Vector3f(base.m20(), base.m21(), base.m22()).length());

        Vector3f right = new Vector3f(view.m00(), view.m10(), view.m20()).normalize();
        Vector3f up    = new Vector3f(view.m01(), view.m11(), view.m21()).normalize();
        Vector3f fwd   = new Vector3f(view.m02(), view.m12(), view.m22()).normalize();

        float shiftX = halfW * scale.x;
        Vector3f anchor = new Vector3f(pos.x - right.x * shiftX, pos.y - right.y * shiftX, pos.z - right.z * shiftX);

        this.billboardModel.identity()
                // Column 0: right * scaleX
                .m00(right.x * scale.x).m01(right.y * scale.x).m02(right.z * scale.x)
                // Column 1: world-up * scaleY
                .m10(up.x * scale.y).m11(up.y * scale.y).m12(up.z * scale.y)
                // Column 2: forward * scaleZ
                .m20(fwd.x * scale.z).m21(fwd.y * scale.z).m22(fwd.z * scale.z)
                // Column 3: centered world-space position
                .m30(anchor.x).m31(anchor.y).m32(anchor.z).m33(1f);

        return this.billboardModel;
    }


    private static final class LabelGpuMesh
    {
        final Vao vao;
        final VertexBuffer vbo;
        final VertexBuffer ebo;
        final int indexCount;

        private LabelGpuMesh(Vao vao, VertexBuffer vbo, VertexBuffer ebo, int indexCount)
        {
            this.vao = vao;
            this.vbo = vbo;
            this.ebo = ebo;
            this.indexCount = indexCount;
        }

        static LabelGpuMesh upload(MeshData data, BufferLayout layout)
        {
            VertexBuffer vbo = new VertexBuffer(data.vertices, GpuBufferUsage.DYNAMIC);
            VertexBuffer ebo = new VertexBuffer(data.indices, GpuBufferUsage.DYNAMIC);
            Vao vao = Vao.builder().bindVertexBuffer(vbo, layout).elementBuffer(ebo).build();
            return new LabelGpuMesh(vao, vbo, ebo, data.indices.length);
        }

        void destroy()
        {
            vao.destroy();
            vbo.destroy();
            ebo.destroy();
        }
    }
}
