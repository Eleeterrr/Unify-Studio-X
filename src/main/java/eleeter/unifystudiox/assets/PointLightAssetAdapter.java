package eleeter.unifystudiox.assets;

import java.util.UUID;

import org.joml.Vector3f;

import eleeter.unifystudiox.scene.entity.PointLightEntity;
import eleeter.unifystudiox.scene.entity.SceneEntity;

public class PointLightAssetAdapter implements IModelAssetAdapter
{
    @Override
    public IModelAsset tryCreate(SceneEntity entity)
    {
        if (!(entity instanceof PointLightEntity pointLight))
        {
            return null;
        }

        ModelPreviewSpec previewSpec = new ModelPreviewSpec(() ->
                {
                    String uniqueId = "light_" + UUID.randomUUID().toString().substring(0, 8);
                    PointLightEntity preview = new PointLightEntity(uniqueId);
                    preview.setColor(pointLight.getColor().x, pointLight.getColor().y, pointLight.getColor().z);
                    preview.setIntensity(pointLight.getIntensity());
                    preview.setRange(pointLight.getRange());
                    preview.setPosition(new Vector3f(0f, 0f, 0f));
                    return preview;
                },
                new ModelPreviewSpec.Bounds(0, -pointLight.getRange() * 0.5f, 0, pointLight.getRange(), pointLight.getRange(), pointLight.getRange()),
                1.0f);

        return new ModelAssetDescriptor(pointLight.getId(), "Point Light", false, previewSpec, AssetCategory.LIGHT);
    }
}
