package eleeter.unifystudiox.animation.interpolation;

import org.joml.Quaternionf;

import eleeter.unifystudiox.animation.api.Interpolatable;

public class SlerpQuaternionInterpolator implements Interpolatable<Quaternionf>
{
    @Override
    public Quaternionf interpolate(Quaternionf from, Quaternionf to, float t)
    {
        if (from == null)
        {
            throw new IllegalArgumentException(
                "SlerpQuaternionInterpolator.interpolate: 'from' must not be null.");
        }
        if (to == null)
        {
            throw new IllegalArgumentException(
                "SlerpQuaternionInterpolator.interpolate: 'to' must not be null.");
        }

        return new Quaternionf(from).slerp(to, t);
    }
}
