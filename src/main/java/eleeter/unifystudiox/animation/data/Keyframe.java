package eleeter.unifystudiox.animation.data;

public class Keyframe<T>
{
    private final float time;
    private final T value;
    private final EasingType easingType;

    public Keyframe(float time, T value, EasingType easingType)
    {
        if (time < 0f)
        {
            throw new IllegalArgumentException(
                    "Keyframe: time must be >= 0. Got: " + time);
        }
        if (value == null)
        {
            throw new IllegalArgumentException(
                    "Keyframe: value must not be null.");
        }
        if (easingType == null)
        {
            throw new IllegalArgumentException(
                    "Keyframe: easingType must not be null.");
        }

        this.time = time;
        this.value = value;
        this.easingType = easingType;
    }

    public float getTime()
    {
        return this.time;
    }

    public T getValue()
    {
        return this.value;
    }

    public EasingType getEasingType()
    {
        return this.easingType;
    }

    @Override
    public String toString()
    {
        return "Keyframe{time=" + this.time + ", easingType=" + this.easingType + '}';
    }
}
