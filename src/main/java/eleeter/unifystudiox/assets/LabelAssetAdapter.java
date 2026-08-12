package eleeter.unifystudiox.assets;

import java.util.UUID;

import org.joml.Vector3f;

import eleeter.unifystudiox.scene.entity.LabelEntity;
import eleeter.unifystudiox.scene.entity.SceneEntity;

public class LabelAssetAdapter implements IModelAssetAdapter
{
    private static final String PREVIEW_TEXT = "Label";
    private static final float PREVIEW_FONT_SIZE = 1f;

    @Override
    public IModelAsset tryCreate(SceneEntity entity)
    {
        if (!(entity instanceof LabelEntity label))
        {
            return null;
        }

        label.getMeshData();

        ModelPreviewSpec previewSpec = new ModelPreviewSpec(
                () ->
                {
                    String uniqueId = "label_" + UUID.randomUUID().toString().substring(0, 8);
                    LabelEntity preview = new LabelEntity(uniqueId);
                    preview.setText(PREVIEW_TEXT);
                    preview.setFont(label.getFont());
                    preview.setColor(label.getColor().x, label.getColor().y, label.getColor().z, label.getColor().w);
                    preview.setBillboard(false);
                    preview.setPosition(new Vector3f(0f, 0f, 0f));
                    preview.setFontSize(PREVIEW_FONT_SIZE);
                    preview.getMeshData();
                    return preview;
                },
                new ModelPreviewSpec.Bounds(0, 0, 0, 2.5f, 0.8f, 0.1f),
                1.0f);

        return new ModelAssetDescriptor(label.getId(), "Label: " + label.getText(), false, previewSpec, AssetCategory.ANNOTATION);
    }
}
