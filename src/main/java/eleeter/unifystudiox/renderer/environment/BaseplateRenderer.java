package eleeter.unifystudiox.renderer.environment;

import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.graphics.math.TransformStack;
import eleeter.unifystudiox.graphics.Vao;
import eleeter.unifystudiox.graphics.math.Geometry;
import eleeter.unifystudiox.graphics.math.GeometryData;
import eleeter.unifystudiox.graphics.VertexBuffer;
import eleeter.unifystudiox.graphics.api.IShaderProgram;
import eleeter.unifystudiox.graphics.buffer.GpuBufferUsage;
import eleeter.unifystudiox.graphics.gfx.PipelineState;
import eleeter.unifystudiox.graphics.layout.AttributeType;
import eleeter.unifystudiox.graphics.layout.BufferLayout;
import eleeter.unifystudiox.renderer.core.RenderBucket;
import eleeter.unifystudiox.renderer.core.RenderCommand;
import eleeter.unifystudiox.renderer.core.RenderContext;
import eleeter.unifystudiox.renderer.core.RenderPass;
import eleeter.unifystudiox.renderer.entities.EntityRenderer;
import eleeter.unifystudiox.renderer.lighting.ParticleLightUploader;
import eleeter.unifystudiox.renderer.lighting.PointLightUploader;
import eleeter.unifystudiox.renderer.lighting.SpotlightUploader;
import eleeter.unifystudiox.scene.entity.BaseplateEntity;

public class BaseplateRenderer implements EntityRenderer<BaseplateEntity>
{

    private static final BufferLayout BASEPLATE_LAYOUT = BufferLayout.builder().add(0, 3, AttributeType.FLOAT)
            .add(1, 3, AttributeType.FLOAT).add(2, 2, AttributeType.FLOAT).build();

    private Vao vaoObject;
    private VertexBuffer vboObject;
    private VertexBuffer eboObject;
    private IShaderProgram shadowDepthShader;

    private final TransformStack stack = new TransformStack(8);

    private boolean initialized = false;

    @Override
    public Class<BaseplateEntity> getSupportedType()
    {
        return BaseplateEntity.class;
    }

    private void initGpuResources(BaseplateEntity entity)
    {
        GeometryData geometry = Geometry.cube(1.0F, 1.0F, 1.0F);

        this.vboObject = new VertexBuffer(geometry.vertices(), GpuBufferUsage.STATIC);

        this.eboObject = new VertexBuffer(geometry.indices(), GpuBufferUsage.STATIC);

        this.vaoObject = Vao.builder().bindVertexBuffer(this.vboObject, BASEPLATE_LAYOUT).elementBuffer(this.eboObject).build();

        this.initialized = true;
    }

    @Override
    public void submitCommands(BaseplateEntity entity, RenderContext context)
    {
        if (!this.initialized)
            initGpuResources(entity);

        if (context.pass() == RenderPass.SHADOW_DEPTH)
        {
            if (this.shadowDepthShader == null)
            {
                this.shadowDepthShader = context.backend().createShaderProgram("/shaders/obj_depth.vert", "/shaders/obj_depth.frag", null);
            }

            RenderCommand cmd = context.bucketManager().allocateCommand();
            cmd.sortKey = ((long) this.shadowDepthShader.hashCode() << 32) | this.vaoObject.getHandle();
            cmd.shader = this.shadowDepthShader;
            cmd.texture = null;
            cmd.vao = this.vaoObject;
            cmd.count = 36;
            cmd.indexed = true;
            cmd.state = PipelineState.SHADOW;
            cmd.renderer = this;
            cmd.entity = entity;
            cmd.customId = 0;
            context.bucketManager().submit(RenderBucket.SOLID_3D, cmd);
            return;
        }

        IShaderProgram shader = context.shader();
        TextureGL tex = entity.getTexture();

        this.stack.identity().mul(entity.getModelMatrix());

        RenderCommand cmd1 = context.bucketManager().allocateCommand();

        cmd1.sortKey = ((long) shader.hashCode() << 32) | (tex != null ? tex.hashCode() : 0);
        cmd1.shader = shader;
        cmd1.texture = tex;
        cmd1.vao = this.vaoObject;
        cmd1.count = 36;
        cmd1.indexed = true;
        cmd1.renderer = this;
        cmd1.entity = entity;
        cmd1.customId = 0;

        context.bucketManager().submit(RenderBucket.SOLID_3D, cmd1);

        this.stack.push();
        this.stack.translate(0.45f, 0.05f, 0.45f).scale(100.05f);

        RenderCommand cmd2 = context.bucketManager().allocateCommand();
        cmd2.sortKey = ((long) shader.hashCode() << 32);
        cmd2.shader = shader;
        cmd2.texture = null;
        cmd2.vao = this.vaoObject;
        cmd2.count = 36;
        cmd2.indexed = true;
        cmd2.renderer = this;
        cmd2.entity = entity;
        cmd2.customId = 1;
        cmd2.modelMatrix.set(this.stack.last());

        context.bucketManager().submit(RenderBucket.SOLID_3D, cmd2);

        this.stack.pop();
    }

    @Override
    public void setupUniforms(IShaderProgram shader, BaseplateEntity entity, int customId, RenderContext context)
    {
        if (context.pass() == RenderPass.SHADOW_DEPTH)
        {
            shader.setUniform("uModel", entity.getModelMatrix(), context.matrixBuffer());
            return;
        }

        if (customId == 0)
        {
            shader.setUniform("uModel", entity.getModelMatrix(), context.matrixBuffer());
            shader.setUniform("uBaseColor", entity.getColor().x, entity.getColor().y, entity.getColor().z);
            SpotlightUploader.upload(shader, context.scene());
            PointLightUploader.upload(shader, context.scene());
            ParticleLightUploader.upload(shader, context.scene(), context.matrixBuffer());
        } else if (customId == 1)
        {
            this.stack.identity().mul(entity.getModelMatrix());
            this.stack.translate(0.45f, 0.05f, 0.45f).scale(0.05f);

            shader.setUniform("uModel", this.stack.last(), context.matrixBuffer());
            shader.setUniform("uBaseColor", 1.0f, 0.5f, 0.0f);
        }
    }

    @Override
    public void cleanup()
    {
        if (this.initialized)
        {
            this.vaoObject.destroy();
            this.vboObject.destroy();
            this.eboObject.destroy();
        }

        if (this.shadowDepthShader != null)
        {
            this.shadowDepthShader.cleanup();
            this.shadowDepthShader = null;
        }
    }
}
