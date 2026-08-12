package eleeter.unifystudiox.cubic;

import java.util.ArrayList;
import java.util.List;

public class CubicModel
{
    public final String sourceName;
    public final String formatVersion;
    public final int textureWidth;
    public final int textureHeight;
    public final List<CubeElement> elements = new ArrayList<>();
    public final List<CubicAnimation> animations = new ArrayList<>();
    public final List<String> warnings = new ArrayList<>();
    public final CubicGroup root;

    public CubicModel(String sourceName, String formatVersion, int textureWidth, int textureHeight, CubicGroup root)
    {
        this.sourceName = sourceName;
        this.formatVersion = formatVersion;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.root = root;
    }
}
