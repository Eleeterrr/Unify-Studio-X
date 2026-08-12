package eleeter.unifystudiox.renderer.tool;

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
import eleeter.unifystudiox.scene.entity.gizmo.GizmoAxis;
import eleeter.unifystudiox.scene.entity.gizmo.ViewportGizmoEntity;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ViewportGizmoRenderer implements EntityRenderer<ViewportGizmoEntity>
{
    private static final BufferLayout LAYOUT = BufferLayout.builder()
            .add(0, 3, AttributeType.FLOAT)
            .add(1, 3, AttributeType.FLOAT)
            .add(2, 1, AttributeType.FLOAT)
            .build();

    private static final int SEG = 16;
    private static final float SHAFT_LEN = 0.70f;
    private static final float SHAFT_R = 0.034f;
    private static final float CONE_R = 0.092f;
    private static final float NEG_R = 0.12f;
    private static final float Y = 2F * 0.5F;

    private static final float[] X_COL = {0.95f, 0.20f, 0.20f};
    private static final float[] Y_COL = {0.20f, 0.88f, 0.20f};
    private static final float[] Z_COL = {0.20f, 0.48f, 1.00f};
    private static final float[] NEG_COL = {0.33f, 0.33f, 0.33f};

    private Vao vao;
    private VertexBuffer vbo;
    private int count;
    private IShaderProgram shader;
    private boolean initialized = false;

    @Override
    public Class<ViewportGizmoEntity> getSupportedType()
    {
        return ViewportGizmoEntity.class;
    }


    @Override
    public void submitCommands(ViewportGizmoEntity entity, RenderContext context)
    {
        if (context.pass() == RenderPass.SHADOW_DEPTH) return;
        if (!this.initialized) initGpuResources(context);

        RenderCommand cmd = context.bucketManager().allocateCommand();
        cmd.shader = this.shader;
        cmd.vao = this.vao;
        cmd.count = this.count;
        cmd.primitiveType = PrimitiveType.TRIANGLES;
        cmd.state = PipelineState.GIZMO;
        cmd.renderer = this;
        cmd.entity = entity;

        context.bucketManager().submit(RenderBucket.EDITOR, cmd);
    }


    @Override
    public void setupUniforms(IShaderProgram shader, ViewportGizmoEntity entity, int customId, RenderContext context)
    {
        Matrix4f rotationMatrix = new Matrix4f(context.viewMatrix());
        rotationMatrix.setTranslation(0.0F, Y * -0.5F, 0.0F);

        float aspect = (float) context.scene().getUi().getRoot().getTransform().getComputedW() / context.scene().getUi().getRoot().getTransform().getComputedH();

        Matrix4f projection = new Matrix4f().setOrtho(
                -aspect * 10.0F, aspect * 10.0F, -10.0F, 10.0F, -20.0F, 20.0F);

        float xOff = aspect * 8.5F;
        float yOff = 8.5F;
        Matrix4f model = new Matrix4f().translation(xOff, yOff, -5.5F).mul(rotationMatrix).scale(1.5F);

        shader.setUniform("uProjection", projection, context.matrixBuffer());
        shader.setUniform("uView", new Matrix4f().identity(), context.matrixBuffer());
        shader.setUniform("uModel", model, context.matrixBuffer());
        shader.setUniform("uHighlightAxis", entity.getHoveredAxis().ordinal());
        shader.setUniform("uActiveAxis", 0);
    }


    private void initGpuResources(RenderContext context)
    {
        List<Float> data = new ArrayList<>();

        buildPositiveAxis(data, GizmoAxis.X, X_COL);
        buildPositiveAxis(data, GizmoAxis.Y, Y_COL);
        buildPositiveAxis(data, GizmoAxis.Z, Z_COL);

        addSphere(data, new Vector3f(-1f, 0f, 0f), NEG_R, NEG_COL, 3f);
        addSphere(data, new Vector3f(0f, -1f, 0f), NEG_R, NEG_COL, 4f);
        addSphere(data, new Vector3f(0f, 0f, -1f), NEG_R, NEG_COL, 5f);

        this.count = data.size() / 7;   // 3 pos + 3 color + 1 axisId

        float[] arr = new float[data.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = data.get(i);

        this.vbo = new VertexBuffer(arr, GpuBufferUsage.STATIC);
        this.vao = Vao.builder().bindVertexBuffer(this.vbo, LAYOUT).build();
        this.shader = context.backend().createShaderProgram("/shaders/gizmo.vert", "/shaders/gizmo.frag", null);

        this.initialized = true;
    }


    private void buildPositiveAxis(List<Float> data, GizmoAxis axis, float[] col)
    {
        float id = (float) axis.ordinal();
        Vector3f dir = axis.direction();
        Vector3f shaftEnd = new Vector3f(dir).mul(SHAFT_LEN);
        Vector3f coneTip = new Vector3f(dir);           // full unit length

        addCylinder(data, new Vector3f(0, 0, 0), shaftEnd, SHAFT_R, SEG, col, id);
        addCone(data, shaftEnd, coneTip, CONE_R, SEG, col, id);
    }


    private void addCylinder(List<Float> data, Vector3f from, Vector3f to,
                             float radius, int segs, float[] col, float axisId)
    {
        Vector3f ax = new Vector3f(to).sub(from).normalize();
        Vector3f[] b = buildRing(from, ax, radius, segs);
        Vector3f[] t = buildRing(to, ax, radius, segs);

        for (int i = 0; i < segs; i++)
        {
            int j = (i + 1) % segs;
            tri(data, b[i], t[i], b[j], col, axisId);
            tri(data, b[j], t[i], t[j], col, axisId);
        }
        fanCap(data, from, b, segs, col, axisId, true);
        fanCap(data, to, t, segs, col, axisId, false);
    }

    private void addCone(List<Float> data, Vector3f base, Vector3f tip,
                         float radius, int segs, float[] col, float axisId)
    {
        Vector3f ax = new Vector3f(tip).sub(base).normalize();
        Vector3f[] ring = buildRing(base, ax, radius, segs);

        for (int i = 0; i < segs; i++)
        {
            int j = (i + 1) % segs;
            tri(data, tip, ring[i], ring[j], col, axisId);
        }
        fanCap(data, base, ring, segs, col, axisId, true);
    }

    private void addSphere(List<Float> data, Vector3f center, float radius,
                           float[] col, float axisId)
    {
        final int R = 8, S = 12;
        for (int r = 0; r < R; r++)
        {
            for (int s = 0; s < S; s++)
            {
                float p0 = (float) (Math.PI * r / R);
                float p1 = (float) (Math.PI * (r + 1) / R);
                float t0 = (float) (2 * Math.PI * s / S);
                float t1 = (float) (2 * Math.PI * (s + 1) / S);

                Vector3f v0 = spt(center, radius, p0, t0);
                Vector3f v1 = spt(center, radius, p0, t1);
                Vector3f v2 = spt(center, radius, p1, t0);
                Vector3f v3 = spt(center, radius, p1, t1);

                tri(data, v0, v2, v1, col, axisId);
                tri(data, v1, v2, v3, col, axisId);
            }
        }
    }


    private Vector3f[] buildRing(Vector3f center, Vector3f axis, float radius, int segs)
    {
        Vector3f n = new Vector3f(axis).normalize();
        Vector3f t = Math.abs(n.x) < 0.9f ? new Vector3f(1, 0, 0) : new Vector3f(0, 1, 0);
        Vector3f u = new Vector3f(n).cross(t).normalize();
        Vector3f v = new Vector3f(n).cross(u).normalize();

        Vector3f[] ring = new Vector3f[segs];
        for (int i = 0; i < segs; i++)
        {
            float a = (float) (2.0 * Math.PI * i / segs);
            ring[i] = new Vector3f(center)
                    .add(new Vector3f(u).mul((float) Math.cos(a) * radius))
                    .add(new Vector3f(v).mul((float) Math.sin(a) * radius));
        }
        return ring;
    }


    private void fanCap(List<Float> data, Vector3f center, Vector3f[] ring,
                        int segs, float[] col, float axisId, boolean flip)
    {
        for (int i = 0; i < segs; i++)
        {
            int j = (i + 1) % segs;
            if (flip) tri(data, center, ring[j], ring[i], col, axisId);
            else tri(data, center, ring[i], ring[j], col, axisId);
        }
    }

    private Vector3f spt(Vector3f c, float r, float phi, float theta)
    {
        return new Vector3f(
                c.x + r * (float) (Math.sin(phi) * Math.cos(theta)),
                c.y + r * (float) (Math.cos(phi)),
                c.z + r * (float) (Math.sin(phi) * Math.sin(theta))
        );
    }

    private void tri(List<Float> data, Vector3f a, Vector3f b, Vector3f c,
                     float[] col, float axisId)
    {
        vert(data, a, col, axisId);
        vert(data, b, col, axisId);
        vert(data, c, col, axisId);
    }

    private void vert(List<Float> data, Vector3f p, float[] col, float axisId)
    {
        data.add(p.x);
        data.add(p.y);
        data.add(p.z);
        data.add(col[0]);
        data.add(col[1]);
        data.add(col[2]);
        data.add(axisId);
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
