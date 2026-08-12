package eleeter.unifystudiox.vfx.force;

import eleeter.unifystudiox.vfx.core.VFXParticle;

public interface VFXForce
{
    void apply(VFXParticle particle, float elapsedTime, float dt);
}
