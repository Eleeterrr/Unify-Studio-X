package eleeter.unifystudiox.vfx.parameter;

import java.util.ArrayList;
import java.util.List;

public class VFXCurve
{
    public final List<float[]> keys = new ArrayList<>();

    public float evaluate(float t)
    {
        if (this.keys.isEmpty())
        {
            return 1.0F;
        }

        int size = this.keys.size();

        if (t <= this.keys.get(0)[0])
        {
            return this.keys.get(0)[1];
        }

        if (t >= this.keys.get(size - 1)[0])
        {
            return this.keys.get(size - 1)[1];
        }

        int lo = 0;
        int hi = size - 1;
        while (lo + 1 < hi)
        {
            int mid = (lo + hi) / 2;
            if (this.keys.get(mid)[0] <= t)
            {
                lo = mid;
            } else
            {
                hi = mid;
            }
        }

        float[] a = this.keys.get(lo);
        float[] b = this.keys.get(hi);
        float span = b[0] - a[0];

        if (span <= 0.0F)
        {
            return a[1];
        }

        float localT = (t - a[0]) / span;
        return a[1] + localT * (b[1] - a[1]);
    }


    /**
     * Flat line at a constant value v.
     */
    public static VFXCurve constant(float v)
    {
        VFXCurve c = new VFXCurve();
        c.keys.add(new float[]{0.0F, v});
        c.keys.add(new float[]{1.0F, v});
        return c;
    }

    public static VFXCurve fadeIn()
    {
        VFXCurve c = new VFXCurve();
        c.keys.add(new float[]{0.0F, 0.0F});
        c.keys.add(new float[]{1.0F, 1.0F});
        return c;
    }

    public static VFXCurve fadeOut()
    {
        VFXCurve c = new VFXCurve();
        c.keys.add(new float[]{0.0F, 1.0F});
        c.keys.add(new float[]{1.0F, 0.0F});
        return c;
    }

    public static VFXCurve fadeInOut()
    {
        VFXCurve c = new VFXCurve();
        c.keys.add(new float[]{0.0F, 0.0F});
        c.keys.add(new float[]{0.5F, 1.0F});
        c.keys.add(new float[]{1.0F, 0.0F});
        return c;
    }

    public static VFXCurve growThenShrink()
    {
        VFXCurve c = new VFXCurve();
        c.keys.add(new float[]{0.0F, 0.0F});
        c.keys.add(new float[]{0.3F, 1.0F});
        c.keys.add(new float[]{1.0F, 0.0F});
        return c;
    }

    public static VFXCurve easeOut()
    {
        VFXCurve c = new VFXCurve();
        c.keys.add(new float[]{0.0F, 1.0F});
        c.keys.add(new float[]{0.4F, 0.7F});
        c.keys.add(new float[]{0.7F, 0.3F});
        c.keys.add(new float[]{1.0F, 0.0F});
        return c;
    }


    public static VFXCurve fireCoreCurve()
    {
        VFXCurve c = new VFXCurve();
        c.keys.add(new float[]{0.0F, 0.0F});
        c.keys.add(new float[]{0.2F, 1.0F});
        c.keys.add(new float[]{0.55F, 0.65F});
        c.keys.add(new float[]{1.0F, 0.0F});
        return c;
    }


    public static VFXCurve fireBodyCurve()
    {
        VFXCurve c = new VFXCurve();
        c.keys.add(new float[]{0.0F, 0.0F});
        c.keys.add(new float[]{0.35F, 1.0F});
        c.keys.add(new float[]{0.7F, 0.55F});
        c.keys.add(new float[]{1.0F, 0.0F});
        return c;
    }
}
