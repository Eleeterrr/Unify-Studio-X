package eleeter.unifystudiox.assets;

public class ModelAssetDescriptor implements IModelAsset
{
    private final String id;
    private final String displayName;
    private final boolean animations;
    private final ModelPreviewSpec previewSpec;
    private final AssetCategory category;

    public ModelAssetDescriptor(String id, String displayName, boolean animations, ModelPreviewSpec previewSpec)
    {
        this(id, displayName, animations, previewSpec, AssetCategory.MODEL);
    }

    public ModelAssetDescriptor(String id, String displayName, boolean animations, ModelPreviewSpec previewSpec, AssetCategory category)
    {
        this.id = id;
        this.displayName = displayName;
        this.animations = animations;
        this.previewSpec = previewSpec;
        this.category = category;
    }

    @Override
    public AssetCategory getCategory()
    {
        return this.category;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public String getDisplayName()
    {
        return this.displayName;
    }

    @Override
    public boolean hasAnimations()
    {
        return this.animations;
    }

    @Override
    public ModelPreviewSpec getPreviewSpec()
    {
        return this.previewSpec;
    }
}
