package eleeter.unifystudiox.animation.interpolation;

import eleeter.unifystudiox.animation.api.Interpolatable;

public class BooleanInterpolator implements Interpolatable<Boolean>
{
    @Override
    public Boolean interpolate(Boolean from, Boolean to, float t)
    {
        if (from == null)
        {
            throw new IllegalArgumentException(
                "BooleanInterpolator.interpolate: 'from' must not be null.");
        }
        if (to == null)
        {
            throw new IllegalArgumentException(
                "BooleanInterpolator.interpolate: 'to' must not be null.");
        }

        if (t >= 1.0f)
        {
            return to;
        }
        return from;
    }
}
