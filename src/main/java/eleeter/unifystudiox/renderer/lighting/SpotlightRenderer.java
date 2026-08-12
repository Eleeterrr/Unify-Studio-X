package eleeter.unifystudiox.renderer.lighting;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import eleeter.unifystudiox.graphics.TransformStack;
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
import eleeter.unifystudiox.scene.SpotlightData;
import eleeter.unifystudiox.scene.entity.SpotlightEntity;

public class SpotlightRenderer implements EntityRenderer<SpotlightEntity>
{
    private static final int CONE_SEGMENTS = 24;
    private static final float WIRE_ALPHA = 0.82F;

    private static final BufferLayout LAYOUT = BufferLayout.builder()
            .add(0, 3, AttributeType.FLOAT)
            .add(1, 3, AttributeType.FLOAT)
            .build();

    private Vao vao;
    private VertexBuffer vbo;
    private int count;

    /* state tracking for lazy geometry rebuild */
    private float cachedCutoffDeg = -1.0F;
    private float cachedRange     = -1.0F;

    private IShaderProgram shader;
    private boolean initialized = false;

    @Override
    public Class<SpotlightEntity> getSupportedType()
    {
        return SpotlightEntity.class;
    }

    @Override
    public void submitCommands(SpotlightEntity entity, RenderContext context)
    {
        if (context.pass() == RenderPass.SHADOW_DEPTH)
        {
            return;
        }

        if (!this.initialized)
        {
            initGpuResources(context);
        }

        SpotlightData data = entity.getData();

        if (data.outerCutoffDeg != this.cachedCutoffDeg || data.range != this.cachedRange)
        {
            rebuildCone(data.outerCutoffDeg, data.range);
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
    public void setupUniforms(IShaderProgram shader, SpotlightEntity entity, int customId, RenderContext context)
    {
        FloatBuffer fb = context.matrixBuffer();
        shader.setUniform("uProjection", context.projectionMatrix(), fb);
        shader.setUniform("uView", context.viewMatrix(), fb);
        shader.setUniform("uModel", entity.getModelMatrix(), fb);

        SpotlightData data = entity.getData();
        shader.setUniform("uColor", data.color.x, data.color.y, data.color.z);
        shader.setUniform("uAlpha", WIRE_ALPHA);
    }
    

    private void initGpuResources(RenderContext context)
    {
        this.shader = context.backend().createShaderProgram("/shaders/spotlight.vert", "/shaders/spotlight.frag", null);
        this.initialized = true;
    }

    private void rebuildCone(float outerCutoffDeg, float height)
    {
        if (this.vao != null)
        {
            this.vao.destroy();
            this.vbo.destroy();
        }

        float radius = (float) (Math.tan(Math.toRadians(outerCutoffDeg)) * height);

        List<Float> list = new ArrayList<>();
        TransformStack stack = new TransformStack(16);
        stack.push();

        Vector3f base = new Vector3f(0.0F, -height, 0.0F);
        Vector3f tip = new Vector3f(0.0F, 0.0F, 0.0F);

        Draw3D.wireCone(list, stack, base, tip, radius, CONE_SEGMENTS, 1.0F, 1.0F, 1.0F);

        stack.pop();

        this.count = list.size() / 6;

        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++)
        {
            arr[i] = list.get(i);
        }

        this.vbo = new VertexBuffer(arr, GpuBufferUsage.DYNAMIC);
        this.vao = Vao.builder().bindVertexBuffer(this.vbo, LAYOUT).build();

        this.cachedCutoffDeg = outerCutoffDeg;
        this.cachedRange     = height;
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
