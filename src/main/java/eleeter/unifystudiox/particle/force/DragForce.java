package eleeter.unifystudiox.particle.force;

import eleeter.unifystudiox.particle.ParticleData;
import eleeter.unifystudiox.particle.api.ParticleForce;

public class DragForce implements ParticleForce
{
    private final float coefficient;


    public DragForce(float coefficient)
    {
        this.coefficient = coefficient;
    }

    @Override
    public void apply(ParticleData particle, float dt)
    {
        float scale = 1.0F - this.coefficient * dt;

        if (scale < 0.0F)
        {
            scale = 0.0F;
        }

        particle.velocityX *= scale;
        particle.velocityY *= scale;
        particle.velocityZ *= scale;
    }
}
