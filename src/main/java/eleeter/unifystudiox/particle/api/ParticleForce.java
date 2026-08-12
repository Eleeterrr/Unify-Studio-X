package eleeter.unifystudiox.particle.api;

import eleeter.unifystudiox.particle.ParticleData;

public interface ParticleForce
{
    void apply(ParticleData particle, float dt);
}
