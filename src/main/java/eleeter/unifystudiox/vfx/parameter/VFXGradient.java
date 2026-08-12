package eleeter.unifystudiox.vfx.parameter;

import java.util.ArrayList;
import java.util.List;

public class VFXGradient
{

    public final List<float[]> stops = new ArrayList<>();


    public float[] evaluate(float t)
    {
        if (this.stops.isEmpty())
        {
            return new float[]{1.0F, 1.0F, 1.0F, 1.0F};
        }

        int size = this.stops.size();

        if (t <= this.stops.get(0)[0])
        {
            float[] s = this.stops.get(0);
            return new float[]{s[1], s[2], s[3], s[4]};
        }

        if (t >= this.stops.get(size - 1)[0])
        {
            float[] s = this.stops.get(size - 1);
            return new float[]{s[1], s[2], s[3], s[4]};
        }

        int lo = 0;
        int hi = size - 1;
        while (lo + 1 < hi)
        {
            int mid = (lo + hi) / 2;
            if (this.stops.get(mid)[0] <= t)
            {
                lo = mid;
            } else
            {
                hi = mid;
            }
        }

        float[] a = this.stops.get(lo);
        float[] b = this.stops.get(hi);
        float span = b[0] - a[0];

        if (span <= 0.0F)
        {
            return new float[]{a[1], a[2], a[3], a[4]};
        }

        float localT = (t - a[0]) / span;
        return new float[]
                {
                        lerp(a[1], b[1], localT),
                        lerp(a[2], b[2], localT),
                        lerp(a[3], b[3], localT),
                        lerp(a[4], b[4], localT)
                };
    }

    private static float lerp(float a, float b, float t)
    {
        return a + t * (b - a);
    }


    public static VFXGradient white()
    {
        VFXGradient g = new VFXGradient();
        g.stops.add(new float[]{0.0F, 1.0F, 1.0F, 1.0F, 1.0F});
        g.stops.add(new float[]{1.0F, 1.0F, 1.0F, 1.0F, 0.0F});
        return g;
    }


    public static VFXGradient fire()
    {
        VFXGradient g = new VFXGradient();
        g.stops.add(new float[]{0.0F, 1.0F, 0.95F, 0.3F, 1.0F});
        g.stops.add(new float[]{0.3F, 1.0F, 0.6F, 0.05F, 0.9F});
        g.stops.add(new float[]{0.7F, 0.8F, 0.15F, 0.0F, 0.6F});
        g.stops.add(new float[]{1.0F, 0.3F, 0.0F, 0.0F, 0.0F});
        return g;
    }


    public static VFXGradient fireCore()
    {
        VFXGradient g = new VFXGradient();
        g.stops.add(new float[]{0.0F, 1.0F, 1.0F, 0.85F, 1.0F});
        g.stops.add(new float[]{0.15F, 1.0F, 0.98F, 0.4F, 1.0F});
        g.stops.add(new float[]{0.45F, 1.0F, 0.72F, 0.05F, 0.95F});
        g.stops.add(new float[]{0.75F, 0.95F, 0.28F, 0.0F, 0.7F});
        g.stops.add(new float[]{1.0F, 0.5F, 0.05F, 0.0F, 0.0F});
        return g;
    }

    public static VFXGradient fireOuter()
    {
        VFXGradient g = new VFXGradient();
        g.stops.add(new float[]{0.0F, 1.0F, 0.5F, 0.0F, 0.85F});
        g.stops.add(new float[]{0.35F, 0.75F, 0.18F, 0.0F, 0.7F});
        g.stops.add(new float[]{0.65F, 0.35F, 0.06F, 0.0F, 0.45F});
        g.stops.add(new float[]{1.0F, 0.08F, 0.02F, 0.0F, 0.0F});
        return g;
    }

    public static VFXGradient fireSmoke()
    {
        VFXGradient g = new VFXGradient();
        g.stops.add(new float[]{0.0F, 0.18F, 0.12F, 0.08F, 0.55F});
        g.stops.add(new float[]{0.4F, 0.22F, 0.18F, 0.15F, 0.35F});
        g.stops.add(new float[]{0.75F, 0.3F, 0.28F, 0.25F, 0.15F});
        g.stops.add(new float[]{1.0F, 0.35F, 0.33F, 0.3F, 0.0F});
        return g;
    }

    /**
     * White > grey > fully transparent.
     */
    public static VFXGradient smoke()
    {
        VFXGradient g = new VFXGradient();
        g.stops.add(new float[]{0.0F, 0.85F, 0.85F, 0.85F, 0.8F});
        g.stops.add(new float[]{0.5F, 0.55F, 0.55F, 0.55F, 0.4F});
        g.stops.add(new float[]{1.0F, 0.3F, 0.3F, 0.3F, 0.0F});
        return g;
    }


    public static VFXGradient lightning()
    {
        VFXGradient g = new VFXGradient();
        g.stops.add(new float[]{0.0F, 1.0F, 1.0F, 1.0F, 1.0F});
        g.stops.add(new float[]{0.3F, 0.7F, 0.85F, 1.0F, 0.9F});
        g.stops.add(new float[]{1.0F, 0.3F, 0.5F, 1.0F, 0.0F});
        return g;
    }


    public static VFXGradient magic()
    {
        VFXGradient g = new VFXGradient();
        g.stops.add(new float[]{0.0F, 0.7F, 0.1F, 1.0F, 1.0F});
        g.stops.add(new float[]{0.4F, 0.2F, 0.9F, 1.0F, 0.9F});
        g.stops.add(new float[]{0.75F, 1.0F, 1.0F, 1.0F, 0.6F});
        g.stops.add(new float[]{1.0F, 0.9F, 0.9F, 1.0F, 0.0F});
        return g;
    }


    public static VFXGradient explosion()
    {
        VFXGradient g = new VFXGradient();
        g.stops.add(new float[]{0.0F, 1.0F, 1.0F, 0.9F, 1.0F});
        g.stops.add(new float[]{0.2F, 1.0F, 0.85F, 0.1F, 1.0F});
        g.stops.add(new float[]{0.55F, 0.9F, 0.35F, 0.0F, 0.8F});
        g.stops.add(new float[]{0.8F, 0.15F, 0.1F, 0.1F, 0.5F});
        g.stops.add(new float[]{1.0F, 0.05F, 0.05F, 0.05F, 0.0F});
        return g;
    }
}
