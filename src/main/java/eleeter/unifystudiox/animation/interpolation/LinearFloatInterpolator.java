package eleeter.unifystudiox.animation.interpolation;

import eleeter.unifystudiox.animation.api.Interpolatable;

public class LinearFloatInterpolator implements Interpolatable<Float>
{
    @Override
    public Float interpolate(Float from, Float to, float t)
    {
        if (from == null)
        {
            throw new IllegalArgumentException(
                "LinearFloatInterpolator.interpolate: 'from' must not be null.");
        }
        if (to == null)
        {
            throw new IllegalArgumentException(
                "LinearFloatInterpolator.interpolate: 'to' must not be null.");
        }

        return from + (to - from) * t;
    }
}
