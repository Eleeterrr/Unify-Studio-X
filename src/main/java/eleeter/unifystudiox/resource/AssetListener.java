package eleeter.unifystudiox.resource;


public interface AssetListener
{
    void onAssetAdded(String path);

    void onAssetRemoved(String path);

    default void onAssetModified(String path) {}
}
