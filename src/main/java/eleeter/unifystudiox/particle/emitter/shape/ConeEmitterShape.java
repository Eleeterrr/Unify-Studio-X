package eleeter.unifystudiox.particle.emitter.shape;

import java.util.Random;

import org.joml.Vector3f;

import eleeter.unifystudiox.particle.api.EmitterShape;

public class ConeEmitterShape implements EmitterShape
{
    private final float baseRadius;
    private final float halfAngleCos;

    public ConeEmitterShape(float baseRadius, float halfAngleDeg)
    {
        this.baseRadius = baseRadius;
        this.halfAngleCos = (float) Math.cos(Math.toRadians(halfAngleDeg));
    }

    @Override
    public void spawn(Vector3f outPosition, Vector3f outDirection, Random rng)
    {
        float angle = rng.nextFloat() * 2.0F * (float) Math.PI;
        float r = (float) Math.sqrt(rng.nextFloat()) * this.baseRadius;
        outPosition.set((float) Math.cos(angle) * r, 0.0F, (float) Math.sin(angle) * r);

        float cosTheta = this.halfAngleCos + rng.nextFloat() * (1.0F - this.halfAngleCos);
        float sinTheta = (float) Math.sqrt(1.0F - cosTheta * cosTheta);
        float phi = rng.nextFloat() * 2.0F * (float) Math.PI;

        outDirection.set(sinTheta * (float) Math.cos(phi), cosTheta, sinTheta * (float) Math.sin(phi));
    }
}
