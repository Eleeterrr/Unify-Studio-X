package eleeter.unifystudiox.ecs;


public class Entity
{

    private final int id;
    private final EntityWorld world;

    public Entity(int id, EntityWorld world)
    {
        this.id = id;
        this.world = world;
    }

    public int getId()
    {
        return this.id;
    }

    public EntityWorld getWorld()
    {
        return this.world;
    }

    /**
     * Adds a component to this entity.
     */
    public Entity add(Component component)
    {
        this.world.addComponent(this.id, component);
        return this;
    }

    /**
     * Retrieves a component from this entity.
     */
    public <T extends Component> T get(Class<T> componentClass)
    {
        return this.world.getComponent(this.id, componentClass);
    }

    /**
     * Removes a component from this entity.
     */
    public <T extends Component> Entity remove(Class<T> componentClass)
    {
        this.world.removeComponent(this.id, componentClass);
        return this;
    }

    /**
     * Destroys this entity from the world.
     */
    public void destroy()
    {
        this.world.destroyEntity(this.id);
    }

    @Override
    public int hashCode()
    {
        return this.id;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (!(obj instanceof Entity other))
        {
            return false;
        }
        return this.id == other.id && this.world == other.world;
    }
}
