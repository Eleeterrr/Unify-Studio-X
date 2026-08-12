package eleeter.unifystudiox.graphics.text.render.shaping;

import eleeter.unifystudiox.graphics.text.render.font.Font;
import eleeter.elfontlib.shaping.SimpleTextShaper;

public class TextShaper
{
    private final eleeter.elfontlib.shaping.TextShaper handle;

    public TextShaper()
    {
        this.handle = new SimpleTextShaper();
    }

    public TextLayout shape(String text, Font font, float fontSize)
    {
        eleeter.elfontlib.shaping.TextLayout rawLayout = this.handle.shape(text, font.getHandle(), fontSize);
        return rawLayout != null ? new TextLayout(rawLayout) : null;
    }
}
