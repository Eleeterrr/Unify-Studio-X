package eleeter.unifystudiox.assets;

import eleeter.unifystudiox.amb.AmbLoader;
import eleeter.unifystudiox.amb.AmbMesh;
import eleeter.unifystudiox.amb.AmbModel;
import eleeter.unifystudiox.amb.AmbModelInstance;
import eleeter.unifystudiox.gltf.GltfLoader;
import eleeter.unifystudiox.scene.entity.SceneEntity;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.joml.Vector3f;

public class AmbModelAssetAdapter implements IModelAssetAdapter
{
    private static final Map<String, AmbModel> modelCache = new HashMap<>();
    private static final Map<String, ModelPreviewSpec.Bounds> boundsCache = new HashMap<>();

    @Override
    public IModelAsset tryCreate(SceneEntity entity)
    {
        if (!(entity instanceof AmbModelInstance instance))
        {
            return null;
        }

        Path sourcePath = Paths.get(instance.sourceModel.filePath);
        String displayName = sourcePath.getFileName() != null ? sourcePath.getFileName().toString() : instance.getId();
        ModelPreviewSpec previewSpec = new ModelPreviewSpec(() ->
        {
            String path = instance.sourceModel.filePath;
            AmbModel freshModel = modelCache.get(path);
            if (freshModel == null)
            {
                if (path.toLowerCase().endsWith(".glb") || path.toLowerCase().endsWith(".gltf"))
                {
                    freshModel = GltfLoader.load(path);
                } else
                {
                    freshModel = AmbLoader.load(path);
                }
                modelCache.put(path, freshModel);
            }
            String uniqueId = "amb_" + UUID.randomUUID().toString().substring(0, 8);
            AmbModelInstance preview = new AmbModelInstance(uniqueId, freshModel);
            preview.setTexture(instance.getTexture());
            preview.setPosition(new Vector3f(0f, 0f, 0f));
            preview.setScale(new Vector3f(1f, 1f, 1f));
            return preview;
        },
                getOrMeasureBounds(instance),
                1.0f);

        return new ModelAssetDescriptor(instance.getId(), displayName, false, previewSpec, AssetCategory.MODEL);
    }

    private static ModelPreviewSpec.Bounds getOrMeasureBounds(AmbModelInstance instance)
    {
        String path = instance.sourceModel.filePath;
        if (boundsCache.containsKey(path))
        {
            return boundsCache.get(path);
        }
        ModelPreviewSpec.Bounds bounds = measureModelBounds(instance);
        boundsCache.put(path, bounds);
        return bounds;
    }

    private static ModelPreviewSpec.Bounds measureModelBounds(AmbModelInstance instance)
    {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;
        boolean hasVertex = false;

        for (AmbMesh mesh : instance.sourceModel.meshes)
        {

            float[] vertexData = mesh.vertexData;
            if (vertexData == null || vertexData.length < 3)
            {
                continue;
            }

            for (int i = 0; i + 2 < vertexData.length; i += 16)
            {
                float x = vertexData[i];
                float y = vertexData[i + 1];
                float z = vertexData[i + 2];
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                minZ = Math.min(minZ, z);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
                maxZ = Math.max(maxZ, z);
                hasVertex = true;
            }
        }

        if (!hasVertex)
        {
            return null;
        }

        return new ModelPreviewSpec.Bounds((minX + maxX) * 0.5f, (minY + maxY) * 0.5f, (minZ + maxZ) * 0.5f,
                maxX - minX, maxY - minY, maxZ - minZ);
    }
}
