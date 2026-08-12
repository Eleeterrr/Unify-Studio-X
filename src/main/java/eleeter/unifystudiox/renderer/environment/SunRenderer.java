package eleeter.unifystudiox.renderer.environment;

import java.nio.FloatBuffer;

import org.joml.Vector3f;

import eleeter.unifystudiox.graphics.Vao;
import eleeter.unifystudiox.graphics.VertexBuffer;
import eleeter.unifystudiox.graphics.api.IShaderProgram;
import eleeter.unifystudiox.graphics.api.PrimitiveType;
import eleeter.unifystudiox.graphics.buffer.GpuBufferUsage;
import eleeter.unifystudiox.graphics.gfx.PipelineState;
import eleeter.unifystudiox.graphics.layout.AttributeType;
import eleeter.unifystudiox.graphics.layout.BufferLayout;
import eleeter.unifystudiox.renderer.core.RenderBucket;
import eleeter.unifystudiox.renderer.core.RenderCommand;
import eleeter.unifystudiox.renderer.core.RenderContext;
import eleeter.unifystudiox.renderer.core.RenderPass;
import eleeter.unifystudiox.renderer.entities.EntityRenderer;
import eleeter.unifystudiox.scene.Environment;
import eleeter.unifystudiox.scene.entity.SunEntity;

public class SunRenderer implements EntityRenderer<SunEntity>
{
    private static final BufferLayout LAYOUT = BufferLayout.builder()
            .add(0, 2, AttributeType.FLOAT)
            .build();

    private Vao vao;
    private VertexBuffer vbo;
    private IShaderProgram shader;
    private boolean initialized = false;

    @Override
    public Class<SunEntity> getSupportedType()
    {
        return SunEntity.class;
    }

    private void initGpuResources(RenderContext context)
    {
        float[] vertices =
        {
                -1.0F, -1.0F,
                 1.0F, -1.0F,
                 1.0F,  1.0F,
                -1.0F,  1.0F
        };

        this.vbo = new VertexBuffer(vertices, GpuBufferUsage.STATIC);
        this.vao = Vao.builder().bindVertexBuffer(this.vbo, LAYOUT).build();

        this.shader = context.backend().createShaderProgram("/shaders/sun.vert", "/shaders/sun.frag", null);

        this.initialized = true;
    }

    @Override
    public void submitCommands(SunEntity sun, RenderContext context)
    {
        if (context.pass() == RenderPass.SHADOW_DEPTH)
        {
            return;
        }
        if (!this.initialized) initGpuResources(context);

        RenderCommand cmd = context.bucketManager().allocateCommand();
        cmd.sortKey = ((long) this.shader.hashCode() << 32);
        cmd.shader = this.shader;
        cmd.texture = null;
        cmd.vao = this.vao;
        cmd.count = 4;
        cmd.indexed = false;
        cmd.primitiveType = PrimitiveType.TRIANGLE_FAN;
        cmd.state = PipelineState.ADDITIVE;
        cmd.renderer = this;
        cmd.entity = sun;
        cmd.customId = 0;
        
        context.bucketManager().submit(RenderBucket.BACKGROUND, cmd);
    }
    
    @Override
    public void setupUniforms(IShaderProgram shader, SunEntity sun, int customId, RenderContext context)
    {
        FloatBuffer fb = context.matrixBuffer();
        shader.setUniform("u_projection", context.projectionMatrix(), fb);
        shader.setUniform("u_view", context.viewMatrix(), fb);

        Environment env = context.scene().getEnvironment();
        Vector3f dir = env.getSunDirection();
        Vector3f color = env.getSunColor();

        shader.setUniform("u_sunDir", dir.x, dir.y, dir.z);
        shader.setUniform("u_sunColor", color.x, color.y, color.z);
        shader.setUniform("u_intensity", env.getSunIntensity());
        shader.setUniform("u_size", sun.getSize());
    }
    

    @Override
    public void cleanup()
    {
        if (this.initialized)
        {
            this.vao.destroy();
            this.vbo.destroy();
            this.shader.cleanup();
        }
    }
}
