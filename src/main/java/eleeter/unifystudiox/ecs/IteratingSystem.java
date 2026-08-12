package eleeter.unifystudiox.ecs;

import java.util.BitSet;


public abstract class IteratingSystem extends EntitySystem
{

    private final Family family;

    public IteratingSystem(Family family)
    {
        this(family, 0);
    }

    public IteratingSystem(Family family, int priority)
    {
        super(priority);
        this.family = family;
    }

    public Family getFamily()
    {
        return this.family;
    }

    @Override
    public void update(float deltaTime)
    {
        if (getWorld() == null)
        {
            return;
        }

        BitSet entities = getWorld().getEntitiesFor(this.family);
        for (int i = entities.nextSetBit(0); i >= 0; i = entities.nextSetBit(i + 1))
        {
            processEntity(i, deltaTime);
        }
    }

    protected abstract void processEntity(int entityId, float deltaTime);
}
