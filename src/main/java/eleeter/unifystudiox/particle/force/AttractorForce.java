package eleeter.unifystudiox.particle.force;

import eleeter.unifystudiox.particle.ParticleData;
import eleeter.unifystudiox.particle.api.ParticleForce;

public class AttractorForce implements ParticleForce
{
    private static final float MIN_DIST_SQ = 0.01F;

    private final float attractorX;
    private final float attractorY;
    private final float attractorZ;
    private final float strength;
    private final float range;


    public AttractorForce(float x, float y, float z, float strength, float range)
    {
        this.attractorX = x;
        this.attractorY = y;
        this.attractorZ = z;
        this.strength = strength;
        this.range = range;
    }

    @Override
    public void apply(ParticleData particle, float dt)
    {
        float dx = this.attractorX - particle.positionX;
        float dy = this.attractorY - particle.positionY;
        float dz = this.attractorZ - particle.positionZ;
        float distSq = dx * dx + dy * dy + dz * dz;

        if (distSq > this.range * this.range)
        {
            return;
        }

        if (distSq < MIN_DIST_SQ)
        {
            distSq = MIN_DIST_SQ;
        }

        float dist = (float) Math.sqrt(distSq);
        float attenuation = 1.0F - dist / this.range;
        float forceMag = this.strength * attenuation * dt / dist;

        particle.velocityX += dx * forceMag;
        particle.velocityY += dy * forceMag;
        particle.velocityZ += dz * forceMag;
    }
}
