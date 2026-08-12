package eleeter.unifystudiox.particle.curve;

public class FloatCurve
{
    private static final int SINGLE_KEY_COUNT = 1;

    private final float[] times;
    private final float[] values;


    public FloatCurve(float[] times, float[] values)
    {
        if (times.length == 0 || times.length != values.length)
        {
            throw new IllegalArgumentException("FloatCurve requires at least one key and equal-length arrays.");
        }

        this.times = times;
        this.values = values;
    }


    public static FloatCurve constant(float constantValue)
    {
        return new FloatCurve(new float[]{0.0F}, new float[]{constantValue});
    }


    public static FloatCurve linear(float start, float end)
    {
        return new FloatCurve(new float[]{0.0F, 1.0F}, new float[]{start, end});
    }


    public float evaluate(float t)
    {
        if (this.times.length == SINGLE_KEY_COUNT)
        {
            return this.values[0];
        }

        if (t <= this.times[0])
        {
            return this.values[0];
        }

        int last = this.times.length - 1;

        if (t >= this.times[last])
        {
            return this.values[last];
        }

        int lo = binarySearch(t);
        int hi = lo + 1;

        float range = this.times[hi] - this.times[lo];
        float alpha = (t - this.times[lo]) / range;

        return this.values[lo] + alpha * (this.values[hi] - this.values[lo]);
    }


    private int binarySearch(float t)
    {
        int lo = 0;
        int hi = this.times.length - 2;

        while (lo < hi)
        {
            int mid = (lo + hi + 1) >>> 1;

            if (this.times[mid] <= t)
            {
                lo = mid;
            } else
            {
                hi = mid - 1;
            }
        }

        return lo;
    }
}
