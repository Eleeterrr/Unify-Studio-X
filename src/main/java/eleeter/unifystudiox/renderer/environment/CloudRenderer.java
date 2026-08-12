package eleeter.unifystudiox.renderer.environment;

import java.nio.FloatBuffer;

import org.joml.Vector3f;

import eleeter.unifystudiox.graphics.Shaders;
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
import eleeter.unifystudiox.renderer.core.RenderSettings;
import eleeter.unifystudiox.renderer.entities.EntityRenderer;
import eleeter.unifystudiox.scene.Environment;
import eleeter.unifystudiox.scene.entity.CloudEntity;

public class CloudRenderer implements EntityRenderer<CloudEntity>
{
    private static final BufferLayout LAYOUT = BufferLayout.builder()
            .add(0, 3, AttributeType.FLOAT)
            .build();

    private Vao vao;
    private VertexBuffer vbo;
    private IShaderProgram shader;
    private boolean initialized = false;
    private final long startNanos = System.nanoTime();

    private static final float[] CUBE =
            {
                    -1.0F, 1.0F, -1.0F, -1.0F, -1.0F, -1.0F, 1.0F, -1.0F, -1.0F, 1.0F, -1.0F, -1.0F, 1.0F, 1.0F, -1.0F, -1.0F, 1.0F, -1.0F,
                    -1.0F, -1.0F, 1.0F, -1.0F, -1.0F, -1.0F, -1.0F, 1.0F, -1.0F, -1.0F, 1.0F, -1.0F, -1.0F, 1.0F, 1.0F, -1.0F, -1.0F, 1.0F,
                    1.0F, -1.0F, -1.0F, 1.0F, -1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, -1.0F, 1.0F, -1.0F, -1.0F,
                    -1.0F, -1.0F, 1.0F, 1.0F, -1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, -1.0F, 1.0F, 1.0F, -1.0F, -1.0F, 1.0F,
                    -1.0F, 1.0F, -1.0F, 1.0F, 1.0F, -1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, -1.0F, 1.0F, 1.0F, -1.0F, 1.0F, -1.0F,
                    -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, 1.0F, 1.0F, -1.0F, -1.0F, 1.0F, -1.0F, -1.0F, -1.0F, -1.0F, 1.0F, 1.0F, -1.0F, 1.0F
            };

    @Override
    public Class<CloudEntity> getSupportedType()
    {
        return CloudEntity.class;
    }

    private void initGpuResources(RenderContext context)
    {
        this.vbo = new VertexBuffer(CUBE, GpuBufferUsage.STATIC);
        this.vao = Vao.builder().bindVertexBuffer(this.vbo, LAYOUT).build();

        this.shader = Shaders.Cloud();
        this.initialized = true;
    }

    @Override
    public void submitCommands(CloudEntity cloud, RenderContext context)
    {
        if (!RenderSettings.CLOUDS_ENABLED)
        {
            return;
        }

        if (context.pass() == RenderPass.SHADOW_DEPTH)
        {
            return;
        }

        if (!this.initialized)
        {
            initGpuResources(context);
        }


        RenderCommand cmd = context.bucketManager().allocateCommand();
        cmd.sortKey = ((long) this.shader.hashCode() << 32);
        cmd.shader = this.shader;
        cmd.texture = null;
        cmd.vao = this.vao;
        cmd.count = 36;
        cmd.indexed = false;
        cmd.primitiveType = PrimitiveType.TRIANGLES;
        cmd.state = PipelineState.TRANSPARENT;
        cmd.renderer = this;
        cmd.entity = cloud;
        cmd.customId = 0;

        context.bucketManager().submit(RenderBucket.BACKGROUND, cmd);
    }

    @Override
    public void setupUniforms(IShaderProgram shader, CloudEntity cloud, int customId, RenderContext context)
    {
        FloatBuffer fb = context.matrixBuffer();
        shader.setUniform("uProjection", context.projectionMatrix(), fb);
        shader.setUniform("uView", context.viewMatrix(), fb);

        float time = (float) ((System.nanoTime() - this.startNanos) * 1e-9);
        shader.setUniform("uTime", time);

        shader.setUniform("uCoverage", RenderSettings.CLOUD_COVERAGE);
        shader.setUniform("uSpeed", RenderSettings.CLOUD_SPEED);
        shader.setUniform("uDensity", RenderSettings.CLOUD_DENSITY);
        shader.setUniform("uAltitude", RenderSettings.CLOUD_ALTITUDE);

        Vector3f w = cloud.getWindDir();
        shader.setUniform("uWindDir", w.x, w.y, w.z);

        Environment env = context.scene().getEnvironment();
        Vector3f sd = env.getSunDirection();
        shader.setUniform("uSunDir", sd.x, sd.y, sd.z);

        Vector3f sc = env.getSunColor();
        float si = env.getSunIntensity();
        shader.setUniform("uSunColor", sc.x * si, sc.y * si, sc.z * si);
        shader.setUniform("uNightMode", RenderSettings.NIGHT_MODE);
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
