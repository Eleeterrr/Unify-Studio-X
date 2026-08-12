package eleeter.unifystudiox.renderer.lighting;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import eleeter.unifystudiox.graphics.Vao;
import eleeter.unifystudiox.graphics.VertexBuffer;
import eleeter.unifystudiox.graphics.api.IShaderProgram;
import eleeter.unifystudiox.graphics.api.PrimitiveType;
import eleeter.unifystudiox.graphics.buffer.GpuBufferUsage;
import eleeter.unifystudiox.graphics.draw.Draw3D;
import eleeter.unifystudiox.graphics.gfx.PipelineState;
import eleeter.unifystudiox.graphics.layout.AttributeType;
import eleeter.unifystudiox.graphics.layout.BufferLayout;
import eleeter.unifystudiox.renderer.core.RenderBucket;
import eleeter.unifystudiox.renderer.core.RenderCommand;
import eleeter.unifystudiox.renderer.core.RenderContext;
import eleeter.unifystudiox.renderer.core.RenderPass;
import eleeter.unifystudiox.renderer.entities.EntityRenderer;
import eleeter.unifystudiox.scene.PointLightData;
import eleeter.unifystudiox.scene.entity.PointLightEntity;

public class PointLightRenderer implements EntityRenderer<PointLightEntity>
{
    private static final int SPHERE_SEGMENTS = 24;
    private static final float WIRE_ALPHA = 0.82F;
    private static final float GIZMO_RADIUS = 0.35F;

    private static final BufferLayout LAYOUT = BufferLayout.builder()
            .add(0, 3, AttributeType.FLOAT)
            .add(1, 3, AttributeType.FLOAT)
            .build();

    private Vao vao;
    private VertexBuffer vbo;
    private int count;
    private boolean built = false;

    private IShaderProgram shader;
    private boolean initialized = false;

    @Override
    public Class<PointLightEntity> getSupportedType()
    {
        return PointLightEntity.class;
    }

    @Override
    public void submitCommands(PointLightEntity entity, RenderContext context)
    {
        if (context.pass() == RenderPass.SHADOW_DEPTH)
        {
            return;
        }

        if (!this.initialized)
        {
            initGpuResources(context);
        }

        if (!this.built)
        {
            buildSphere();
        }

        RenderCommand cmd = context.bucketManager().allocateCommand();
        cmd.sortKey = ((long) this.shader.hashCode() << 32);
        cmd.shader = this.shader;
        cmd.texture = null;
        cmd.vao = this.vao;
        cmd.count = this.count;
        cmd.indexed = false;
        cmd.primitiveType = PrimitiveType.LINES;
        cmd.state = PipelineState.TRANSPARENT;
        cmd.renderer = this;
        cmd.entity = entity;
        cmd.customId = 0;

        context.bucketManager().submit(RenderBucket.WORLD_2D, cmd);
    }

    @Override
    public void setupUniforms(IShaderProgram shader, PointLightEntity entity, int customId, RenderContext context)
    {
        FloatBuffer fb = context.matrixBuffer();
        shader.setUniform("uProjection", context.projectionMatrix(), fb);
        shader.setUniform("uView", context.viewMatrix(), fb);
        shader.setUniform("uModel", entity.getModelMatrix(), fb);

        PointLightData data = entity.getData();
        shader.setUniform("uColor", data.color.x, data.color.y, data.color.z);
        shader.setUniform("uAlpha", WIRE_ALPHA);
    }

    private void initGpuResources(RenderContext context)
    {
        this.shader = context.backend().createShaderProgram("/shaders/spotlight.vert", "/shaders/spotlight.frag", null);
        this.initialized = true;
    }

    private void buildSphere()
    {
        List<Float> list = new ArrayList<>();

        Draw3D.wireSphere(list, new Vector3f(0f, 0f, 0f), GIZMO_RADIUS, SPHERE_SEGMENTS, 1.0F, 1.0F, 1.0F);

        this.count = list.size() / 6;

        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++)
        {
            arr[i] = list.get(i);
        }

        this.vbo = new VertexBuffer(arr, GpuBufferUsage.STATIC);
        this.vao = Vao.builder().bindVertexBuffer(this.vbo, LAYOUT).build();

        this.built = true;
    }

    @Override
    public void cleanup()
    {
        if (this.vao != null)
        {
            this.vao.destroy();
            this.vbo.destroy();
        }
        if (this.shader != null)
        {
            this.shader.cleanup();
        }
    }
}
