package eleeter.unifystudiox.ecs.systems;

import java.util.BitSet;

import eleeter.unifystudiox.ecs.EntitySystem;
import eleeter.unifystudiox.ecs.EntityWorld;
import eleeter.unifystudiox.ecs.Family;
import eleeter.unifystudiox.ecs.components.HierarchyComponent;
import eleeter.unifystudiox.ecs.components.TransformComponent;


public class TransformSystem extends EntitySystem
{

    private final Family family;

    public TransformSystem()
    {
        super(100);
        this.family = Family.all(TransformComponent.class).build();
    }

    @Override
    public void update(float deltaTime)
    {
        EntityWorld world = getWorld();
        if (world == null)
        {
            return;
        }

        BitSet entities = world.getEntitiesFor(this.family);
        for (int i = entities.nextSetBit(0); i >= 0; i = entities.nextSetBit(i + 1))
        {
            TransformComponent transform = world.getComponent(i, TransformComponent.class);
            HierarchyComponent hierarchy = world.getComponent(i, HierarchyComponent.class);

            if (transform.isDirty || (hierarchy != null && isParentDirty(world, hierarchy.parent)))
            {
                updateTransform(world, i, transform, hierarchy);
            }
        }
    }

    private boolean isParentDirty(EntityWorld world, int parentId)
    {
        if (parentId == -1)
        {
            return false;
        }
        
        TransformComponent parentTransform = world.getComponent(parentId, TransformComponent.class);
        if (parentTransform == null)
        {
            return false;
        }
        
        if (parentTransform.isDirty)
        {
            return true;
        }
        
        HierarchyComponent parentHierarchy = world.getComponent(parentId, HierarchyComponent.class);
        if (parentHierarchy != null)
        {
            return isParentDirty(world, parentHierarchy.parent);
        }
        return false;
    }

    private void updateTransform(EntityWorld world, int entityId, TransformComponent transform, HierarchyComponent hierarchy)
    {
        if (hierarchy != null && hierarchy.parent != -1)
        {
            TransformComponent parentTransform = world.getComponent(hierarchy.parent, TransformComponent.class);
            if (parentTransform != null)
            {
                HierarchyComponent parentHierarchy = world.getComponent(hierarchy.parent, HierarchyComponent.class);
                if (parentTransform.isDirty)
                {
                    updateTransform(world, hierarchy.parent, parentTransform, parentHierarchy);
                }

                transform.worldPosition.set(transform.localPosition).mulPosition(parentTransform.modelMatrix);
                transform.worldRotation.set(parentTransform.worldRotation).mul(transform.localRotation);
                transform.worldScale.set(transform.localScale).mul(parentTransform.worldScale);
            }
            else
            {
                transform.worldPosition.set(transform.localPosition);
                transform.worldRotation.set(transform.localRotation);
                transform.worldScale.set(transform.localScale);
            }
        }
        else
        {
            transform.worldPosition.set(transform.localPosition);
            transform.worldRotation.set(transform.localRotation);
            transform.worldScale.set(transform.localScale);
        }

        transform.modelMatrix.translationRotateScale(transform.worldPosition, transform.worldRotation, transform.worldScale
        );

        transform.isDirty = false;
    }
}
