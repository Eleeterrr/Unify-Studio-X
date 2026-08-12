package eleeter.unifystudiox.animation.interpolation;

import eleeter.unifystudiox.animation.api.Interpolatable;

public class StepInterpolator<T> implements Interpolatable<T>
{
    @Override
    public T interpolate(T from, T to, float t)
    {
        if (from == null)
        {
            throw new IllegalArgumentException(
                "StepInterpolator.interpolate: 'from' must not be null.");
        }
        if (to == null)
        {
            throw new IllegalArgumentException(
                "StepInterpolator.interpolate: 'to' must not be null.");
        }

        if (t >= 1.0f)
        {
            return to;
        }
        return from;
    }
}
