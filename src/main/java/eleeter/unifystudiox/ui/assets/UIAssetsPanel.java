package eleeter.unifystudiox.ui.assets;

import eleeter.unifystudiox.assets.browser.AssetBrowserDataSource;
import eleeter.unifystudiox.assets.browser.AssetBrowserItem;
import eleeter.unifystudiox.assets.browser.AssetBrowserItemType;
import eleeter.unifystudiox.assets.browser.AssetBrowserSection;
import eleeter.unifystudiox.ui.ShapeDraw;
import eleeter.unifystudiox.ui.UIKey;
import eleeter.unifystudiox.ui.assets.placement.AssetPlacementController;
import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.layout.GridLayout;
import eleeter.unifystudiox.ui.framework.layout.UIRect;
import eleeter.unifystudiox.ui.framework.render.UIBoxMath;
import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.menu.IContextMenuProvider;
import eleeter.unifystudiox.ui.menu.UIContextMenu;
import eleeter.unifystudiox.ui.model_editor.UIModelEditSpace;
import eleeter.unifystudiox.ui.theme.UIDropShadow;
import eleeter.unifystudiox.ui.widgets.UILabel;
import eleeter.unifystudiox.ui.widgets.UIScrollContainer;
import java.util.ArrayList;
import java.util.List;

public class UIAssetsPanel extends UIPanel implements IContextMenuProvider
{
    private static final float HEADER_H = 38.0F;
    private static final float FOOTER_H = 18.0F;
    private static final float BORDER = 1.0F;

    private static final float TILE_PADDING = 14.0F;
    private static final float TILE_SIZE = 156.0F;
    private static final float TILE_TOTAL_H = TILE_SIZE + 22.0F + 18.0F + 8.0F;
    private static final float SECTION_HEADER_H = 24.0F;

    private float panelWidth = 800.0F;
    private float panelHeight = 500.0F;

    private float panelOffsetX = 0.0F;
    private float panelOffsetY = 0.0F;

    private float baseX = 0.0F;
    private float baseY = 0.0F;
    private boolean baseInitialized = false;

    private boolean isDragging = false;
    private boolean isResizing = false;
    private float dragStartMouseX = 0.0F;
    private float dragStartMouseY = 0.0F;
    private float dragStartOffsetX = 0.0F;
    private float dragStartOffsetY = 0.0F;
    private float dragStartW = 0.0F;
    private float dragStartH = 0.0F;
    private float gripHoverProgress = 0.0F;

    private boolean lastToggleKeyState;
    private final AssetBrowserDataSource browserDataSource;
    private final ModelPreviewRenderer previewRenderer;
    private final UILabel titleLabel;
    private final UIScrollContainer content;

    private UIDropShadow shadow;

    private int lastBrowserRevision = -1;
    private int lastColumns = -1;
    private float lastContentWidth = -1.0F;

    private AssetPlacementController placementController;
    private boolean wasVisibleBeforePlacement = false;
    private boolean isAutoHidden = false;

    /**
     * Sets up the panel with the given data source
     */
    public UIAssetsPanel(AssetBrowserDataSource browserDataSource)
    {
        super("assets_panel");
        this.browserDataSource = browserDataSource;
        this.previewRenderer = new ModelPreviewRenderer();

        this.content = new UIScrollContainer("assets_panel_content");
        this.content.setScrollbarWidth(6.0F);
        this.content.setScrollSpeed(40.0F);
        this.content.setZIndex(20);

        this.addChild(this.content);

        this.setBlocksInput(true);
        this.setVisible(false);
        this.setZIndex(20);

        this.getTransform().set(0.0F, 0.0F, 1.0F, 1.0F);

        this.titleLabel = new UILabel("assets_panel_title");
        this.titleLabel.setText("Asset Browser");
        this.titleLabel.setAlignment(UILabel.Align.LEFT);
        this.titleLabel.setTextColor(0.88F, 0.91F, 0.98F, 1.0F);

        this.titleLabel.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F).setPixelOffset(20, 10).setPixelSize(-44, (int) HEADER_H - 14);

        this.addChild(this.titleLabel);
    }

    /*
     * Well, we need to override this because we can drag and resize the window freely.
     */
    @Override
    public void updateLayout(float parentX, float parentY, float parentW, float parentH)
    {
        if (!this.baseInitialized)
        {
            this.baseX = parentX + (parentW - this.panelWidth) * 0.5F;
            this.baseY = parentY + (parentH - this.panelHeight) * 0.5F;
            this.baseInitialized = true;
        }

        super.updateLayout(parentX, parentY, parentW, parentH);

        this.cx = this.baseX + this.panelOffsetX;
        this.cy = this.baseY + this.panelOffsetY;
        this.cw = this.panelWidth;
        this.ch = this.panelHeight;

        this.titleLabel.markDirty();
        this.titleLabel.updateLayout(this.cx, this.cy, this.cw, this.ch);

        float contentX = this.cx + BORDER;
        float contentY = this.cy + HEADER_H;
        float contentW = this.cw - BORDER * 2.0F;
        float contentH = this.ch - HEADER_H - FOOTER_H - BORDER;

        this.content.markDirty();
        this.content.updateLayout(contentX, contentY, contentW, contentH);
    }

    @Override
    public boolean containsPoint(float x, float y)
    {
        if (!isVisible() || !isEnabled())
        {
            return false;
        }

        return UIRect.contains(x, y, this.cx, this.cy, this.cw, this.ch);
    }

    @Override
    public void render(UIRenderer renderer)
    {
        if (!isVisible())
        {
            return;
        }

        renderSelf(renderer);

        this.content.render(renderer);
        this.titleLabel.render(renderer);
    }

    @Override
    public void updateLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        boolean toggleKeyDown = context.isKeyPressed(UIKey.M);
        if (toggleKeyDown && !this.lastToggleKeyState)
        {
            this.setVisible(!this.isVisible());
        }
        this.lastToggleKeyState = toggleKeyDown;

        if (this.placementController != null)
        {
            this.placementController.update(context);
            boolean placementActive = this.placementController.isActive();

            if (placementActive && !this.isAutoHidden)
            {
                this.wasVisibleBeforePlacement = this.isVisible();
                this.setVisible(false);
                this.isAutoHidden = true;
            } else if (!placementActive && this.isAutoHidden)
            {
                this.setVisible(this.wasVisibleBeforePlacement);
                this.isAutoHidden = false;
            }
        }

        if (this.isVisible())
        {
            super.updateLogic(context, deltaTime);
        }
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        float px = this.cx;
        float py = this.cy;
        float pw = this.cw;
        float ph = this.ch;

        float contentX = px + BORDER;
        float contentY = py + HEADER_H;
        float contentW = pw - BORDER * 2.0F;
        float contentH = ph - HEADER_H - FOOTER_H - BORDER;


        this.content.markDirty();
        float viewportX = this.content.getViewport().x;
        float viewportY = this.content.getViewport().y;
        float viewportW = this.content.getViewport().w;
        float viewportH = this.content.getViewport().h;

        float gripX = px + pw - 14.0F - 6.0F;
        float gripY = py + ph - FOOTER_H;
        boolean isOverGrip = context.getMouseX() >= gripX && context.getMouseX() <= px + pw && context.getMouseY() >= gripY && context.getMouseY() <= py + ph;

        float gripTarget = isOverGrip || this.isResizing ? 1.0F : 0.0F;
        this.gripHoverProgress = approach(this.gripHoverProgress, gripTarget, (float) deltaTime * 8.0F);

        boolean isOverHeader = context.getMouseX() >= px + 10.0F && context.getMouseX() <= px + pw - 10.0F && context.getMouseY() >= py + 4.0F && context.getMouseY() <= py + HEADER_H - 4.0F;

        if (context.isClicked(this) && isOverGrip && !this.isDragging)
        {
            this.isResizing = true;
            this.dragStartMouseX = context.getMouseX();
            this.dragStartMouseY = context.getMouseY();
            this.dragStartW = this.panelWidth;
            this.dragStartH = this.panelHeight;
        }

        if (this.isResizing)
        {
            if (context.isMouseDown())
            {
                this.panelWidth = UIBoxMath.calculateResize(this.dragStartW, this.dragStartMouseX, context.getMouseX(), 380.0F, 2000.0F);

                this.panelHeight = UIBoxMath.calculateResize(this.dragStartH, this.dragStartMouseY, context.getMouseY(), 280.0F, 2000.0F);
                this.markDirty();
                this.lastContentWidth = -1.0F;
            } else
            {
                this.isResizing = false;
            }
        }

        if (context.isClicked(this) && isOverHeader && !this.isResizing)
        {
            this.isDragging = true;
            this.dragStartMouseX = context.getMouseX();
            this.dragStartMouseY = context.getMouseY();
            this.dragStartOffsetX = this.panelOffsetX;
            this.dragStartOffsetY = this.panelOffsetY;
        }

        if (this.isDragging)
        {
            if (context.isMouseDown())
            {
                this.panelOffsetX = UIBoxMath.calculateDrag(this.dragStartOffsetX, this.dragStartMouseX, context.getMouseX());
                this.panelOffsetY = UIBoxMath.calculateDrag(this.dragStartOffsetY, this.dragStartMouseY, context.getMouseY());
                this.markDirty();
            } else
            {
                this.isDragging = false;
            }
        }

        this.browserDataSource.refresh();
        this.previewRenderer.syncAssets(this.browserDataSource.getPreviewModels());
        updatePreviewInteraction(context, viewportX, viewportY, viewportW, viewportH);
        this.previewRenderer.renderPending(isMouseInsideContent(context, viewportX, viewportY, viewportW, viewportH) ? 12 : 2);

        int revision = this.browserDataSource.getRevision();
        float usableW = Math.max(1.0F, contentW - 6.0F - TILE_PADDING);
        int columns = computeColumnCount(usableW);
        boolean layoutChanged = Math.abs(usableW - this.lastContentWidth) > 0.5F || columns != this.lastColumns;

        if (revision != this.lastBrowserRevision || layoutChanged)
        {
            rebuildBrowserContent(this.browserDataSource.getSections(), columns, usableW);
            this.lastBrowserRevision = revision;
            this.lastColumns = columns;
            this.lastContentWidth = usableW;
        }
        for (UIElement child : this.content.getChildren())
        {
            if (child instanceof AssetBrowserTile)
            {
                ((AssetBrowserTile) child).setClipRect(contentX, contentY, contentW, contentH);
            }
        }
    }

    @Override
    public void collectInteractable(List<UIElement> out)
    {
        if (!isVisible() || !isEnabled())
        {
            return;
        }


        this.titleLabel.collectInteractable(out);

        this.content.collectInteractable(out);

        if (this.getBlocksInput())
        {
            out.add(this);
        }
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        float x = this.cx;
        float y = this.cy;
        float w = this.cw;
        float h = this.ch;

        this.shadow.drawRounded(renderer, x, y + 4.0F, w, h, 0.0F, 7.0F, 0.50F, 14.0F);

        renderer.drawRoundedRect(x, y, w, h, 0.13F, 0.14F, 0.17F, 1.0F, 13.0F);

        renderer.drawRoundedRect(x + BORDER, y + BORDER, w - BORDER * 2.0F, h - BORDER * 2.0F, 0.10F, 0.11F, 0.13F, 0.97F, 12.0F);

        float hdrR = this.isDragging ? 0.17F : 0.13F;
        float hdrG = this.isDragging ? 0.20F : 0.16F;
        float hdrB = this.isDragging ? 0.25F : 0.20F;
        renderer.drawRoundedRect(x + BORDER, y + BORDER, w - BORDER * 2.0F, HEADER_H, hdrR, hdrG, hdrB, 1.0F, 11.0F);

        renderer.drawRoundedRect(x + BORDER, y + BORDER, w - BORDER * 2.0F, 1.5F, 0.55F, 0.57F, 0.65F, 0.16F, 1.0F);

        renderer.drawRect(x + BORDER, y + HEADER_H, w - BORDER * 2.0F, 1.0F, 0.22F, 0.25F, 0.30F, 0.65F);

        renderer.drawRoundedRect(x + BORDER + 1.0F, y + BORDER + 8.0F, 3.0F, h - BORDER * 2.0F - 16.0F, 0.28F, 0.52F, 0.98F, 0.80F, 1.5F);

        renderer.drawRoundedRect(x + BORDER, y + h - FOOTER_H, w - BORDER * 2.0F, FOOTER_H - BORDER, 0.09F, 0.10F, 0.12F, 1.0F, 3.0F);

        renderer.drawRect(x + BORDER, y + h - FOOTER_H, w - BORDER * 2.0F, 1.0F, 0.20F, 0.22F, 0.27F, 0.45F);

        float dotY = y + HEADER_H * 0.5F - 1.5F;
        float dotStartX = x + w * 0.5F - 14.0F;
        float dotAlpha = this.isDragging ? 0.60F : 0.22F;
        for (int i = 0; i < 3; i++)
        {
            renderer.drawRoundedRect(dotStartX + (float) i * 14.0F, dotY, 5.0F, 3.0F, 0.62F, 0.65F, 0.78F, dotAlpha, 1.5F);
        }

        float gr = lerp(0.55F, 0.28F, this.gripHoverProgress);
        float gg = lerp(0.58F, 0.52F, this.gripHoverProgress);
        float gb = lerp(0.68F, 0.98F, this.gripHoverProgress);
        float ga = lerp(0.28F, 0.90F, this.gripHoverProgress);

        float gx = x + w - 5.0F;
        float gy = y + h - 4.0F;
        renderer.drawRect(gx - 14.0F, gy - 2.0F, 10.0F, 2.0F, gr, gg, gb, ga);
        renderer.drawRect(gx - 2.0F, gy - 14.0F, 2.0F, 10.0F, gr, gg, gb, ga);
        renderer.drawRect(gx - 9.0F, gy - 2.0F, 2.0F, 2.0F, gr, gg, gb, ga * 0.50F);
        renderer.drawRect(gx - 2.0F, gy - 9.0F, 2.0F, 2.0F, gr, gg, gb, ga * 0.50F);
        renderer.drawRect(gx - 4.0F, gy - 2.0F, 2.0F, 2.0F, gr, gg, gb, ga * 0.25F);
        renderer.drawRect(gx - 2.0F, gy - 4.0F, 2.0F, 2.0F, gr, gg, gb, ga * 0.25F);
    }

    public void cleanupResources()
    {
        this.previewRenderer.cleanup();
    }

    public void setPlacementController(AssetPlacementController controller)
    {
        this.placementController = controller;
    }

    private void clearTileChildren()
    {
        List<UIElement> toRemove = new ArrayList<>();

        for (UIElement child : this.content.getChildren())
        {
            toRemove.add(child);
        }

        for (UIElement child : toRemove)
        {
            this.content.removeChild(child);
        }
    }

    private void rebuildBrowserContent(List<AssetBrowserSection> sections, int columns, float contentWidth)
    {
        clearTileChildren();

        float cursorY = TILE_PADDING;

        for (AssetBrowserSection section : sections)
        {
            this.content.addChild(createSectionHeader(section, contentWidth, cursorY));

            cursorY += SECTION_HEADER_H + 6.0F;

            List<AssetBrowserItem> items = section.getItems();

            List<GridLayout.TileSlot> slots = GridLayout.layout(items.size(), columns, TILE_SIZE, TILE_TOTAL_H, TILE_PADDING, cursorY);

            for (int i = 0; i < items.size(); i++)
            {
                GridLayout.TileSlot slot = slots.get(i);

                AssetBrowserItem item = items.get(i);

                AssetBrowserTile tile = new AssetBrowserTile("asset_browser_tile_" + section.getId() + "_" + item.getId(), item, this.previewRenderer);

                tile.setZIndex(21);

                tile.getTransform().set(0.0F, 0.0F, 0.0F, 0.0F).setPixelOffset((int) slot.x(), (int) slot.y()).setPixelSize((int) TILE_SIZE, (int) TILE_TOTAL_H);

                tile.setOnClick(this::onTileClicked);

                this.content.addChild(tile);
            }

            cursorY += GridLayout.rowsHeight(items.size(), columns, TILE_TOTAL_H, TILE_PADDING) + 20.0F;
        }

        float totalContentHeight = cursorY + TILE_PADDING;

        this.content.setContentHeight(totalContentHeight);
    }

    private UILabel createSectionHeader(AssetBrowserSection section, float contentWidth, float y)
    {
        UILabel header = new UILabel("asset_browser_header_" + section.getId());
        header.setText(section.getTitle());
        header.setAlignment(UILabel.Align.LEFT);
        header.setTextColor(0.52F, 0.58F, 0.72F, 1.0F);
        header.getTransform().set(0.0F, 0.0F, 0.0F, 0.0F).setPixelOffset((int) TILE_PADDING, (int) y).setPixelSize((int) Math.max(1.0F, contentWidth - TILE_PADDING), (int) SECTION_HEADER_H);
        return header;
    }

    private void updatePreviewInteraction(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, float contentX, float contentY, float contentW, float contentH)
    {
        if (!isMouseInsideContent(context, contentX, contentY, contentW, contentH))
        {
            return;
        }

        float deltaX = context.getMouseDeltaX();
        float deltaY = context.getMouseDeltaY();

        if (Math.abs(deltaX) < 0.01F && Math.abs(deltaY) < 0.01F)
        {
            return;
        }

        this.previewRenderer.addPreviewRotationDelta(deltaX * 0.35F, deltaY * 0.35F);
    }

    private boolean isMouseInsideContent(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, float x, float y, float w, float h)
    {
        return UIRect.contains(context.getMouseX(), context.getMouseY(), x, y, w, h);
    }

    private int computeColumnCount(float width)
    {
        return GridLayout.computeColumns(width, TILE_SIZE, TILE_PADDING);
    }

    private void onTileClicked(AssetBrowserItem item)
    {
        if (this.placementController == null || item.getModelAsset() == null)
        {
            return;
        }
        this.placementController.beginPlacement(item.getModelAsset());
    }


    @Override
    public UIContextMenu getContextMenu(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        UIElement hovered = context.getHoveredElement();
        AssetBrowserTile targetTile = null;
        while (hovered != null)
        {
            if (hovered instanceof AssetBrowserTile)
            {
                targetTile = (AssetBrowserTile) hovered;
                break;
            }
            hovered = hovered.getParent();
        }

        if (targetTile == null)
        {
            return null;
        }

        AssetBrowserItem item = targetTile.getItem();
        if (item.getType() != AssetBrowserItemType.MODEL)
        {
            return null;
        }

        UIContextMenu menu = new UIContextMenu();
        menu.addItem("Edit Model", ShapeDraw::drawSettingsIcon, () ->
        {
            List<UIModelEditSpace> editSpaces = this.getParent().findChildrenByClass(UIModelEditSpace.class);

            if (!editSpaces.isEmpty())
            {
                editSpaces.get(0).setModel(item.getModelAsset());
            }
        });

        return menu;
    }

    private static float approach(float current, float target, float step)
    {
        return current < target ? Math.min(current + step, target) : Math.max(current - step, target);
    }

    private static float lerp(float a, float b, float t)
    {
        return a + (b - a) * t;
    }
}