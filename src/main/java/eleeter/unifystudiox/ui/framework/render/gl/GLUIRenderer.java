package eleeter.unifystudiox.ui.framework.render.gl;

import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.graphics.TransformStack;
import eleeter.unifystudiox.graphics.Vao;
import eleeter.unifystudiox.graphics.VertexBuffer;
import eleeter.unifystudiox.graphics.api.IGraphicsBackend;
import eleeter.unifystudiox.graphics.api.IShaderProgram;
import eleeter.unifystudiox.graphics.api.PrimitiveType;
import eleeter.unifystudiox.graphics.buffer.GpuBufferUsage;
import eleeter.unifystudiox.graphics.gfx.PipelineState;
import eleeter.unifystudiox.graphics.gfx.PipelineState.BlendMode;
import eleeter.unifystudiox.graphics.gl.GLGraphicsBackend;
import eleeter.unifystudiox.graphics.layout.AttributeType;
import eleeter.unifystudiox.graphics.layout.BufferLayout;
import eleeter.unifystudiox.graphics.text.render.MeshData;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import java.awt.geom.Rectangle2D;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Stack;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

public class GLUIRenderer implements UIRenderer
{
    private final IGraphicsBackend backend;

    private IShaderProgram shader;
    private IShaderProgram textShader;
    private IShaderProgram roundedShader;
    private IShaderProgram batchShader;
    private IShaderProgram shapeShader;

    private final TransformStack matrixStack =
            new TransformStack();

    private Vao quadVao;
    private VertexBuffer quadVbo;
    private Vao textVao;
    private VertexBuffer textVbo;
    private VertexBuffer textEbo;
    private FloatBuffer matrixBuffer;

    private final Matrix4f projection = new Matrix4f();

    /* Physical-pixel scale factors set once per frame in beginFrame() */
    private float clipScaleX = 1.0F;
    private float clipScaleY = 1.0F;
    private float clipPhysicalH = 0.0F;

    private final Stack<Rectangle2D.Float> clipStack = new Stack<>();

    public GLUIRenderer()
    {
        this.backend = new GLGraphicsBackend();
    }

    public GLUIRenderer(IGraphicsBackend backend)
    {
        this.backend = backend;
    }

    @Override
    public void init()
    {
        this.shapeShader = this.backend.createShaderProgram("/shaders/ui_shape.vert", "/shaders/ui_shape.frag", null);
        this.shader = this.backend.createShaderProgram("/shaders/ui.vert", "/shaders/ui.frag", null);
        this.textShader = this.backend.createShaderProgram("/shaders/ui_text.vert", "/shaders/ui_text.frag", null);
        this.roundedShader = this.backend.createShaderProgram("/shaders/ui_rounded.vert", "/shaders/ui_rounded.frag", null);
        this.batchShader = this.backend.createShaderProgram("/shaders/ui_batch.vert", "/shaders/ui_batch.frag", null);

        float[] quad = {0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1};
        this.quadVbo = new VertexBuffer(quad, GpuBufferUsage.STATIC);

        BufferLayout layout = BufferLayout.builder().add(0, 2, AttributeType.FLOAT).build();
        this.quadVao = Vao.builder().bindVertexBuffer(this.quadVbo, layout).build();

        this.textVbo = new VertexBuffer(GpuBufferUsage.DYNAMIC);
        this.textEbo = new VertexBuffer(GpuBufferUsage.DYNAMIC);
        BufferLayout textLayout = BufferLayout.builder().add(0, 3, AttributeType.FLOAT).add(1, 2, AttributeType.FLOAT).build();
        this.textVao = Vao.builder().bindVertexBuffer(this.textVbo, textLayout).elementBuffer(this.textEbo).build();

        this.matrixBuffer = MemoryUtil.memAllocFloat(16);
    }


    @Override
    public void beginFrame(float logicalW, float logicalH, float physicalW, float physicalH)
    {
        this.backend.saveState();

        PipelineState uiState = new PipelineState().setDepthTest(false).setDepthWrite(false).setCullFace(false).setBlendMode(BlendMode.ALPHA);
        this.backend.applyState(uiState);
        this.backend.setScissorEnabled(false);

        this.clipScaleX = physicalW / logicalW;
        this.clipScaleY = physicalH / logicalH;
        this.clipPhysicalH = physicalH;
        this.clipStack.clear();

        this.projection.identity().ortho2D(0.0F, logicalW, logicalH, 0.0F);
        FloatBuffer projBuf = this.projection.get(this.matrixBuffer);

        this.shapeShader.bind();
        this.shapeShader.setUniformMatrix4f("uProjection", projBuf);

        this.shader.bind();
        this.shader.setUniformMatrix4f("uProjection", projBuf);

        this.textShader.bind();
        this.textShader.setUniformMatrix4f("uProjection", projBuf);

        this.roundedShader.bind();
        this.roundedShader.setUniformMatrix4f("uProjection", projBuf);

        this.batchShader.bind();
        this.batchShader.setUniformMatrix4f("uProjection", projBuf);

        this.shader.bind();
    }

    @Override
    public void drawRect(float x, float y, float w, float h, float r, float g, float b, float a)
    {
        this.shader.bind();
        this.shader.setUniform("uHasTexture", 0);
        this.shader.setUniform("uRect", x, y, w, h);
        this.shader.setUniform("uColor", r, g, b, a);
        this.quadVao.bind();
        this.backend.drawArrays(PrimitiveType.TRIANGLES, 0, 6);
    }

    @Override
    public void drawRoundedRect(float x, float y, float w, float h, float r, float g, float b, float a, float radius)
    {
        this.roundedShader.bind();
        this.roundedShader.setUniform("uRect", x, y, w, h);
        this.roundedShader.setUniform("uColor", r, g, b, a);
        this.roundedShader.setUniform("uSize", w, h);
        this.roundedShader.setUniform("uRadius", radius);
        this.quadVao.bind();
        this.backend.drawArrays(PrimitiveType.TRIANGLES, 0, 6);
        this.shader.bind();
    }

    @Override
    public void drawTexture(float x, float y, float w, float h, TextureGL texture, float r, float g, float b, float a)
    {
        this.shader.bind();
        texture.bind(0);
        this.shader.setUniform("uHasTexture", 1);
        this.shader.setUniform("uSampler", 0);
        this.shader.setUniform("uRect", x, y, w, h);
        this.shader.setUniform("uColor", r, g, b, a);
        this.quadVao.bind();
        this.backend.drawArrays(PrimitiveType.TRIANGLES, 0, 6);
    }

    @Override
    public void drawFramebufferTexture(float x, float y, float w, float h, int glTextureHandle, float r, float g, float b, float a)
    {
        this.shader.bind();
        this.backend.bindTextureId(0, glTextureHandle);
        this.shader.setUniform("uHasTexture", 1);
        this.shader.setUniform("uSampler", 0);
        this.shader.setUniform("uRect", x, y, w, h);
        this.shader.setUniform("uColor", r, g, b, a);
        this.quadVao.bind();
        this.backend.drawArrays(PrimitiveType.TRIANGLES, 0, 6);
    }

    @Override
    public void drawText(MeshData data, TextureGL atlas, float x, float y, float scale, float r, float g, float b, float a)
    {
        if (data == null || data.indices.length == 0) return;

        this.textVbo.upload(data.vertices);
        this.textEbo.upload(data.indices);

        this.textShader.bind();
        atlas.bind(0);
        this.textShader.setUniform("uMsdfAtlas", 0);
        this.textShader.setUniform("uOffset", x, y);
        this.textShader.setUniform("uScale", scale);
        this.textShader.setUniform("uTextColor", r, g, b, a);
        this.textVao.bind();
        this.backend.drawElements(PrimitiveType.TRIANGLES, data.indices.length);
        this.shader.bind();
    }

    @Override
    public void drawGeometry(Vao vao, int indexCount, TextureGL texture)
    {
        this.batchShader.bind();
        if (texture != null)
        {
            texture.bind(0);
            this.batchShader.setUniform("uHasTexture", 1);
            this.batchShader.setUniform("uSampler", 0);
        } else
        {
            this.batchShader.setUniform("uHasTexture", 0);
        }
        vao.bind();
        this.backend.drawElements(PrimitiveType.TRIANGLES, indexCount);
        this.shader.bind();
    }

    @Override
    public void drawShapeGeometry(List<Float> vertices, List<Integer> indices, float r, float g, float b, float a)
    {
        if (vertices == null || vertices.isEmpty() ||
                indices == null || indices.isEmpty()) return;

        final int vCount = vertices.size();
        float[] vArr = new float[vCount];

        for (int i = 0; i < vCount; i += 2)
        {
            vArr[i] = snapPhysical(vertices.get(i), this.clipScaleX);
            vArr[i + 1] = snapPhysical(vertices.get(i + 1), this.clipScaleY);
        }

        int[] iArr = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) iArr[i] = indices.get(i);

        VertexBuffer vbo = new VertexBuffer(vArr, GpuBufferUsage.DYNAMIC);
        VertexBuffer ebo = new VertexBuffer(iArr, GpuBufferUsage.DYNAMIC);

        BufferLayout layout = BufferLayout.builder()
                .add(0, 2, AttributeType.FLOAT)
                .build();

        Vao vao = Vao.builder().bindVertexBuffer(vbo, layout).elementBuffer(ebo).build();

        this.shapeShader.bind();
        this.shapeShader.setUniformMatrix4f("uProjection", this.projection.get(this.matrixBuffer));
        this.shapeShader.setUniform("uColor", r, g, b, a);

        vao.bind();
        this.backend.drawElements(PrimitiveType.TRIANGLES, iArr.length);
        vao.unbind();

        vao.destroy();
        ebo.destroy();
        vbo.destroy();

        this.shader.bind();
    }


    private static float snapPhysical(float logicalCoord, float scale)
    {
        float physical = logicalCoord * scale;
        float snapped = (float) Math.floor(physical) + 0.5F;
        return snapped / scale;
    }


    @Override
    public TransformStack getMatrixStack()
    {
        return this.matrixStack;
    }

    @Override
    public void pushClip(float x, float y, float w, float h)
    {
        Rectangle2D.Float region = new Rectangle2D.Float(x, y, w, h);
        if (!this.clipStack.isEmpty())
        {
            Rectangle2D.intersect(this.clipStack.peek(), region, region);
        }
        this.clipStack.push(region);
        applyActiveClip();
    }

    @Override
    public void popClip()
    {
        if (!this.clipStack.isEmpty()) this.clipStack.pop();
        applyActiveClip();
    }

    private void applyActiveClip()
    {
        if (this.clipStack.isEmpty())
        {
            this.backend.setScissorEnabled(false);
        } else
        {
            this.backend.setScissorEnabled(true);
            Rectangle2D.Float c = this.clipStack.peek();
            int sx = (int) (c.x * this.clipScaleX);
            int sh = (int) (c.height * this.clipScaleY);
            int sy = (int) (this.clipPhysicalH - c.y * this.clipScaleY - sh);
            int sw = (int) (c.width * this.clipScaleX);

            if (sx < 0)
            {
                sw += sx;
                sx = 0;
            }
            if (sy < 0)
            {
                sh += sy;
                sy = 0;
            }
            if (sw < 0) sw = 0;
            if (sh < 0) sh = 0;

            this.backend.setScissor(sx, sy, sw, sh);
        }
    }


    @Override
    public void endFrame()
    {
        this.quadVao.unbind();
        this.shader.unbind();
        this.backend.restoreState();
    }

    @Override
    public void cleanup()
    {
        if (this.shapeShader != null) this.shapeShader.cleanup();
        if (this.shader != null) this.shader.cleanup();
        if (this.textShader != null) this.textShader.cleanup();
        if (this.roundedShader != null) this.roundedShader.cleanup();
        if (this.batchShader != null) this.batchShader.cleanup();
        if (this.quadVao != null)
        {
            this.quadVao.destroy();
            this.quadVao = null;
        }
        if (this.quadVbo != null)
        {
            this.quadVbo.destroy();
            this.quadVbo = null;
        }
        if (this.textVao != null)
        {
            this.textVao.destroy();
            this.textVao = null;
        }
        if (this.textVbo != null)
        {
            this.textVbo.destroy();
            this.textVbo = null;
        }
        if (this.textEbo != null)
        {
            this.textEbo.destroy();
            this.textEbo = null;
        }
        if (this.matrixBuffer != null)
        {
            MemoryUtil.memFree(this.matrixBuffer);
            this.matrixBuffer = null;
        }
    }
}
