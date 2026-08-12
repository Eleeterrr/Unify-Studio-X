package eleeter.unifystudiox.particle;

import java.util.Collections;
import java.util.List;

import org.joml.Vector3f;

import eleeter.unifystudiox.ecs.EntityWorld;
import eleeter.unifystudiox.scene.entity.BaseSceneEntity;

public class ParticleEffectEntity extends BaseSceneEntity
{
    private final String id;
    private final List<ParticleEmitter> emitters;


    public ParticleEffectEntity(String id, List<ParticleEmitter> emitters)
    {
        this.id = id;
        this.emitters = Collections.unmodifiableList(emitters);
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public String getAssetPath()
    {
        return null;
    }


    @Override
    public void update(double deltaTime)
    {
        Vector3f pos = getPosition();
        float dt = (float) deltaTime;

        for (int i = 0; i < this.emitters.size(); i++)
        {
            this.emitters.get(i).update(dt, pos.x, pos.y, pos.z);
        }
    }

    public List<ParticleEmitter> getEmitters()
    {
        return this.emitters;
    }

    @Override
    public void initEcs(EntityWorld world)
    {
        super.initEcs(world);
    }

    @Override
    public void cleanup()
    {
        super.cleanup();
    }
}
