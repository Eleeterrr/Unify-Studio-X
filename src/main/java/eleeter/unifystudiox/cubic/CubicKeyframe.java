package eleeter.unifystudiox.cubic;

import org.joml.Vector3f;

public class CubicKeyframe
{
    public final String channel;
    public final float time;
    public final Vector3f value;
    public final String interpolation;

    public CubicKeyframe(String channel, float time, Vector3f value, String interpolation)
    {
        this.channel = channel;
        this.time = time;
        this.value = new Vector3f(value);
        this.interpolation = interpolation;
    }
}
