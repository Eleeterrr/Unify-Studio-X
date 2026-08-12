package eleeter.unifystudiox.ui.browser;

import java.util.ArrayList;
import java.util.List;

import eleeter.unifystudiox.ui.UIKey;
import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;


public class UIBrowserPanel extends UIElement
{
    private static final float PANEL_H = 220.0F;
    private static final float BG_R = 0.09F;
    private static final float BG_G = 0.09F;
    private static final float BG_B = 0.10F;

    private final BrowserSource source;
    private final UIBrowserNavBar navBar;
    private final UIBrowserSidebar sidebar;
    private UIBrowserContentArea contentArea;
    private UIBrowserStatusBar statusBar;

    private final List<String> historyBack = new ArrayList<>();
    private final List<String> historyForward = new ArrayList<>();
    private String currentPath = "/";
    private String currentQuery = "";

    private boolean lastToggleKeyState = false;

    public UIBrowserPanel(String id, BrowserSource source, eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        super(id);
        this.source = source;
        this.setBlocksInput(true);
        this.setVisible(false);
        this.setZIndex(10);

        /* Bottom-docked, 80% wide, centred horizontally */
        this.getTransform().set(0.1F, 1.0F, 0.8F, 0.0F).setAnchor(0.0F, 1.0F).setPixelSize(0, (int) PANEL_H);

        float sideW = UIBrowserSidebar.getWidth();
        float navH = UIBrowserNavBar.getHeight();
        float statusH = UIBrowserStatusBar.getHeight();

        this.navBar = new UIBrowserNavBar(id + "_nav", context);
        this.navBar.setOnBack(this::navigateBack);
        this.navBar.setOnForward(this::navigateForward);
        this.navBar.setOnUp(this::navigateUp);
        this.navBar.setOnToggleView(() -> this.contentArea.setGridView(this.navBar.isGridView()));
        this.navBar.setOnSearchChanged(this::onSearchChanged);
        this.addChild(this.navBar);

        this.sidebar = new UIBrowserSidebar(id + "_sidebar");
        this.sidebar.getTransform().set(0.0F, 0.0F, 0.0F, 1.0F).setPixelOffset(0, (int) navH).setPixelSize((int) sideW, (int) -(navH + statusH));
        this.sidebar.setOnNavigate(this::navigateTo);
        this.addChild(this.sidebar);

        this.contentArea = new UIBrowserContentArea(id + "_content");
        this.contentArea.getTransform()
                .set(0.0F, 0.0F, 1.0F, 1.0F)
                .setPixelOffset((int) sideW, (int) navH)
                .setPixelSize((int) -sideW, (int) -(navH + statusH));
        this.contentArea.setOnSelect(item ->
        {
            this.statusBar.setSelectedItem(item);
        });
        this.contentArea.setOnNavigate(item ->
        {
            if (this.source.canNavigate(item))
            {
                navigateTo(this.currentPath.equals("/") ? "/" + item.getName() : this.currentPath + "/" + item.getName());
            }
        });
        this.addChild(this.contentArea);

        this.statusBar = new UIBrowserStatusBar(id + "_status");
        this.addChild(this.statusBar);

        refreshContent();
        this.sidebar.setBookmarks(this.source.getBookmarks());
    }

    @Override
    public void updateLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        boolean toggleKeyDown = context.isKeyPressed(UIKey.B);
        if (toggleKeyDown && !this.lastToggleKeyState)
        {
            this.setVisible(!this.isVisible());
        }
        this.lastToggleKeyState = toggleKeyDown;

        if (this.isVisible())
        {
            super.updateLogic(context, deltaTime);
        }
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
    }

    private void navigateTo(String path)
    {
        this.historyBack.add(this.currentPath);
        this.historyForward.clear();
        this.currentPath = path;
        this.currentQuery = "";
        refreshContent();
    }

    private void navigateBack()
    {
        if (this.historyBack.isEmpty())
        {
            return;
        }
        this.historyForward.add(this.currentPath);
        this.currentPath = this.historyBack.remove(this.historyBack.size() - 1);
        this.currentQuery = "";
        refreshContent();
    }

    private void navigateForward()
    {
        if (this.historyForward.isEmpty())
        {
            return;
        }
        this.historyBack.add(this.currentPath);
        this.currentPath = this.historyForward.remove(this.historyForward.size() - 1);
        this.currentQuery = "";
        refreshContent();
    }

    private void navigateUp()
    {
        if (this.currentPath.equals("/"))
        {
            return;
        }
        int lastSlash = this.currentPath.lastIndexOf('/');
        String parent = (lastSlash <= 0) ? "/" : this.currentPath.substring(0, lastSlash);
        navigateTo(parent);
    }

    private void onSearchChanged(String query)
    {
        this.currentQuery = query;
        refreshContent();
    }

    private void refreshContent()
    {
        List<BrowserItem> items = this.source.search(this.currentPath, this.currentQuery);
        this.contentArea.setItems(items);
        this.statusBar.setItemCount(items.size());
        this.statusBar.setSelectedItem(null);
        this.navBar.setBreadcrumb(buildBreadcrumb());
    }

    private String buildBreadcrumb()
    {
        if (this.currentPath.equals("/"))
        {
            return this.source.getDisplayName("/");
        }
        StringBuilder sb = new StringBuilder(this.source.getDisplayName("/"));
        String[] parts = this.currentPath.split("/");
        for (String part : parts)
        {
            if (!part.isEmpty())
            {
                sb.append("  >  ").append(part);
            }
        }
        return sb.toString();
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        float x = this.getComputedX();
        float y = this.getComputedY();
        float w = this.getComputedWidth();
        float h = this.getComputedHeight();

        /* Panel border */
        renderer.drawRect(x, y, w, h, 0.20F, 0.20F, 0.22F, 1.0F);

        /* Panel background */
        renderer.drawRect(x + 1.0F, y + 1.0F, w - 2.0F, h - 2.0F, BG_R, BG_G, BG_B, 1.0F);

        /* Subtle top accent line */
        renderer.drawRect(x, y, w, 1.5F, 0.28F, 0.45F, 0.80F, 0.9F);
    }
}
