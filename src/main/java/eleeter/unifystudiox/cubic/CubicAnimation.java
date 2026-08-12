package eleeter.unifystudiox.cubic;

import java.util.LinkedHashMap;
import java.util.Map;

public class CubicAnimation
{
    public final String name;
    public final float length;
    public final String loopMode;
    public final Map<String, CubicBoneAnimation> bones = new LinkedHashMap<>();

    public CubicAnimation(String name, float length, String loopMode)
    {
        this.name = name;
        this.length = length;
        this.loopMode = loopMode;
    }
}
