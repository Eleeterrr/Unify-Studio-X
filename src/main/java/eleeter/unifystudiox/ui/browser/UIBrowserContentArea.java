package eleeter.unifystudiox.ui.browser;

import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.widgets.UIScrollPanel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class UIBrowserContentArea extends UIElement
{
    private static final float GRID_TILE = 96.0F;
    private static final float GRID_LABEL_H = 18.0F;
    private static final float GRID_GAP = 10.0F;
    private static final float GRID_PADDING = 12.0F;
    private static final float LIST_ROW_H = 22.0F;
    private static final float LIST_PADDING = 8.0F;

    private final UIScrollPanel scroll;
    private final List<BrowserItem> items = new ArrayList<>();
    private boolean isGridView = true;
    private int selectedIndex = -1;
    private int hoveredIndex = -1;

    private Consumer<BrowserItem> onSelect;
    private Consumer<BrowserItem> onNavigate;

    public UIBrowserContentArea(String id)
    {
        super(id);
        this.getTransform().set(0.0F, 0.0F, 1.0F, 1.0F);

        this.scroll = new UIScrollPanel(id + "_scroll");
        this.scroll.getTransform().set(0.0F, 0.0F, 1.0F, 1.0F);
        this.addChild(this.scroll);
    }

    public void setItems(List<BrowserItem> newItems)
    {
        this.items.clear();
        this.items.addAll(newItems);
        this.selectedIndex = -1;
        this.hoveredIndex = -1;
        rebuildScrollHeight();
    }

    public void setGridView(boolean isGrid)
    {
        this.isGridView = isGrid;
        this.selectedIndex = -1;
        rebuildScrollHeight();
    }

    private void rebuildScrollHeight()
    {
        float contentW = Math.max(1.0F, this.scroll.getComputedWidth());
        float totalH;
        if (this.isGridView)
        {
            int cols = computeGridColumns(contentW);
            int rows = this.items.isEmpty() ? 0 : ((this.items.size() + cols - 1) / cols);
            totalH = GRID_PADDING + rows * (GRID_TILE + GRID_LABEL_H + GRID_GAP) + GRID_PADDING;
        } else
        {
            totalH = LIST_PADDING + this.items.size() * LIST_ROW_H + LIST_PADDING;
        }
        this.scroll.setMaxContentHeight(Math.max(this.scroll.getComputedHeight(), totalH));
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        float x = this.scroll.getComputedX();
        float y = this.scroll.getComputedY();
        float w = this.scroll.getComputedWidth();

        this.hoveredIndex = -1;

        if (this.isGridView)
        {
            int cols = computeGridColumns(w);
            for (int i = 0; i < this.items.size(); i++)
            {
                float[] bounds = getGridBounds(i, cols, x, y);
                if (containsPoint(context.getMouseX(), context.getMouseY(), bounds))
                {
                    this.hoveredIndex = i;
                    break;
                }
            }
        } else
        {
            float localY = context.getMouseY() - y - LIST_PADDING;
            if (localY >= 0.0F && context.getMouseX() >= x && context.getMouseX() < x + w)
            {
                int idx = (int) (localY / LIST_ROW_H);
                if (idx >= 0 && idx < this.items.size())
                {
                    this.hoveredIndex = idx;
                }
            }
        }

        if (this.hoveredIndex != -1 && context.isMousePressed())
        {
            this.selectedIndex = this.hoveredIndex;
            if (this.onSelect != null)
            {
                this.onSelect.accept(this.items.get(this.selectedIndex));
            }
        }

        if (this.hoveredIndex != -1 && context.isDoubleClicked(this.scroll) && this.onNavigate != null)
        {
            this.onNavigate.accept(this.items.get(this.hoveredIndex));
        }

        rebuildScrollHeight();
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        float x = this.scroll.getComputedX();
        float y = this.scroll.getComputedY();
        float w = this.scroll.getComputedWidth();

        renderer.drawRect(this.getComputedX(), this.getComputedY(), this.getComputedWidth(), this.getComputedHeight(), 0.09F, 0.09F, 0.10F, 1.0F);

        if (this.isGridView)
        {
            renderGrid(renderer, x, y, w);
        } else
        {
            renderList(renderer, x, y, w);
        }
    }

    private void renderGrid(UIRenderer renderer, float x, float y, float w)
    {
        int cols = computeGridColumns(w);
        for (int i = 0; i < this.items.size(); i++)
        {
            float[] bounds = getGridBounds(i, cols, x, y);
            boolean isSelected = (i == this.selectedIndex);
            boolean isHovered = (i == this.hoveredIndex);

            if (isSelected)
            {
                renderer.drawRoundedRect(bounds[0] - 2.0F, bounds[1] - 2.0F, bounds[2] + 4.0F, bounds[3] + 4.0F, 0.20F, 0.35F, 0.60F, 1.0F, 6.0F);
            } else if (isHovered)
            {
                renderer.drawRoundedRect(bounds[0] - 2.0F, bounds[1] - 2.0F, bounds[2] + 4.0F, bounds[3] + 4.0F, 0.18F, 0.18F, 0.20F, 1.0F, 6.0F);
            }

            float[] iconColor = getTypeColor(this.items.get(i).getType());
            renderer.drawRoundedRect(bounds[0], bounds[1], GRID_TILE, GRID_TILE, iconColor[0] * 0.3F, iconColor[1] * 0.3F, iconColor[2] * 0.3F, 1.0F, 8.0F);
            renderer.drawRoundedRect(bounds[0] + GRID_TILE * 0.25F, bounds[1] + GRID_TILE * 0.25F, GRID_TILE * 0.5F, GRID_TILE * 0.5F, iconColor[0], iconColor[1], iconColor[2], 0.85F, 6.0F);
        }
    }

    private void renderList(UIRenderer renderer, float x, float y, float w)
    {
        for (int i = 0; i < this.items.size(); i++)
        {
            float rowY = y + LIST_PADDING + i * LIST_ROW_H;
            boolean isSelected = (i == this.selectedIndex);
            boolean isHovered = (i == this.hoveredIndex);

            if (isSelected)
            {
                renderer.drawRect(x, rowY, w, LIST_ROW_H, 0.18F, 0.32F, 0.55F, 1.0F);
            } else if (isHovered)
            {
                renderer.drawRect(x, rowY, w, LIST_ROW_H, 0.14F, 0.14F, 0.16F, 1.0F);
            } else if (i % 2 == 0)
            {
                renderer.drawRect(x, rowY, w, LIST_ROW_H, 0.10F, 0.10F, 0.11F, 0.5F);
            }

            float[] iconColor = getTypeColor(this.items.get(i).getType());
            renderer.drawRoundedRect(x + 6.0F, rowY + 5.0F, 12.0F, 12.0F, iconColor[0], iconColor[1], iconColor[2], 0.9F, 3.0F);
        }
    }

    private float[] getGridBounds(int index, int cols, float baseX, float baseY)
    {
        int col = index % cols;
        int row = index / cols;
        float px = baseX + GRID_PADDING + col * (GRID_TILE + GRID_GAP);
        float py = baseY + GRID_PADDING + row * (GRID_TILE + GRID_LABEL_H + GRID_GAP);
        return new float[]{px, py, GRID_TILE, GRID_TILE + GRID_LABEL_H};
    }

    private int computeGridColumns(float width)
    {
        float cell = GRID_TILE + GRID_GAP;
        return Math.max(1, (int) Math.floor((width - GRID_PADDING * 2.0F) / cell));
    }

    private boolean containsPoint(float mx, float my, float[] bounds)
    {
        return mx >= bounds[0] && mx < bounds[0] + bounds[2] && my >= bounds[1] && my < bounds[1] + bounds[3];
    }

    private float[] getTypeColor(BrowserItemType type)
    {
        if (type == BrowserItemType.FOLDER)
        {
            return new float[]{0.85F, 0.65F, 0.25F};
        }

        if (type == BrowserItemType.MODEL)
        {
            return new float[]{0.30F, 0.55F, 0.90F};
        }

        if (type == BrowserItemType.TEXTURE)
        {
            return new float[]{0.35F, 0.75F, 0.45F};
        }

        if (type == BrowserItemType.AUDIO)
        {
            return new float[]{0.75F, 0.35F, 0.75F};
        }

        if (type == BrowserItemType.SCENE)
        {
            return new float[]{0.90F, 0.45F, 0.30F};
        }
        return new float[]{0.50F, 0.50F, 0.50F};
    }

    public BrowserItem getSelectedItem()
    {
        return (this.selectedIndex >= 0 && this.selectedIndex < this.items.size()) ? this.items.get(this.selectedIndex) : null;
    }

    public int getItemCount()
    {
        return this.items.size();
    }

    public void setOnSelect(Consumer<BrowserItem> callback)
    {
        this.onSelect = callback;
    }

    public void setOnNavigate(Consumer<BrowserItem> callback)
    {
        this.onNavigate = callback;
    }
}
