package eleeter.unifystudiox.renderer.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eleeter.unifystudiox.renderer.entities.EntityRenderer;
import eleeter.unifystudiox.scene.entity.SceneEntity;
import eleeter.unifystudiox.util.log.AniLogger;

public class RenderDispatcher
{

    private final Map<Class<?>, EntityRenderer<?>> registry = new HashMap<>();
    private final Set<Class<?>> reportedMissing = new HashSet<>();


    public <T extends SceneEntity> void register(EntityRenderer<T> renderer)
    {
        this.registry.put(renderer.getSupportedType(), renderer);
    }


    @SuppressWarnings("unchecked")
    public void dispatchAll(Iterable<SceneEntity> entities, RenderContext context)
    {
        for (SceneEntity entity : entities)
        {
            if (!entity.isVisible())
            {
                continue;
            }

            EntityRenderer<SceneEntity> renderer =
                    (EntityRenderer<SceneEntity>) resolve(entity.getClass());

            if (renderer == null)
            {
                if (this.reportedMissing.add(entity.getClass()))
                {
                    AniLogger.warn("RenderDispatcher", "No renderer registered for type '" + 
                        entity.getClass().getSimpleName() + "'");
                }
                continue;
            }

            renderer.render(entity, context);
        }
    }


    public void cleanup()
    {
        this.registry.values().forEach(EntityRenderer::cleanup);
        this.registry.clear();
        this.reportedMissing.clear();
    }



    private EntityRenderer<?> resolve(Class<?> type)
    {
        Class<?> cursor = type;
        while (cursor != null && cursor != Object.class)
        {
            EntityRenderer<?> renderer = this.registry.get(cursor);
            if (renderer != null)
            {
                return renderer;
            }
            cursor = cursor.getSuperclass();
        }
        return null;
    }


    public List<EntityRenderer<?>> getAllRenderers()
    {
        return new ArrayList<>(this.registry.values());
    }


    @SuppressWarnings("unchecked")
    public EntityRenderer<SceneEntity> getRendererFor(SceneEntity entity)
    {
        if (!entity.isVisible()) return null;

        EntityRenderer<SceneEntity> renderer =
                (EntityRenderer<SceneEntity>) resolve(entity.getClass());

        if (renderer == null)
        {
            if (this.reportedMissing.add(entity.getClass()))
            {
                AniLogger.warn("RenderDispatcher", "No renderer registered for type '" +
                        entity.getClass().getSimpleName() + "'");
            }
        }
        return renderer;
    }
}
