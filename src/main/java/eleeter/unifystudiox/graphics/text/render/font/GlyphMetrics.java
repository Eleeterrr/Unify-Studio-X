package eleeter.unifystudiox.graphics.text.render.font;

public class GlyphMetrics
{
    private final eleeter.elfontlib.font.GlyphMetrics handle;

    public GlyphMetrics(eleeter.elfontlib.font.GlyphMetrics handle)
    {
        this.handle = handle;
    }

    public eleeter.elfontlib.font.GlyphMetrics getHandle()
    {
        return this.handle;
    }

    public int getId()
    {
        return this.handle.getId();
    }

    public float getWidth()
    {
        return this.handle.getWidth();
    }

    public float getHeight()
    {
        return this.handle.getHeight();
    }

    public float getXOffset()
    {
        return this.handle.getXOffset();
    }

    public float getYOffset()
    {
        return this.handle.getYOffset();
    }

    public float getXAdvance()
    {
        return this.handle.getXAdvance();
    }

    public float getU0()
    {
        return this.handle.getU0();
    }

    public float getV0()
    {
        return this.handle.getV0();
    }

    public float getU1()
    {
        return this.handle.getU1();
    }

    public float getV1()
    {
        return this.handle.getV1();
    }
}
