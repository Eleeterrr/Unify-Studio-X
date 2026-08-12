package eleeter.unifystudiox.assets;

import java.util.UUID;

import org.joml.Vector3f;

import eleeter.unifystudiox.scene.entity.SceneEntity;
import eleeter.unifystudiox.scene.entity.SpotlightEntity;

public class SpotlightAssetAdapter implements IModelAssetAdapter
{
    @Override
    public IModelAsset tryCreate(SceneEntity entity)
    {
        if (!(entity instanceof SpotlightEntity spotlight))
        {
            return null;
        }

        ModelPreviewSpec previewSpec = new ModelPreviewSpec(() ->
                {
                    String uniqueId = "light_" + UUID.randomUUID().toString().substring(0, 8);
                    SpotlightEntity preview = new SpotlightEntity(uniqueId);
                    preview.setColor(spotlight.getColor().x, spotlight.getColor().y, spotlight.getColor().z);
                    preview.setIntensity(spotlight.getIntensity());
                    preview.setRange(spotlight.getRange());
                    preview.setCutoff(15.0f, 35.0f); /* Standard preview angle */
                    preview.setPosition(new Vector3f(0f, 0f, 0f));
                    return preview;
                },
                new ModelPreviewSpec.Bounds(0, -spotlight.getRange() * 0.5f, 0, spotlight.getRange(), spotlight.getRange(), spotlight.getRange()),
                1.0f);

        return new ModelAssetDescriptor(spotlight.getId(), "Spotlight", false, previewSpec, AssetCategory.LIGHT);
    }
}
