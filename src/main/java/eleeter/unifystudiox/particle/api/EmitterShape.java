package eleeter.unifystudiox.particle.api;

import java.util.Random;

import org.joml.Vector3f;

public interface EmitterShape
{
    void spawn(Vector3f outPosition, Vector3f outDirection, Random rng);
}
