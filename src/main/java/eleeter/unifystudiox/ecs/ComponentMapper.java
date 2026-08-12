package eleeter.unifystudiox.ecs;

import java.lang.reflect.Array;


public class ComponentMapper<T extends Component>
{

    private T[] components;
    private final Class<T> componentClass;

    /**
     * Creates a new ComponentMapper for the given class.
     */
    public static <T extends Component> ComponentMapper<T> getFor(Class<T> componentClass)
    {
        return new ComponentMapper<>(componentClass);
    }

    @SuppressWarnings("unchecked")
    public ComponentMapper(Class<T> componentClass)
    {
        this.componentClass = componentClass;
        this.components = (T[]) Array.newInstance(componentClass, 64);
    }

    /**
     * Gets the component for the specified entity ID.
     */
    public T get(int entityId)
    {
        if (entityId < 0 || entityId >= this.components.length)
        {
            return null;
        }
        return this.components[entityId];
    }

    /**
     * Sets the component for the specified entity ID.
     */
    public void put(int entityId, T component)
    {
        ensureCapacity(entityId);
        this.components[entityId] = component;
    }

    /**
     * Removes the component for the specified entity ID.
     */
    public void remove(int entityId)
    {
        if (entityId >= 0 && entityId < this.components.length)
        {
            this.components[entityId] = null;
        }
    }

    @SuppressWarnings("unchecked")
    private void ensureCapacity(int entityId)
    {
        if (entityId >= this.components.length)
        {
            int newCapacity = Math.max(this.components.length * 2, entityId + 1);
            T[] newArray = (T[]) Array.newInstance(this.componentClass, newCapacity);
            System.arraycopy(this.components, 0, newArray, 0, this.components.length);
            this.components = newArray;
        }
    }
}
