package eleeter.unifystudiox.graphics.text.render.font;

import eleeter.elfontlib.font.msdf.MsdfFont;

public class Font
{
    private final eleeter.elfontlib.font.Font handle;

    public Font(eleeter.elfontlib.font.Font handle)
    {
        this.handle = handle;
    }

    public eleeter.elfontlib.font.Font getHandle()
    {
        return this.handle;
    }

    public GlyphMetrics getGlyph(int id)
    {
        eleeter.elfontlib.font.GlyphMetrics rawMetrics = this.handle.getGlyph(id);
        return rawMetrics != null ? new GlyphMetrics(rawMetrics) : null;
    }

    public float getLineHeight()
    {
        return this.handle.getLineHeight();
    }

    public float getBaseline()
    {
        return this.handle.getBaseline();
    }

    public float getNativeSize()
    {
        return this.handle.getNativeSize();
    }

    public float getDistanceRange()
    {
        if (this.handle instanceof MsdfFont msdf)
        {
            return msdf.getDistanceRange();
        }
        return 2.0F;
    }
}
