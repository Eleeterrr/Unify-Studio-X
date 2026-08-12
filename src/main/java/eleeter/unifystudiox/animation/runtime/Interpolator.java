package eleeter.unifystudiox.animation.runtime;

import eleeter.unifystudiox.animation.api.Interpolatable;
import eleeter.unifystudiox.animation.data.EasingType;
import eleeter.unifystudiox.animation.data.Keyframe;
import java.util.List;

public class Interpolator
{
    private Interpolator()
    {
        throw new UnsupportedOperationException(
                "Interpolator is a static utility class and must not be instantiated.");
    }

    public static <T> T evaluate(List<Keyframe<T>> keyframes, float currentTime, Interpolatable<T> strategy)
    {
        if (keyframes == null || keyframes.isEmpty())
        {
            throw new IllegalArgumentException(
                    "Interpolator.evaluate: keyframes list must not be null or empty.");
        }
        if (strategy == null)
        {
            throw new IllegalArgumentException(
                    "Interpolator.evaluate: strategy must not be null.");
        }

        if (currentTime <= keyframes.get(0).getTime())
        {
            return keyframes.get(0).getValue();
        }

        if (currentTime >= keyframes.get(keyframes.size() - 1).getTime())
        {
            return keyframes.get(keyframes.size() - 1).getValue();
        }

        if (keyframes.size() == 1)
        {
            return keyframes.get(0).getValue();
        }

        Keyframe<T> fromKeyframe = keyframes.get(0);
        Keyframe<T> toKeyframe = keyframes.get(1);

        for (int index = 0; index < keyframes.size() - 1; index++)
        {
            if (currentTime >= keyframes.get(index).getTime()
                    && currentTime < keyframes.get(index + 1).getTime())
            {
                fromKeyframe = keyframes.get(index);
                toKeyframe = keyframes.get(index + 1);
                break;
            }
        }

        float segmentDuration = toKeyframe.getTime() - fromKeyframe.getTime();

        if (segmentDuration <= 0f)
        {
            return toKeyframe.getValue();
        }

        float rawT = (currentTime - fromKeyframe.getTime()) / segmentDuration;
        float easedT = applyEasing(rawT, fromKeyframe.getEasingType());

        return strategy.interpolate(fromKeyframe.getValue(), toKeyframe.getValue(), easedT);
    }

    private static float applyEasing(float t, EasingType easingType)
    {
        switch (easingType)
        {
            case LINEAR:
                return t;

            case STEP:
                return t;

            case EASE_IN:
                return t * t;

            case EASE_OUT:
                return t * (2f - t);

            case BEZIER:
                return t;

            default:
                return t;
        }
    }
}
