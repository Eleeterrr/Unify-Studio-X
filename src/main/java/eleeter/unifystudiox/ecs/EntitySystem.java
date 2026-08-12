package eleeter.unifystudiox.ecs;


public abstract class EntitySystem
{

    private int priority;
    private boolean enabled;
    private EntityWorld world;

    public EntitySystem()
    {
        this(0);
    }

    public EntitySystem(int priority)
    {
        this.priority = priority;
        this.enabled = true;
    }

    public void setWorld(EntityWorld world)
    {
        this.world = world;
    }

    public EntityWorld getWorld()
    {
        return this.world;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    public int getPriority()
    {
        return this.priority;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public abstract void update(float deltaTime);
}
