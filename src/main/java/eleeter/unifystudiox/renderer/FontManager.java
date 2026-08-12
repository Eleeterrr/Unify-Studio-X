package eleeter.unifystudiox.renderer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.graphics.text.render.font.Font;
import eleeter.unifystudiox.renderer.shading.TextureSampling;
import eleeter.elfontlib.font.msdf.MsdfFont;
import eleeter.elfontlib.font.msdf.MsdfFontData;
import eleeter.elfontlib.font.msdf.MsdfJsonParser;

public class FontManager
{
    private static final Map<String, MsdfFont> fonts = new HashMap<>();
    private static final Map<String, TextureGL> atlases = new HashMap<>();

    private FontManager()
    {
    }


    public static void load(String key, String jsonPath, String atlasPath)
    {
        if (fonts.containsKey(key))
        {
            return; // already cached — no-op
        }

        try
        {
            MsdfFontData data = MsdfJsonParser.loadFontData(jsonPath);
            MsdfFont font = new MsdfFont(data);
            TextureGL atlas = new TextureGL(atlasPath, TextureSampling.LINEAR);

            fonts.put(key, font);
            atlases.put(key, atlas);
        } catch (IOException e)
        {
            throw new RuntimeException("FontManager: failed to load font '" + key + "' from " + jsonPath, e);
        }
    }

    public static Font getFont(String key)
    {
        MsdfFont raw = fonts.get(key);
        return raw != null ? new Font(raw) : null;
    }

    public static TextureGL getAtlas(String key)
    {
        return atlases.get(key);
    }


    public static boolean isLoaded(String key)
    {
        return fonts.containsKey(key);
    }


    public static String keyForFont(Font font)
    {
        if (font == null)
        {
            return null;
        }
        for (Map.Entry<String, MsdfFont> entry : fonts.entrySet())
        {
            if (entry.getValue() == font.getHandle())
            {
                return entry.getKey();
            }
        }
        return null;
    }

    public static void cleanup()
    {
        atlases.values().forEach(TextureGL::cleanup);
        atlases.clear();
        fonts.clear();
    }
}
