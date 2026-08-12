package eleeter.unifystudiox.assets;

/**
 * Unified internal model asset contract consumed by tooling/UI.
 * Format-specific loaders are expected to convert into this abstraction.
 */
public interface IModelAsset
{
    String getId();

    String getDisplayName();

    boolean hasAnimations();

    ModelPreviewSpec getPreviewSpec();

    default AssetCategory getCategory()
    {
        return AssetCategory.MODEL;
    }
}
