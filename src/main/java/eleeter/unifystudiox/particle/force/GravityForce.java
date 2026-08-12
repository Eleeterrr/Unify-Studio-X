package eleeter.unifystudiox.particle.force;

import eleeter.unifystudiox.particle.ParticleData;
import eleeter.unifystudiox.particle.api.ParticleForce;

public class GravityForce implements ParticleForce
{
    private final float ax;
    private final float ay;
    private final float az;


    public GravityForce(float ax, float ay, float az)
    {
        this.ax = ax;
        this.ay = ay;
        this.az = az;
    }

    public GravityForce(float strength)
    {
        this(0.0F, -strength, 0.0F);
    }

    @Override
    public void apply(ParticleData particle, float dt)
    {
        particle.velocityX += this.ax * dt;
        particle.velocityY += this.ay * dt;
        particle.velocityZ += this.az * dt;
    }
}
