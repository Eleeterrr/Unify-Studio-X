package eleeter.unifystudiox.assets.browser;

import java.util.List;

public class AssetBrowserSection
{
    private final String id;
    private final String title;
    private final List<AssetBrowserItem> items;

    public AssetBrowserSection(String id, String title, List<AssetBrowserItem> items)
    {
        this.id = id;
        this.title = title;
        this.items = List.copyOf(items);
    }

    public String getId()
    {
        return this.id;
    }

    public String getTitle()
    {
        return this.title;
    }

    public List<AssetBrowserItem> getItems()
    {
        return this.items;
    }
}
