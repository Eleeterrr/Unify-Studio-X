package eleeter.unifystudiox.ui.browser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class BrowserItem
{
    private final String name;
    private final BrowserItemType type;
    private final int iconId;
    private final Map<String, String> metadata;

    public BrowserItem(String name, BrowserItemType type, int iconId)
    {
        this.name = name;
        this.type = type;
        this.iconId = iconId;
        this.metadata = new HashMap<>();
    }

    public BrowserItem withMeta(String key, String value)
    {
        this.metadata.put(key, value);
        return this;
    }

    public String getName()
    {
        return this.name;
    }

    public BrowserItemType getType()
    {
        return this.type;
    }

    public int getIconId()
    {
        return this.iconId;
    }

    public Map<String, String> getMetadata()
    {
        return Collections.unmodifiableMap(this.metadata);
    }

    public String getMeta(String key)
    {
        return this.metadata.getOrDefault(key, "");
    }
}
