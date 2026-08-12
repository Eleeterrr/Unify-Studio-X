package eleeter.unifystudiox.animation.interpolation;

import org.joml.Vector3f;

import eleeter.unifystudiox.animation.api.Interpolatable;

public class LinearVector3Interpolator implements Interpolatable<Vector3f>
{
    @Override
    public Vector3f interpolate(Vector3f from, Vector3f to, float t)
    {
        if (from == null)
        {
            throw new IllegalArgumentException(
                "LinearVector3Interpolator.interpolate: 'from' must not be null.");
        }
        if (to == null)
        {
            throw new IllegalArgumentException(
                "LinearVector3Interpolator.interpolate: 'to' must not be null.");
        }

        return new Vector3f(from.x + (to.x - from.x) * t, from.y + (to.y - from.y) * t, from.z + (to.z - from.z) * t
        );
    }
}
