package eleeter.unifystudiox.graphics.gl;

import org.lwjgl.opengl.GL45C;

import eleeter.unifystudiox.graphics.TextureFormatBit;
import eleeter.unifystudiox.graphics.api.BlitMask;
import eleeter.unifystudiox.graphics.api.IFramebuffer;
import eleeter.unifystudiox.graphics.api.IGraphicsBackend;
import eleeter.unifystudiox.graphics.api.IShaderProgram;
import eleeter.unifystudiox.graphics.api.PrimitiveType;
import eleeter.unifystudiox.graphics.gfx.PipelineState;
import eleeter.unifystudiox.graphics.gfx.StateCommitter;

public class GLGraphicsBackend implements IGraphicsBackend
{

    private final StateCommitter stateCommitter;

    private boolean savedDepthTest;
    private boolean savedCullFace;
    private boolean savedBlend;
    private boolean savedScissorTest;
    private int savedProgram;
    private int savedVao;
    private int savedBlendSrcRgb;
    private int savedBlendDstRgb;
    private final int[] savedScissorBox = new int[4];

    private int savedPolygonMode;
    private boolean savedPolygonOffsetFill;
    private boolean savedPolygonOffsetLine;
    private float savedPolygonOffsetFactor;
    private float savedPolygonOffsetUnits;

    public GLGraphicsBackend()
    {
        this.stateCommitter = new StateCommitter();
    }

    @Override
    public void init()
    {
        this.stateCommitter.reset();
        GL45C.glDepthFunc(GL45C.GL_LEQUAL);
        GL45C.glEnable(GL45C.GL_MULTISAMPLE);
    }

    @Override
    public void clear(BlitMask mask)
    {
        int glMask = 0;
        switch (mask)
        {
            case COLOR_BUFFER -> glMask = GL45C.GL_COLOR_BUFFER_BIT;
            case DEPTH_BUFFER -> glMask = GL45C.GL_DEPTH_BUFFER_BIT;
            case STENCIL_BUFFER -> glMask = GL45C.GL_STENCIL_BUFFER_BIT;
            case COLOR_AND_DEPTH -> glMask = GL45C.GL_COLOR_BUFFER_BIT | GL45C.GL_DEPTH_BUFFER_BIT;
        }
        GL45C.glClear(glMask);
    }

    @Override
    public void clearDepth()
    {
        this.stateCommitter.apply(PipelineState.OPAQUE);
        GL45C.glClear(GL45C.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void clearFrame()
    {
        this.stateCommitter.apply(PipelineState.OPAQUE);
        GL45C.glClear(GL45C.GL_COLOR_BUFFER_BIT | GL45C.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void clearColor(float r, float g, float b, float a)
    {
        GL45C.glClearColor(r, g, b, a);
    }

    @Override
    public void setViewport(int width, int height)
    {
        GL45C.glViewport(0, 0, width, height);
    }

    @Override
    public void setScissor(int x, int y, int width, int height)
    {
        GL45C.glScissor(x, y, width, height);
    }

    @Override
    public void setScissorEnabled(boolean enabled)
    {
        if (enabled)
        {
            GL45C.glEnable(GL45C.GL_SCISSOR_TEST);
        } else
        {
            GL45C.glDisable(GL45C.GL_SCISSOR_TEST);
        }
    }

    @Override
    public void bindSampler(int unit, int samplerId)
    {
        GL45C.glBindSampler(unit, samplerId);
    }

    @Override
    public void applyState(PipelineState state)
    {
        this.stateCommitter.apply(state);
    }

    @Override
    public void resetState()
    {
        this.stateCommitter.reset();
    }

    private int getGLPrimitive(PrimitiveType type)
    {
        return switch (type)
        {
            case POINTS -> GL45C.GL_POINTS;
            case LINES -> GL45C.GL_LINES;
            case LINE_STRIP -> GL45C.GL_LINE_STRIP;
            case TRIANGLES -> GL45C.GL_TRIANGLES;
            case TRIANGLE_STRIP -> GL45C.GL_TRIANGLE_STRIP;
            case TRIANGLE_FAN -> GL45C.GL_TRIANGLE_FAN;
        };
    }

    @Override
    public void drawElements(PrimitiveType primitiveType, int count)
    {
        GL45C.glDrawElements(getGLPrimitive(primitiveType), count, GL45C.GL_UNSIGNED_INT, 0);
    }

    @Override
    public void drawArrays(PrimitiveType primitiveType, int first, int count)
    {
        GL45C.glDrawArrays(getGLPrimitive(primitiveType), first, count);
    }

    @Override
    public IFramebuffer createFramebuffer(int width, int height, int samples, TextureFormatBit[] colorFormats, TextureFormatBit depthFormat)
    {
        return null;
    }

    @Override
    public IShaderProgram createShaderProgram(String vertexPath, String fragmentPath, String geometryPath)
    {
        GLShaderProgram.Builder builder = GLShaderProgram.builder()
                .vertex(vertexPath)
                .fragment(fragmentPath);
        if (geometryPath != null)
        {
            builder.geometry(geometryPath);
        }
        return builder.build();
    }

    @Override
    public void saveState()
    {
        this.savedDepthTest = GL45C.glIsEnabled(GL45C.GL_DEPTH_TEST);
        this.savedCullFace = GL45C.glIsEnabled(GL45C.GL_CULL_FACE);
        this.savedBlend = GL45C.glIsEnabled(GL45C.GL_BLEND);
        this.savedScissorTest = GL45C.glIsEnabled(GL45C.GL_SCISSOR_TEST);
        this.savedProgram = GL45C.glGetInteger(GL45C.GL_CURRENT_PROGRAM);
        this.savedVao = GL45C.glGetInteger(GL45C.GL_VERTEX_ARRAY_BINDING);
        this.savedBlendSrcRgb = GL45C.glGetInteger(GL45C.GL_BLEND_SRC_RGB);
        this.savedBlendDstRgb = GL45C.glGetInteger(GL45C.GL_BLEND_DST_RGB);
        GL45C.glGetIntegerv(GL45C.GL_SCISSOR_BOX, this.savedScissorBox);

        this.savedPolygonMode = GL45C.glGetInteger(GL45C.GL_POLYGON_MODE);
        this.savedPolygonOffsetFill = GL45C.glIsEnabled(GL45C.GL_POLYGON_OFFSET_FILL);
        this.savedPolygonOffsetLine = GL45C.glIsEnabled(GL45C.GL_POLYGON_OFFSET_LINE);
        this.savedPolygonOffsetFactor = GL45C.glGetFloat(GL45C.GL_POLYGON_OFFSET_FACTOR);
        this.savedPolygonOffsetUnits = GL45C.glGetFloat(GL45C.GL_POLYGON_OFFSET_UNITS);
    }

    @Override
    public void restoreState()
    {
        GL45C.glBindVertexArray(0);

        if (this.savedDepthTest)
        {
            GL45C.glEnable(GL45C.GL_DEPTH_TEST);
        }
        else
        {
            GL45C.glDisable(GL45C.GL_DEPTH_TEST);
        }

        if (this.savedCullFace)
        {
            GL45C.glEnable(GL45C.GL_CULL_FACE);
        }
        else
        {
            GL45C.glDisable(GL45C.GL_CULL_FACE);
        }

        if (this.savedBlend)
        {
            GL45C.glEnable(GL45C.GL_BLEND);
        }
        else
        {
            GL45C.glDisable(GL45C.GL_BLEND);
        }

        GL45C.glBlendFunc(this.savedBlendSrcRgb, this.savedBlendDstRgb);
        GL45C.glUseProgram(this.savedProgram);
        GL45C.glBindVertexArray(this.savedVao);

        if (this.savedScissorTest)
        {
            GL45C.glEnable(GL45C.GL_SCISSOR_TEST);
            GL45C.glScissor(this.savedScissorBox[0], this.savedScissorBox[1], this.savedScissorBox[2], this.savedScissorBox[3]);
        }
        else
        {
            GL45C.glDisable(GL45C.GL_SCISSOR_TEST);
        }

        GL45C.glPolygonMode(GL45C.GL_FRONT_AND_BACK, this.savedPolygonMode);

        if (this.savedPolygonOffsetFill)
        {
            GL45C.glEnable(GL45C.GL_POLYGON_OFFSET_FILL);
        }
        else
        {
            GL45C.glDisable(GL45C.GL_POLYGON_OFFSET_FILL);
        }

        if (this.savedPolygonOffsetLine)
        {
            GL45C.glEnable(GL45C.GL_POLYGON_OFFSET_LINE);
        }
        else
        {
            GL45C.glDisable(GL45C.GL_POLYGON_OFFSET_LINE);
        }

        GL45C.glPolygonOffset(this.savedPolygonOffsetFactor, this.savedPolygonOffsetUnits);

        this.stateCommitter.invalidate();
    }

    @Override
    public void bindTextureId(int unit, int textureId)
    {
        GL45C.glBindTextureUnit(unit, textureId);
    }
}
