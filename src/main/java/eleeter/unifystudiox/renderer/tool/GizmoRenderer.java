package eleeter.unifystudiox.renderer.tool;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import eleeter.unifystudiox.graphics.math.TransformStack;
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
import eleeter.unifystudiox.scene.entity.gizmo.GizmoEntity;

public class GizmoRenderer implements EntityRenderer<GizmoEntity>
{
    private static final BufferLayout LAYOUT = BufferLayout.builder()
            .add(0, 3, AttributeType.FLOAT)
            .add(1, 3, AttributeType.FLOAT)
            .build();

    private Vector3f gizmoOrigin;
    private Vector3f cameraPos;
    private Matrix4f invView;
    private Matrix4f unifiedModel;
    private TransformStack matrixStack;
    private float scaleFactor;
    private FloatBuffer fb;

    private Vao vaoTranslate;
    private VertexBuffer vboTranslate;
    private int countTranslate;

    private Vao vaoRotate;
    private VertexBuffer vboRotate;
    private int countRotate;

    private Vao vaoScale;
    private VertexBuffer vboScale;
    private int countScale;

    private IShaderProgram shader;
    private boolean initialized = false;


    public TransformStack MatrixStack()
    {
        return this.matrixStack;
    }

    @Override
    public Class<GizmoEntity> getSupportedType()
    {
        return GizmoEntity.class;
    }

    @Override
    public void submitCommands(GizmoEntity gizmo, RenderContext context)
    {
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
        
        switch (gizmo.getMode())
        {
            case TRANSLATE ->
            {
                cmd.vao = this.vaoTranslate;
                cmd.count = this.countTranslate;
            }
            case SCALE ->
            {
                cmd.vao = this.vaoScale;
                cmd.count = this.countScale;
            }
            case ROTATE ->
            {
                cmd.vao = this.vaoRotate;
                cmd.count = this.countRotate;
            }
        }
        
        cmd.indexed = false;
        cmd.primitiveType = PrimitiveType.TRIANGLES;
        cmd.state = PipelineState.GIZMO;
        cmd.renderer = this;
        cmd.entity = gizmo;
        cmd.customId = 0;
        
        context.bucketManager().submit(RenderBucket.EDITOR, cmd);
    }
    
    @Override
    public void setupUniforms(IShaderProgram shader, GizmoEntity gizmo, int customId, RenderContext context)
    {
        this.fb = context.matrixBuffer();
        shader.setUniform("uProjection", context.projectionMatrix(), this.fb);
        shader.setUniform("uView", context.viewMatrix(), this.fb);
        
        this.invView = context.viewMatrix().invert(new Matrix4f());
        this.cameraPos = this.invView.getTranslation(new Vector3f());

        this.gizmoOrigin = gizmo.getModelMatrix().getTranslation(new Vector3f());
        float distToCam = this.cameraPos.distance(this.gizmoOrigin);

        this.scaleFactor = distToCam * 0.2F;

        this.unifiedModel = new Matrix4f().translationRotateScale(
                this.gizmoOrigin,
                gizmo.getRotation(),
                new Vector3f(this.scaleFactor, this.scaleFactor, this.scaleFactor)
        );
        shader.setUniform("uModel", this.unifiedModel, fb);

        shader.setUniform("uHighlightAxis", gizmo.getHoveredAxis().ordinal());
        shader.setUniform("uActiveAxis", gizmo.getActiveAxis().ordinal());
    }
    

    private void initGpuResources(RenderContext context)
    {
        TransformStack stack = new TransformStack(16);
        stack.push();

        List<Float> transData = new ArrayList<>();
        Draw3D.arrow(transData, stack, new Vector3f(1.0F, 0.0F, 0.0F), 0.85F, 0.035F, 0.08F, 16, 1.0F, 0.22F, 0.22F);
        Draw3D.arrow(transData, stack, new Vector3f(0.0F, 1.0F, 0.0F), 0.85F, 0.035F, 0.08F, 16, 0.22F, 1.0F, 0.22F);
        Draw3D.arrow(transData, stack, new Vector3f(0.0F, 0.0F, 1.0F), 0.85F, 0.035F, 0.08F, 16, 0.22F, 0.55F, 1.0F);
        this.countTranslate = transData.size() / 6;
        this.vboTranslate = createVbo(transData);
        this.vaoTranslate = Vao.builder().bindVertexBuffer(this.vboTranslate, LAYOUT).build();

        List<Float> scaleData = new ArrayList<>();
        Draw3D.scaleHandle(scaleData, stack, new Vector3f(1.0F, 0.0F, 0.0F), 0.85F, 0.035F, 0.08F, 16, 1.0F, 0.22F, 0.22F);
        Draw3D.scaleHandle(scaleData, stack, new Vector3f(0.0F, 1.0F, 0.0F), 0.85F, 0.035F, 0.08F, 16, 0.22F, 1.0F, 0.22F);
        Draw3D.scaleHandle(scaleData, stack, new Vector3f(0.0F, 0.0F, 1.0F), 0.85F, 0.035F, 0.08F, 16, 0.22F, 0.55F, 1.0F);
        this.countScale = scaleData.size() / 6;
        this.vboScale = createVbo(scaleData);
        this.vaoScale = Vao.builder().bindVertexBuffer(this.vboScale, LAYOUT).build();

        List<Float> rotateData = new ArrayList<>();
        Draw3D.ring(rotateData, stack, 'X', 1.0F, 0.045F, 64, 12, 1.0F, 0.22F, 0.22F);
        Draw3D.ring(rotateData, stack, 'Y', 1.0F, 0.045F, 64, 12, 0.22F, 1.0F, 0.22F);
        Draw3D.ring(rotateData, stack, 'Z', 1.0F, 0.045F, 64, 12, 0.22F, 0.55F, 1.0F);

        /* Central Free-Rotation Handle */
        Draw3D.box(rotateData, stack, new Vector3f(0.0F, 0.0F, 0.0F), 0.14F, 1.0F, 1.0F, 1.0F); // White Outline
        Draw3D.box(rotateData, stack, new Vector3f(0.0F, 0.0F, 0.0F), 0.12F, 1.0F, 1.0F, 0.1F); // Yellow Core

        this.countRotate = rotateData.size() / 6;
        this.vboRotate = createVbo(rotateData);
        this.vaoRotate = Vao.builder().bindVertexBuffer(this.vboRotate, LAYOUT).build();

        this.shader = context.backend().createShaderProgram("/shaders/gizmo.vert", "/shaders/gizmo.frag", null);
        this.initialized = true;

        stack.pop();
    }

    private VertexBuffer createVbo(List<Float> data)
    {
        float[] arr = new float[data.size()];
        for (int i = 0; i < data.size(); i++)
        {
            arr[i] = data.get(i);
        }
        return new VertexBuffer(arr, GpuBufferUsage.STATIC);
    }

    @Override
    public void cleanup()
    {
        if (!this.initialized)
        {
            return;
        }
        this.vaoTranslate.destroy();
        this.vboTranslate.destroy();
        this.vaoRotate.destroy();
        this.vboRotate.destroy();
        this.vaoScale.destroy();
        this.vboScale.destroy();
        this.shader.cleanup();
    }
}
