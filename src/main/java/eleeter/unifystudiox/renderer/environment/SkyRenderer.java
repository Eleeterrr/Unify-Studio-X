package eleeter.unifystudiox.renderer.environment;

import java.nio.FloatBuffer;

import org.joml.Vector3f;

import eleeter.unifystudiox.graphics.Vao;
import eleeter.unifystudiox.graphics.VertexBuffer;
import eleeter.unifystudiox.graphics.api.IShaderProgram;
import eleeter.unifystudiox.graphics.buffer.GpuBufferUsage;
import eleeter.unifystudiox.graphics.gfx.PipelineState;
import eleeter.unifystudiox.graphics.math.Geometry;
import eleeter.unifystudiox.graphics.math.GeometryData;
import eleeter.unifystudiox.graphics.layout.AttributeType;
import eleeter.unifystudiox.graphics.layout.BufferLayout;
import eleeter.unifystudiox.renderer.core.RenderBucket;
import eleeter.unifystudiox.renderer.core.RenderCommand;
import eleeter.unifystudiox.renderer.core.RenderContext;
import eleeter.unifystudiox.renderer.core.RenderPass;
import eleeter.unifystudiox.renderer.core.RenderSettings;
import eleeter.unifystudiox.renderer.entities.EntityRenderer;
import eleeter.unifystudiox.scene.Environment;
import eleeter.unifystudiox.scene.entity.SkyEntity;

public class SkyRenderer implements EntityRenderer<SkyEntity>
{
    private static final BufferLayout LAYOUT = BufferLayout.builder()
            .add(0, 3, AttributeType.FLOAT)
            .build();

    private Vao vao;
    private VertexBuffer vbo;
    private IShaderProgram shader;
    private boolean initialized = false;
    private final long startNanos = System.nanoTime();

    @Override
    public Class<SkyEntity> getSupportedType()
    {
        return SkyEntity.class;
    }

    private void initGpuResources(RenderContext context)
    {

        GeometryData geometry = Geometry.cubePositionUnindexed(2.0F, 2.0F, 2.0F);
        this.vbo = new VertexBuffer(geometry.vertices(), GpuBufferUsage.STATIC);
        this.vao = Vao.builder().bindVertexBuffer(this.vbo, LAYOUT).build();

        /* TODO: ADD THESE SHADER PATH IN THE Shaders class.
        * I'm too lazy to set uniforms.
        * */
        this.shader = context.backend().createShaderProgram("/shaders/sky.vert", "/shaders/sky.frag", null);

        this.initialized = true;
    }

    @Override
    public void submitCommands(SkyEntity sky, RenderContext context)
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
        cmd.count = 36;
        cmd.indexed = false;
        cmd.state = PipelineState.SKY;
        cmd.renderer = this;
        cmd.entity = sky;
        cmd.customId = 0;

        context.bucketManager().submit(RenderBucket.BACKGROUND, cmd);
    }

    @Override
    public void setupUniforms(IShaderProgram shader, SkyEntity sky, int customId, RenderContext context)
    {
        FloatBuffer fb = context.matrixBuffer();
        shader.setUniform("uProjection", context.projectionMatrix(), fb);
        shader.setUniform("uView", context.viewMatrix(), fb);

        shader.setUniform("uTopColor", sky.getTopColor().x, sky.getTopColor().y, sky.getTopColor().z);
        shader.setUniform("uBottomColor", sky.getBottomColor().x, sky.getBottomColor().y, sky.getBottomColor().z);
        shader.setUniform("uHorizonColor", sky.getHorizonColor().x, sky.getHorizonColor().y, sky.getHorizonColor().z);

        shader.setUniform("uHaze", sky.getHaze());
        shader.setUniform("uSunSize", sky.getSunSize());
        shader.setUniform("uNightMode", RenderSettings.NIGHT_MODE);
        shader.setUniform("uTime", (float) ((System.nanoTime() - this.startNanos) * 1e-9));

        Environment env = context.scene().getEnvironment();
        Vector3f sunDir = env.getSunDirection();
        shader.setUniform("uSunDir", -sunDir.x, -sunDir.y, -sunDir.z);
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
