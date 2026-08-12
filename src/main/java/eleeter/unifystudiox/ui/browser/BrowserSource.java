package eleeter.unifystudiox.ui.browser;

import java.util.List;


public interface BrowserSource
{
    /** Returns all items at the given path string */
    List<BrowserItem> getItems(String path);

    boolean canNavigate(BrowserItem item);

    List<BrowserItem> search(String path, String query);

    String getDisplayName(String path);

    List<BrowserItem> getBookmarks();
}
