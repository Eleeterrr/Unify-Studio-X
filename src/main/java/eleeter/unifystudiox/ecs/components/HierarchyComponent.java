package eleeter.unifystudiox.ecs.components;

import eleeter.unifystudiox.ecs.Component;

/**
 * Defines a parent-child relationship for the entity.
 */
public class HierarchyComponent implements Component
{

    public int parent = -1;

    public HierarchyComponent()
    {
    }

    public HierarchyComponent(int parent)
    {
        this.parent = parent;
    }
}
