package eleeter.unifystudiox.particle.emitter.shape;

import java.util.Random;

import org.joml.Vector3f;

import eleeter.unifystudiox.particle.api.EmitterShape;

public class BoxEmitterShape implements EmitterShape
{
    private final float halfX;
    private final float halfY;
    private final float halfZ;


    public BoxEmitterShape(float sizeX, float sizeY, float sizeZ)
    {
        this.halfX = sizeX * 0.5F;
        this.halfY = sizeY * 0.5F;
        this.halfZ = sizeZ * 0.5F;
    }

    @Override
    public void spawn(Vector3f outPosition, Vector3f outDirection, Random rng)
    {
        outPosition.set((rng.nextFloat() * 2.0F - 1.0F) * this.halfX, (rng.nextFloat() * 2.0F - 1.0F) * this.halfY, (rng.nextFloat() * 2.0F - 1.0F) * this.halfZ
        );

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
        outDirection.set(x * invLen, y * invLen, z * invLen);
    }
}
