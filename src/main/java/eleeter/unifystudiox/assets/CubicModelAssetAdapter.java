package eleeter.unifystudiox.assets;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.joml.Vector3f;

import eleeter.unifystudiox.cubic.CubicModelInstance;
import eleeter.unifystudiox.cubic.render.CubeRuntimeNode;
import eleeter.unifystudiox.scene.entity.SceneEntity;

public class CubicModelAssetAdapter implements IModelAssetAdapter
{
    private static final Map<String, ModelPreviewSpec.Bounds> boundsCache = new HashMap<>();
    @Override
    public IModelAsset tryCreate(SceneEntity entity)
    {
        if (!(entity instanceof CubicModelInstance instance))
        {
            return null;
        }

        boolean hasAnimations = !instance.getModel().animations.isEmpty();
        ModelPreviewSpec previewSpec = new ModelPreviewSpec(
                () ->
                {
                    String uniqueId = "cubic_" + UUID.randomUUID().toString().substring(0, 8);
                    CubicModelInstance preview = new CubicModelInstance(uniqueId, instance.getModel());
                    resetPose(preview.getModel().root);
                    preview.setTexture(instance.getTexture());
                    preview.setPosition(new Vector3f(0f, 0f, 0f));
                    preview.setScale(new Vector3f(1f, 1f, 1f));
                    return preview;
                },
                /* fuck up lazy fix */
                getOrMeasureBounds(instance), 0.0625f);

        return new ModelAssetDescriptor(
                instance.getId(),
                instance.getModel().name != null ? instance.getModel().name : instance.getId(),
                hasAnimations,
                previewSpec,
                AssetCategory.MODEL);
    }

    private static void resetPose(CubeRuntimeNode node)
    {
        node.translation.set(0, 0, 0);
        node.rotation.identity();
        node.scale.set(1, 1, 1);
        for (CubeRuntimeNode child : node.children)
        {
            resetPose(child);
        }
    }

    private static ModelPreviewSpec.Bounds getOrMeasureBounds(CubicModelInstance instance)
    {
        String key = instance.getModel().name + "_" + instance.getModel().vertexData.length;
        if (boundsCache.containsKey(key))
        {
            return boundsCache.get(key);
        }
        ModelPreviewSpec.Bounds bounds = measureVertexArrayBounds(instance.getModel().vertexData);
        boundsCache.put(key, bounds);
        return bounds;
    }

    private static ModelPreviewSpec.Bounds measureVertexArrayBounds(float[] vertexData)
    {
        if (vertexData == null || vertexData.length < 3)
        {
            return null;
        }

        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;

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
        }

        if (minX > maxX || minY > maxY || minZ > maxZ)
        {
            return null;
        }

        return new ModelPreviewSpec.Bounds((minX + maxX) * 0.5f, (minY + maxY) * 0.5f, (minZ + maxZ) * 0.5f, maxX - minX, maxY - minY, maxZ - minZ);
    }
}
