package eleeter.unifystudiox.vfx.force;

import eleeter.unifystudiox.vfx.core.VFXParticle;

public class TurbulenceForce implements VFXForce
{

    private final float strength;
    private final float frequency;

    public TurbulenceForce(float strength, float frequency)
    {
        this.strength = strength;
        this.frequency = frequency;
    }

    @Override
    public void apply(VFXParticle particle, float elapsedTime, float dt)
    {
        float x = particle.x * frequency;
        float y = particle.y * frequency;
        float z = particle.z * frequency;
        float time = elapsedTime * frequency;

        particle.vx += perlin(x, y, z, time) * strength * dt;
        particle.vy += perlin(x, y, z, time + 31.7F) * strength * dt;
        particle.vz += perlin(x, y, z, time + 127.3F) * strength * dt;
    }

    private static float perlin(float x, float y, float z, float w)
    {
        int x0 = (int) Math.floor(x);
        int y0 = (int) Math.floor(y);
        int z0 = (int) Math.floor(z);
        int w0 = (int) Math.floor(w);

        float dx = x - x0;
        float dy = y - y0;
        float dz = z - z0;
        float dw = w - w0;

        float fx = fade(dx);
        float fy = fade(dy);
        float fz = fade(dz);
        float fw = fade(dw);

        float n000 = grad(hash(x0, y0, z0, w0), dx, dy, dz, dw);
        float n100 = grad(hash(x0 + 1, y0, z0, w0), dx - 1.0F, dy, dz, dw);
        float n010 = grad(hash(x0, y0 + 1, z0, w0), dx, dy - 1.0F, dz, dw);
        float n110 = grad(hash(x0 + 1, y0 + 1, z0, w0),
                dx - 1.0F, dy - 1.0F, dz, dw);

        float n001 = grad(hash(x0, y0, z0 + 1, w0), dx, dy, dz - 1.0F, dw);
        float n101 = grad(hash(x0 + 1, y0, z0 + 1, w0),
                dx - 1.0F, dy, dz - 1.0F, dw);
        float n011 = grad(hash(x0, y0 + 1, z0 + 1, w0),
                dx, dy - 1.0F, dz - 1.0F, dw);
        float n111 = grad(hash(x0 + 1, y0 + 1, z0 + 1, w0),
                dx - 1.0F, dy - 1.0F, dz - 1.0F, dw);

        float xy0 = lerp(
                lerp(n000, n100, fx),
                lerp(n010, n110, fx),
                fy
        );

        float xy1 = lerp(
                lerp(n001, n101, fx),
                lerp(n011, n111, fx),
                fy
        );

        return lerp(xy0, xy1, fz);
    }

    private static float fade(float value)
    {
        return value * value * value
                * (value * (value * 6.0F - 15.0F) + 10.0F);
    }

    private static float lerp(float a, float b, float amount)
    {
        return a + amount * (b - a);
    }

    private static int hash(int x, int y, int z, int w)
    {
        int result = x * 1619
                ^ y * 31337
                ^ z * 6971
                ^ w * 1013;

        result ^= result >> 8;

        return result & 0xFF;
    }

    private static float grad(int hash, float x, float y, float z, float w)
    {
        int value = hash & 7;

        float first = value < 4 ? x : y;
        float second = value < 4 ? y : z;

        if ((value & 1) != 0)
        {
            first = -first;
        }

        if ((value & 2) != 0)
        {
            second = -second;
        }

        return first + second;
    }
}
