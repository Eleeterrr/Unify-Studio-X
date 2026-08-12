package eleeter.unifystudiox.particle.emitter.shape;

import java.util.Random;

import org.joml.Vector3f;

import eleeter.unifystudiox.particle.api.EmitterShape;

public class SphereEmitterShape implements EmitterShape
{
    private final float radius;


    public SphereEmitterShape(float radius)
    {
        this.radius = radius;
    }

    @Override
    public void spawn(Vector3f outPosition, Vector3f outDirection, Random rng)
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
        float nx = x * invLen;
        float ny = y * invLen;
        float nz = z * invLen;

        outPosition.set(nx * this.radius, ny * this.radius, nz * this.radius);
        outDirection.set(nx, ny, nz);
    }
}
