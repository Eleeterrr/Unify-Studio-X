package eleeter.unifystudiox.graphics.gfx;

public class PipelineState
{
    public enum BlendMode
    {
        NONE,
        ALPHA,
        ADDITIVE,
        PREMULTIPLIED
    }

    public enum PolygonMode
    {
        FILL,
        LINE
    }

    public enum CullFaceMode
    {
        BACK,
        FRONT
    }

    public static final PipelineState OPAQUE = new PipelineState();

    public static final PipelineState OPAQUE_NO_CULL = new PipelineState()
            .setCullFace(false);

    public static final PipelineState SHADOW = new PipelineState()
            .setCullFace(true)
            .setCullFaceMode(CullFaceMode.FRONT)
            .setDepthWrite(true)
            .setDepthTest(true);

    public static final PipelineState SKY = new PipelineState()
            .setDepthWrite(false)
            .setCullFace(false);

    public static final PipelineState TRANSPARENT = new PipelineState()
            .setBlendMode(BlendMode.ALPHA)
            .setDepthWrite(false)
            .setCullFace(false);

    public static final PipelineState ADDITIVE = new PipelineState()
            .setBlendMode(BlendMode.ADDITIVE)
            .setDepthWrite(false);

    public static final PipelineState PREMULTIPLIED = new PipelineState()
            .setBlendMode(BlendMode.PREMULTIPLIED)
            .setDepthWrite(false)
            .setCullFace(false);

    public static final PipelineState WIREFRAME_OVERLAY = new PipelineState()
            .setPolygonMode(PolygonMode.LINE)
            .setPolygonOffset(true)
            .setPolygonOffsetFactor(-1.0F)
            .setPolygonOffsetUnits(-1.0F)
            .setCullFace(false);

    public static final PipelineState GIZMO = new PipelineState()
            .setDepthTest(false)
            .setCullFace(false);

    private boolean isDepthTest;
    private boolean isDepthWrite;
    private boolean isCullFace;
    private CullFaceMode cullFaceMode;
    private BlendMode blendMode;
    private PolygonMode polygonMode;
    private boolean hasPolygonOffset;
    private float polygonOffsetFactor;
    private float polygonOffsetUnits;

    public PipelineState()
    {
        this.isDepthTest = true;
        this.isDepthWrite = true;
        this.isCullFace = true;
        this.cullFaceMode = CullFaceMode.BACK;
        this.blendMode = BlendMode.NONE;
        this.polygonMode = PolygonMode.FILL;
        this.hasPolygonOffset = false;
        this.polygonOffsetFactor = 0.0F;
        this.polygonOffsetUnits = 0.0F;
    }

    public boolean isDepthTest()
    {
        return this.isDepthTest;
    }

    public PipelineState setDepthTest(boolean isDepthTest)
    {
        this.isDepthTest = isDepthTest;
        return this;
    }

    public boolean isDepthWrite()
    {
        return this.isDepthWrite;
    }

    public PipelineState setDepthWrite(boolean isDepthWrite)
    {
        this.isDepthWrite = isDepthWrite;
        return this;
    }

    public boolean isCullFace()
    {
        return this.isCullFace;
    }

    public PipelineState setCullFace(boolean isCullFace)
    {
        this.isCullFace = isCullFace;
        return this;
    }

    public CullFaceMode getCullFaceMode()
    {
        return this.cullFaceMode;
    }

    public PipelineState setCullFaceMode(CullFaceMode cullFaceMode)
    {
        this.cullFaceMode = cullFaceMode;
        return this;
    }

    public BlendMode getBlendMode()
    {
        return this.blendMode;
    }

    public PipelineState setBlendMode(BlendMode blendMode)
    {
        this.blendMode = blendMode;
        return this;
    }

    public PolygonMode getPolygonMode()
    {
        return this.polygonMode;
    }

    public PipelineState setPolygonMode(PolygonMode polygonMode)
    {
        this.polygonMode = polygonMode;
        return this;
    }

    public boolean hasPolygonOffset()
    {
        return this.hasPolygonOffset;
    }

    public PipelineState setPolygonOffset(boolean hasPolygonOffset)
    {
        this.hasPolygonOffset = hasPolygonOffset;
        return this;
    }

    public float getPolygonOffsetFactor()
    {
        return this.polygonOffsetFactor;
    }

    public PipelineState setPolygonOffsetFactor(float polygonOffsetFactor)
    {
        this.polygonOffsetFactor = polygonOffsetFactor;
        return this;
    }

    public float getPolygonOffsetUnits()
    {
        return this.polygonOffsetUnits;
    }

    public PipelineState setPolygonOffsetUnits(float polygonOffsetUnits)
    {
        this.polygonOffsetUnits = polygonOffsetUnits;
        return this;
    }
}
