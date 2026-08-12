package eleeter.unifystudiox.particle.force;

import eleeter.unifystudiox.particle.ParticleData;
import eleeter.unifystudiox.particle.api.ParticleForce;

public class TurbulenceForce implements ParticleForce
{
    private static final float INV_HASH_SCALE = 1.0F / 16777216.0F;

    private final float strength;
    private final float frequency;


    public TurbulenceForce(float strength, float frequency)
    {
        this.strength = strength;
        this.frequency = frequency;
    }

    @Override
    public void apply(ParticleData particle, float dt)
    {
        float px = particle.positionX * this.frequency;
        float py = particle.positionY * this.frequency;
        float pz = particle.positionZ * this.frequency;
        float t = particle.maxLife - particle.life;

        float nx = noise(px, py + 31.7F, pz + t);
        float ny = noise(px + 53.3F, py, pz + t + 17.4F);
        float nz = noise(px + 97.1F, py + 73.6F, pz + t);

        float scale = this.strength * dt;
        particle.velocityX += nx * scale;
        particle.velocityY += ny * scale;
        particle.velocityZ += nz * scale;
    }


    private float noise(float x, float y, float z)
    {
        int ix = (int) (x * 1000.0F);
        int iy = (int) (y * 1000.0F);
        int iz = (int) (z * 1000.0F);

        int h = ix * 73856093 ^ iy * 19349663 ^ iz * 83492791;
        h = h ^ (h >>> 13);
        h = h * 1000003;
        h = h ^ (h >>> 17);

        return (float) (h & 0x00FFFFFF) * INV_HASH_SCALE * 2.0F - 1.0F;
    }
}
