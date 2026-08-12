package eleeter.unifystudiox.graphics.text.render.shaping;

public class TextLayout
{
    private final eleeter.elfontlib.shaping.TextLayout handle;

    public TextLayout(eleeter.elfontlib.shaping.TextLayout handle)
    {
        this.handle = handle;
    }

    public eleeter.elfontlib.shaping.TextLayout getHandle()
    {
        return this.handle;
    }

    public float getWidth()
    {
        return this.handle.getWidth();
    }

    public float getHeight()
    {
        return this.handle.getHeight();
    }
}
