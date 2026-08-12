package eleeter.unifystudiox.ecs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityWorld
{

    private int nextEntityId = 0;
    private final Deque<Integer> recycledIds = new ArrayDeque<>();
    private BitSet[] entityComponentBits = new BitSet[1024];

    private final Map<Class<? extends Component>, ComponentMapper<?>> mappers = new HashMap<>();
    private final List<EntitySystem> systems = new ArrayList<>();

    private final Map<Family, BitSet> familyCache = new HashMap<>();

    public EntityWorld()
    {
    }

    /**
     * Creates a new entity in the world.
     */
    public Entity createEntity()
    {
        int id;
        if (this.recycledIds.isEmpty())
        {
            id = this.nextEntityId++;
        }
        else
        {
            id = this.recycledIds.pop();
        }

        ensureCapacity(id);
        this.entityComponentBits[id] = new BitSet();
        return new Entity(id, this);
    }

    /**
     * Destroys an entity and recycles its ID.
     */
    public void destroyEntity(int entityId)
    {
        if (entityId < 0 || entityId >= this.entityComponentBits.length) return;

        BitSet bits = this.entityComponentBits[entityId];
        if (bits != null)
        {
            for (ComponentMapper<?> mapper : this.mappers.values())
            {
                mapper.remove(entityId);
            }
            removeFromFamilyCaches(entityId);
            this.entityComponentBits[entityId] = null;
            this.recycledIds.push(entityId);
        }
    }

    /**
     * Adds a component to a specific entity ID
     */
    @SuppressWarnings("unchecked")
    public <T extends Component> void addComponent(int entityId, T component)
    {
        if (entityId < 0 || entityId >= this.entityComponentBits.length) return;

        Class<T> type = (Class<T>) component.getClass();
        ComponentMapper<T> mapper = getMapper(type);
        mapper.put(entityId, component);

        BitSet bits = this.entityComponentBits[entityId];
        if (bits != null)
        {
            bits.set(ComponentType.getIndexFor(type));
            updateFamilyCaches(entityId, bits);
        }
    }


    public <T extends Component> T getComponent(int entityId, Class<T> componentClass)
    {
        return getMapper(componentClass).get(entityId);
    }

    /**
     * Removes a component from a specific entity ID.
     */
    public <T extends Component> void removeComponent(int entityId, Class<T> componentClass)
    {
        if (entityId < 0 || entityId >= this.entityComponentBits.length) return;

        getMapper(componentClass).remove(entityId);

        BitSet bits = this.entityComponentBits[entityId];
        if (bits != null)
        {
            bits.clear(ComponentType.getIndexFor(componentClass));
            updateFamilyCaches(entityId, bits);
        }
    }

    /**
     * Adds and initializes an EntitySystem.
     */
    public void addSystem(EntitySystem system)
    {
        system.setWorld(this);
        this.systems.add(system);
        this.systems.sort((s1, s2) -> Integer.compare(s1.getPriority(), s2.getPriority()));
    }

    /**
     * Removes an EntitySystem.
     */
    public void removeSystem(EntitySystem system)
    {
        this.systems.remove(system);
        system.setWorld(null);
    }

    /**
     * Retrieves a cached BitSet of entity IDs that match the given Family.
     */
    public BitSet getEntitiesFor(Family family)
    {
        return this.familyCache.computeIfAbsent(family, this::buildFamilyCache);
    }

    /**
     * Updates all registered systems.
     */
    public void update(float deltaTime)
    {
        for (EntitySystem system : this.systems)
        {
            if (system.isEnabled())
            {
                system.update(deltaTime);
            }
        }
    }

    /**
     * Clears the entire world of all entities, systems, and caches.
     */
    public void cleanup()
    {
        this.systems.clear();
        this.mappers.clear();
        this.familyCache.clear();
        this.recycledIds.clear();
        this.nextEntityId = 0;
        for (int i = 0; i < this.entityComponentBits.length; i++)
        {
            this.entityComponentBits[i] = null;
        }
    }

    private void ensureCapacity(int id)
    {
        if (id >= this.entityComponentBits.length)
        {
            int newCap = Math.max(this.entityComponentBits.length * 2, id + 1);
            BitSet[] newArr = new BitSet[newCap];
            System.arraycopy(this.entityComponentBits, 0, newArr, 0, this.entityComponentBits.length);
            this.entityComponentBits = newArr;
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Component> ComponentMapper<T> getMapper(Class<T> componentClass)
    {
        return (ComponentMapper<T>) this.mappers.computeIfAbsent(componentClass, ComponentMapper::getFor);
    }

    private BitSet buildFamilyCache(Family family)
    {
        BitSet matched = new BitSet();
        for (int i = 0; i < this.entityComponentBits.length; i++)
        {
            BitSet bits = this.entityComponentBits[i];
            if (bits != null && family.matches(bits))
            {
                matched.set(i);
            }
        }
        return matched;
    }

    private void updateFamilyCaches(int entityId, BitSet newBits)
    {
        for (Map.Entry<Family, BitSet> entry : this.familyCache.entrySet())
        {
            Family family = entry.getKey();
            BitSet cache = entry.getValue();

            if (family.matches(newBits))
            {
                cache.set(entityId);
            }
            else
            {
                cache.clear(entityId);
            }
        }
    }

    private void removeFromFamilyCaches(int entityId)
    {
        for (BitSet cache : this.familyCache.values())
        {
            cache.clear(entityId);
        }
    }
}
