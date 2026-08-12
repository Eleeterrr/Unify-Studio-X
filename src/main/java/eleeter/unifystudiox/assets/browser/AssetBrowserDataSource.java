package eleeter.unifystudiox.assets.browser;

import java.util.List;

import eleeter.unifystudiox.assets.IModelAsset;

public interface AssetBrowserDataSource
{
    void refresh();
    int getRevision();
    List<IModelAsset> getPreviewModels();
    List<AssetBrowserSection> getSections();
}
