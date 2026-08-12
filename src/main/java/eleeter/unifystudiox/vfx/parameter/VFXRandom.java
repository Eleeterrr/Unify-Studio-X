package eleeter.unifystudiox.vfx.parameter;

import java.util.Random;

public class VFXRandom
{

    private static final Random RAND = new Random();

    public float min;
    public float max;

    public VFXRandom(float min, float max)
    {
        this.min = min;
        this.max = max;
    }

    /** Returns a new random value in [min, max] each call. */
    public float sample()
    {
        return this.min + RAND.nextFloat() * (this.max - this.min);
    }
}
