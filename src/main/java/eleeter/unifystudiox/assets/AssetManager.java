package eleeter.unifystudiox.assets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eleeter.unifystudiox.scene.entity.SceneEntity;

public class AssetManager
{
    private final List<IModelAssetAdapter> adapters = new ArrayList<>();
    private final Map<String, IModelAsset> loadedModels = new LinkedHashMap<>();
    private int revision = 0;

    public AssetManager()
    {
        registerAdapter(new AmbModelAssetAdapter());
        registerAdapter(new CubicModelAssetAdapter());
        registerAdapter(new SpotlightAssetAdapter());
        registerAdapter(new PointLightAssetAdapter());
        registerAdapter(new LabelAssetAdapter());
    }

    public void registerAdapter(IModelAssetAdapter adapter)
    {
        if (adapter != null)
        {
            this.adapters.add(adapter);
        }
    }

    public void syncFromScene(Collection<SceneEntity> sceneEntities)
    {
        boolean changed = false;
        Set<String> seenModelIds = new LinkedHashSet<>();

        for (SceneEntity entity : sceneEntities)
        {
            IModelAsset mapped = mapEntity(entity);
            if (mapped == null)
            {
                continue;
            }

            seenModelIds.add(mapped.getId());
            IModelAsset previous = this.loadedModels.put(mapped.getId(), mapped);
            if (previous == null)
            {
                changed = true;
            }
        }

        List<String> toRemove = new ArrayList<>();
        for (String existingId : this.loadedModels.keySet())
        {
            if (!seenModelIds.contains(existingId))
            {
                toRemove.add(existingId);
            }
        }
        if (!toRemove.isEmpty())
        {
            changed = true;
            for (String id : toRemove)
            {
                this.loadedModels.remove(id);
            }
        }

        if (changed)
        {
            this.revision++;
        }
    }

    public List<IModelAsset> getLoadedModels()
    {
        return Collections.unmodifiableList(new ArrayList<>(this.loadedModels.values()));
    }

    public int getRevision()
    {
        return this.revision;
    }

    private IModelAsset mapEntity(SceneEntity entity)
    {
        for (IModelAssetAdapter adapter : this.adapters)
        {
            IModelAsset mapped = adapter.tryCreate(entity);
            if (mapped != null)
            {
                return mapped;
            }
        }
        return null;
    }
}
