package eleeter.unifystudiox.graphics.gfx;

import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_POLYGON_OFFSET_FILL;
import static org.lwjgl.opengl.GL11.GL_POLYGON_OFFSET_LINE;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL11.glPolygonOffset;

public class StateCommitter
{
    private boolean isDepthTest;
    private boolean isDepthWrite;
    private boolean isCullFace;
    private PipelineState.BlendMode blendMode;
    private PipelineState.PolygonMode polygonMode;
    private PipelineState.CullFaceMode cullFaceMode;
    private boolean hasPolygonOffset;
    private float polygonOffsetFactor;
    private float polygonOffsetUnits;
    private boolean isInitialized;

    public StateCommitter()
    {
        this.isInitialized = false;
    }


    public void apply(PipelineState state)
    {
        if (!this.isInitialized)
        {
            forceApply(state);
            this.isInitialized = true;
            return;
        }

        if (this.isDepthTest != state.isDepthTest())
        {
            this.isDepthTest = state.isDepthTest();

            if (this.isDepthTest)
            {
                glEnable(GL_DEPTH_TEST);
            } else
            {
                glDisable(GL_DEPTH_TEST);
            }
        }

        if (this.isDepthWrite != state.isDepthWrite())
        {
            this.isDepthWrite = state.isDepthWrite();
            glDepthMask(this.isDepthWrite);
        }

        if (this.isCullFace != state.isCullFace())
        {
            this.isCullFace = state.isCullFace();

            if (this.isCullFace)
            {
                glEnable(GL_CULL_FACE);
            } else
            {
                glDisable(GL_CULL_FACE);
            }
        }

        if (this.isCullFace)
        {
            if (this.cullFaceMode != state.getCullFaceMode())
            {
                this.cullFaceMode = state.getCullFaceMode();
                if (this.cullFaceMode == PipelineState.CullFaceMode.FRONT)
                {
                    glCullFace(GL_FRONT);
                } else
                {
                    glCullFace(GL_BACK);
                }
            }
        }

        if (this.blendMode != state.getBlendMode())
        {
            this.blendMode = state.getBlendMode();
            applyBlendMode(this.blendMode);
        }

        if (this.polygonMode != state.getPolygonMode())
        {
            this.polygonMode = state.getPolygonMode();

            if (this.polygonMode == PipelineState.PolygonMode.LINE)
            {
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            } else
            {
                glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            }
        }

        if (this.hasPolygonOffset != state.hasPolygonOffset())
        {
            this.hasPolygonOffset = state.hasPolygonOffset();

            if (this.hasPolygonOffset)
            {
                glEnable(GL_POLYGON_OFFSET_FILL);
                glEnable(GL_POLYGON_OFFSET_LINE);
            } else
            {
                glDisable(GL_POLYGON_OFFSET_FILL);
                glDisable(GL_POLYGON_OFFSET_LINE);
            }
        }

        if (this.hasPolygonOffset)
        {
            if (this.polygonOffsetFactor != state.getPolygonOffsetFactor() || this.polygonOffsetUnits != state.getPolygonOffsetUnits())
            {
                this.polygonOffsetFactor = state.getPolygonOffsetFactor();
                this.polygonOffsetUnits = state.getPolygonOffsetUnits();
                glPolygonOffset(this.polygonOffsetFactor, this.polygonOffsetUnits);
            }
        }
    }

    private void forceApply(PipelineState state)
    {
        this.isDepthTest = state.isDepthTest();

        if (this.isDepthTest)
        {
            glEnable(GL_DEPTH_TEST);
        } else
        {
            glDisable(GL_DEPTH_TEST);
        }

        this.isDepthWrite = state.isDepthWrite();
        glDepthMask(this.isDepthWrite);

        this.isCullFace = state.isCullFace();

        if (this.isCullFace)
        {
            glEnable(GL_CULL_FACE);
            this.cullFaceMode = state.getCullFaceMode();
            if (this.cullFaceMode == PipelineState.CullFaceMode.FRONT)
            {
                glCullFace(GL_FRONT);
            } else
            {
                glCullFace(GL_BACK);
            }
        } else
        {
            glDisable(GL_CULL_FACE);
        }

        this.blendMode = state.getBlendMode();
        applyBlendMode(this.blendMode);

        this.polygonMode = state.getPolygonMode();

        if (this.polygonMode == PipelineState.PolygonMode.LINE)
        {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        } else
        {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }

        this.hasPolygonOffset = state.hasPolygonOffset();

        if (this.hasPolygonOffset)
        {
            glEnable(GL_POLYGON_OFFSET_FILL);
            glEnable(GL_POLYGON_OFFSET_LINE);
        } else
        {
            glDisable(GL_POLYGON_OFFSET_FILL);
            glDisable(GL_POLYGON_OFFSET_LINE);
        }

        this.polygonOffsetFactor = state.getPolygonOffsetFactor();
        this.polygonOffsetUnits = state.getPolygonOffsetUnits();

        if (this.hasPolygonOffset)
        {
            glPolygonOffset(this.polygonOffsetFactor, this.polygonOffsetUnits);
        }
    }

    private void applyBlendMode(PipelineState.BlendMode mode)
    {
        if (mode == PipelineState.BlendMode.NONE)
        {
            glDisable(GL_BLEND);
        } else
        {
            glEnable(GL_BLEND);

            if (mode == PipelineState.BlendMode.ALPHA)
            {
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            } else if (mode == PipelineState.BlendMode.ADDITIVE)
            {
                glBlendFunc(GL_SRC_ALPHA, GL_ONE);
            } else if (mode == PipelineState.BlendMode.PREMULTIPLIED)
            {
                glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
            }
        }
    }


    public void reset()
    {
        forceApply(new PipelineState());
        this.isInitialized = false;
    }

    public void invalidate()
    {
        this.isInitialized = false;
    }
}
