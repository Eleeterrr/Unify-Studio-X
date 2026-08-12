package eleeter.unifystudiox.assets.browser;

import eleeter.unifystudiox.assets.AssetCategory;
import eleeter.unifystudiox.assets.IModelAsset;

public class AssetBrowserItem
{
    private final String id;
    private final String title;
    private final String subtitle;
    private final AssetBrowserItemType type;
    private final String previewAssetId;
    private final boolean animated;
    private final boolean dragAndDropPlaceholder;
    private final IModelAsset modelAsset;

    private AssetBrowserItem(
            String id,
            String title,
            String subtitle,
            AssetBrowserItemType type,
            String previewAssetId,
            boolean animated,
            boolean dragAndDropPlaceholder,
            IModelAsset modelAsset)
    {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.type = type;
        this.previewAssetId = previewAssetId;
        this.animated = animated;
        this.dragAndDropPlaceholder = dragAndDropPlaceholder;
        this.modelAsset = modelAsset;
    }

    public static AssetBrowserItem forModel(IModelAsset asset)
    {
        AssetBrowserItemType type = (asset.getCategory() == AssetCategory.TOOL)
                ? AssetBrowserItemType.TOOL
                : AssetBrowserItemType.MODEL;

        String subtitle = buildDefaultSubtitle(asset);

        return new AssetBrowserItem(
                asset.getId(),
                asset.getDisplayName(),
                subtitle,
                type,
                asset.getId(),
                asset.hasAnimations(),
                true,
                asset);
    }

    private static String buildDefaultSubtitle(IModelAsset asset)
    {
        if (asset.getCategory() == AssetCategory.TOOL)
        {
            return "Built-in tool template";
        }
        return asset.hasAnimations() ? "Animated model" : "Static model";
    }

    public static AssetBrowserItem forTool(String id, String title, String subtitle)
    {
        return new AssetBrowserItem(
                id,
                title,
                subtitle,
                AssetBrowserItemType.TOOL,
                null,
                false,
                true,
                null);
    }

    public static AssetBrowserItem placeholder(String id, String title, String subtitle)
    {
        return new AssetBrowserItem(
                id,
                title,
                subtitle,
                AssetBrowserItemType.PLACEHOLDER,
                null,
                false,
                true,
                null);
    }

    public String getId()
    {
        return this.id;
    }

    public String getTitle()
    {
        return this.title;
    }

    public String getSubtitle()
    {
        return this.subtitle;
    }

    public AssetBrowserItemType getType()
    {
        return this.type;
    }

    public String getPreviewAssetId()
    {
        return this.previewAssetId;
    }

    public boolean isAnimated()
    {
        return this.animated;
    }

    public boolean hasPreview()
    {
        return this.previewAssetId != null && !this.previewAssetId.isEmpty();
    }

    public boolean isDragAndDropPlaceholder()
    {
        return this.dragAndDropPlaceholder;
    }

    public IModelAsset getModelAsset()
    {
        return this.modelAsset;
    }
}
