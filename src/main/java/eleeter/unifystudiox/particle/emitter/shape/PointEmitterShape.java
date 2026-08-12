package eleeter.unifystudiox.particle.emitter.shape;

import java.util.Random;

import org.joml.Vector3f;

import eleeter.unifystudiox.particle.api.EmitterShape;

public class PointEmitterShape implements EmitterShape
{
    @Override
    public void spawn(Vector3f outPosition, Vector3f outDirection, Random rng)
    {
        outPosition.set(0.0F, 0.0F, 0.0F);
        randomUnitVector(outDirection, rng);
    }


    private void randomUnitVector(Vector3f out, Random rng)
    {
        float x;
        float y;
        float z;
        float lenSq;

        do
        {
            x = rng.nextFloat() * 2.0F - 1.0F;
            y = rng.nextFloat() * 2.0F - 1.0F;
            z = rng.nextFloat() * 2.0F - 1.0F;
            lenSq = x * x + y * y + z * z;
        }
        while (lenSq > 1.0F || lenSq < 1e-6F);

        float invLen = 1.0F / (float) Math.sqrt(lenSq);
        out.set(x * invLen, y * invLen, z * invLen);
    }
}
