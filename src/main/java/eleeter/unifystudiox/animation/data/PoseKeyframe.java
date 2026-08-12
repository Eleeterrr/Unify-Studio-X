package eleeter.unifystudiox.animation.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PoseKeyframe
{
    private final float time;
    private final EasingType easingType;
    private final Map<String, Transform> boneTransforms;

    public PoseKeyframe(float time, EasingType easingType, Map<String, Transform> boneTransforms)
    {
        if (time < 0f)
        {
            throw new IllegalArgumentException(
                    "PoseKeyframe: time must be >= 0. Got: " + time);
        }
        if (easingType == null)
        {
            throw new IllegalArgumentException(
                    "PoseKeyframe: easingType must not be null.");
        }
        if (boneTransforms == null)
        {
            throw new IllegalArgumentException(
                    "PoseKeyframe: boneTransforms must not be null.");
        }

        this.time = time;
        this.easingType = easingType;
        this.boneTransforms = Collections.unmodifiableMap(new HashMap<>(boneTransforms));
    }

    public float getTime()
    {
        return this.time;
    }

    public EasingType getEasingType()
    {
        return this.easingType;
    }

    public Map<String, Transform> getBoneTransforms()
    {
        return this.boneTransforms;
    }

    @Override
    public String toString()
    {
        return "PoseKeyframe{"
                + "time=" + this.time
                + ", easingType=" + this.easingType
                + ", boneCount=" + this.boneTransforms.size()
                + '}';
    }
}
