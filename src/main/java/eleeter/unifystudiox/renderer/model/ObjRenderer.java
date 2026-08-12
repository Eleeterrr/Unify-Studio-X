package eleeter.unifystudiox.renderer.model;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import eleeter.unifystudiox.graphics.Vao;
import eleeter.unifystudiox.graphics.VertexBuffer;
import eleeter.unifystudiox.graphics.api.IShaderProgram;
import eleeter.unifystudiox.graphics.buffer.GpuBufferUsage;
import eleeter.unifystudiox.graphics.gfx.PipelineState;
import eleeter.unifystudiox.graphics.layout.AttributeType;
import eleeter.unifystudiox.graphics.layout.BufferLayout;
import eleeter.unifystudiox.obj.ObjMesh;
import eleeter.unifystudiox.obj.ObjModelInstance;
import eleeter.unifystudiox.renderer.core.RenderBucket;
import eleeter.unifystudiox.renderer.core.RenderCommand;
import eleeter.unifystudiox.renderer.core.RenderContext;
import eleeter.unifystudiox.renderer.core.RenderPass;
import eleeter.unifystudiox.renderer.entities.EntityRenderer;

public class ObjRenderer implements EntityRenderer<ObjModelInstance>
{
    private static final BufferLayout LAYOUT = BufferLayout.builder()
            .add(0, 3, AttributeType.FLOAT)
            .add(2, 2, AttributeType.FLOAT)
            .add(1, 3, AttributeType.FLOAT)
            .build();

    private record GpuMesh(Vao vao, VertexBuffer vbo, VertexBuffer ebo, int indexCount)
    {
    }

    private final Map<ObjMesh, GpuMesh> meshCache;
    private IShaderProgram shadowDepthShader;
    private IShaderProgram outlinePassShader;
    private boolean initialized = false;

    public ObjRenderer()
    {
        this.meshCache = new HashMap<>();
    }

    @Override
    public Class<ObjModelInstance> getSupportedType()
    {
        return ObjModelInstance.class;
    }

    @Override
    public void submitCommands(ObjModelInstance entity, RenderContext ctx)
    {
        if (!this.initialized)
        {
            this.shadowDepthShader = ctx.backend().createShaderProgram("/shaders/obj_depth.vert", "/shaders/obj_depth.frag", null);
            this.outlinePassShader = ctx.backend().createShaderProgram("/shaders/obj_outline.vert", "/shaders/obj_outline.frag", null);
            this.initialized = true;
        }

        if (ctx.pass() == RenderPass.SHADOW_DEPTH)
        {
            for (ObjMesh mesh : entity.getModel().getMeshes())
            {
                GpuMesh gpu = this.meshCache.computeIfAbsent(mesh, this::upload);

                RenderCommand cmd = ctx.bucketManager().allocateCommand();
                cmd.sortKey = ((long) this.shadowDepthShader.hashCode() << 32L) | (gpu.vao().getHandle());
                cmd.shader = this.shadowDepthShader;
                cmd.texture = null;
                cmd.vao = gpu.vao();
                cmd.count = gpu.indexCount();
                cmd.indexed = true;
                cmd.state = PipelineState.SHADOW;
                cmd.renderer = this;
                cmd.entity = entity;
                cmd.customId = 1;

                ctx.bucketManager().submit(RenderBucket.SOLID_3D, cmd);
            }
            
            return;
        }

        for (ObjMesh mesh : entity.getModel().getMeshes())
        {
            GpuMesh gpu = this.meshCache.computeIfAbsent(mesh, this::upload);

            RenderCommand cmd = ctx.bucketManager().allocateCommand();
            cmd.sortKey = ((long) ctx.shader().hashCode() << 32L) | (gpu.vao().getHandle());
            cmd.shader = ctx.shader();
            cmd.texture = entity.getTexture(mesh);
            cmd.vao = gpu.vao();
            cmd.count = gpu.indexCount();
            cmd.indexed = true;
            cmd.state = PipelineState.OPAQUE;
            cmd.renderer = this;
            cmd.entity = entity;
            cmd.customId = 0;

            ctx.bucketManager().submit(RenderBucket.SOLID_3D, cmd);
        }

        if (ctx.scene().getHoveredEntity() == entity)
        {
            for (ObjMesh mesh : entity.getModel().getMeshes())
            {
                GpuMesh gpu = this.meshCache.get(mesh);
                if (gpu == null) continue;

                RenderCommand cmd = ctx.bucketManager().allocateCommand();
                cmd.sortKey = ((long) this.outlinePassShader.hashCode() << 32L) | (gpu.vao().getHandle());
                cmd.shader = this.outlinePassShader;
                cmd.texture = null;
                cmd.vao = gpu.vao();
                cmd.count = gpu.indexCount();
                cmd.indexed = true;
                cmd.state = PipelineState.WIREFRAME_OVERLAY;
                cmd.renderer = this;
                cmd.entity = entity;
                cmd.customId = 2;

                ctx.bucketManager().submit(RenderBucket.SOLID_3D, cmd);
            }
        }
    }

    @Override
    public void setupUniforms(IShaderProgram shader, ObjModelInstance entity, int customId, RenderContext context)
    {
        FloatBuffer buf = context.matrixBuffer();
        
        if (customId == 1)
        {
            shader.setUniform("uProjection", context.projectionMatrix(), buf);
            shader.setUniform("uView", context.viewMatrix(), buf);
            entity.getModelMatrix().get(buf);
            shader.setUniformMatrix4f("uModel", buf);
        }
        else if (customId == 2)
        {
            shader.setUniform("uProjection", context.projectionMatrix(), buf);
            shader.setUniform("uView", context.viewMatrix(), buf);
            entity.getModelMatrix().get(buf);
            shader.setUniformMatrix4f("uModel", buf);
            shader.setUniform("uOutlineColor", 1.0F, 1.0F, 0.0F);
        }
        else
        {
            entity.getModelMatrix().get(buf);
            shader.setUniformMatrix4f("uModel", buf);
        }
    }



    @Override
    public void cleanup()
    {
        if (this.initialized)
        {
            this.shadowDepthShader.cleanup();
            this.outlinePassShader.cleanup();
        }

        for (GpuMesh gpu : this.meshCache.values())
        {
            gpu.vbo().destroy();
            gpu.ebo().destroy();
            gpu.vao().destroy();
        }
        
        this.meshCache.clear();
    }

    private GpuMesh upload(ObjMesh mesh)
    {
        VertexBuffer vbo = new VertexBuffer(mesh.getVertexData(), GpuBufferUsage.STATIC);
        VertexBuffer ebo = new VertexBuffer(mesh.getIndices(), GpuBufferUsage.STATIC);

        Vao vao = Vao.builder().bindVertexBuffer(vbo, LAYOUT).elementBuffer(ebo).build();

        return new GpuMesh(vao, vbo, ebo, mesh.getIndices().length);
    }
}
