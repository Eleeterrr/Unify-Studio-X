package eleeter.unifystudiox.vfx.force;

import eleeter.unifystudiox.vfx.core.VFXParticle;

public class WindForce implements VFXForce
{

    private final float impulseX;
    private final float impulseY;
    private final float impulseZ;


    public WindForce(float dirX, float dirY, float dirZ, float strength)
    {
        float len = (float) Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        if (len < 1e-6F)
        {
            this.impulseX = 0.0F;
            this.impulseY = 0.0F;
            this.impulseZ = 0.0F;
        } else
        {
            this.impulseX = (dirX / len) * strength;
            this.impulseY = (dirY / len) * strength;
            this.impulseZ = (dirZ / len) * strength;
        }
    }

    @Override
    public void apply(VFXParticle particle, float elapsedTime, float dt)
    {
        particle.vx += this.impulseX * dt;
        particle.vy += this.impulseY * dt;
        particle.vz += this.impulseZ * dt;
    }
}
