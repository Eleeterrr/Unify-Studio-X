package eleeter.unifystudiox.vfx.force;

import eleeter.unifystudiox.vfx.core.VFXParticle;

public class GravityForce implements VFXForce
{

    private final float accelerationX;
    private final float accelerationY;
    private final float accelerationZ;


    public GravityForce(float ax, float ay, float az)
    {
        this.accelerationX = ax;
        this.accelerationY = ay;
        this.accelerationZ = az;
    }

    public GravityForce(float gravityY)
    {
        this(0.0F, -gravityY, 0.0F);
    }

    @Override
    public void apply(VFXParticle particle, float elapsedTime, float dt)
    {
        particle.vx += this.accelerationX * dt;
        particle.vy += this.accelerationY * dt;
        particle.vz += this.accelerationZ * dt;
    }
}
