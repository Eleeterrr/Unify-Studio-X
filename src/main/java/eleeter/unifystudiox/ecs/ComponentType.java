package eleeter.unifystudiox.ecs;

import java.util.HashMap;
import java.util.Map;

public class ComponentType
{

    private static int nextIndex = 0;
    private static final Map<Class<? extends Component>, ComponentType> ASSIGNED_TYPES = new HashMap<>();

    /**
     * Gets or creates the ComponentType for the specified class.
     */
    public static ComponentType getFor(Class<? extends Component> componentType)
    {
        ComponentType type = ASSIGNED_TYPES.get(componentType);
        if (type == null)
        {
            type = new ComponentType();
            ASSIGNED_TYPES.put(componentType, type);
        }
        return type;
    }

    /**
     * Gets the raw bit index for the specified class.
     */
    public static int getIndexFor(Class<? extends Component> componentType)
    {
        return getFor(componentType).getIndex();
    }

    private final int index;

    private ComponentType()
    {
        this.index = nextIndex;
        nextIndex++;
    }

    public int getIndex()
    {
        return this.index;
    }
}
