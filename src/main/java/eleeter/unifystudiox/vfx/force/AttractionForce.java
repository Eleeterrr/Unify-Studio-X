package eleeter.unifystudiox.vfx.force;

import eleeter.unifystudiox.vfx.core.VFXParticle;

public class AttractionForce implements VFXForce
{

    private final float targetX;
    private final float targetY;
    private final float targetZ;
    private final float strength;
    private final float minDistance;


    public AttractionForce(float targetX, float targetY, float targetZ,
                           float strength, float minDist)
    {
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.strength = strength;
        this.minDistance = minDist;
    }

    @Override
    public void apply(VFXParticle particle, float elapsedTime, float dt)
    {
        float dx = this.targetX - particle.x;
        float dy = this.targetY - particle.y;
        float dz = this.targetZ - particle.z;

        float distSq = dx * dx + dy * dy + dz * dz;
        float dist = (float) Math.sqrt(distSq);

        if (dist < this.minDistance)
        {
            return;
        }

        float force = this.strength / distSq;
        float invDist = 1.0F / dist;

        particle.vx += dx * invDist * force * dt;
        particle.vy += dy * invDist * force * dt;
        particle.vz += dz * invDist * force * dt;
    }
}
