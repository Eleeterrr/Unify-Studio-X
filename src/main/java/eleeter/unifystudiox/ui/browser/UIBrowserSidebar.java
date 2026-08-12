package eleeter.unifystudiox.ui.browser;

import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.widgets.UILabel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class UIBrowserSidebar extends UIElement
{
    private static final float WIDTH = 140.0F;
    private static final float ROW_H = 26.0F;
    private static final float BG_R = 0.085F;
    private static final float BG_G = 0.085F;
    private static final float BG_B = 0.095F;
    private static final float HOVER_COLOR = 0.16F;

    private final List<BrowserItem> bookmarks = new ArrayList<>();
    private final List<UILabel> labels = new ArrayList<>();
    private int hoveredIndex = -1;
    private Consumer<String> onNavigate;

    public UIBrowserSidebar(String id)
    {
        super(id);
        this.getTransform().set(0.0F, 0.0F, 0.0F, 1.0F).setPixelSize((int) WIDTH, 0);
    }

    public void setBookmarks(List<BrowserItem> items)
    {
        this.bookmarks.clear();
        this.clearChildren();
        this.labels.clear();

        float cursorY = 8.0F;
        for (int i = 0; i < items.size(); i++)
        {
            BrowserItem item = items.get(i);
            UILabel label = new UILabel(this.getId() + "_bm_" + i);
            label.setText(item.getName());
            label.setAlignment(UILabel.Align.LEFT);
            label.setTextColor(0.78F, 0.80F, 0.82F, 1.0F);
            label.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F).setPixelOffset(22, (int) cursorY).setPixelSize(-6, (int) ROW_H);
            this.addChild(label);
            this.labels.add(label);
            this.bookmarks.add(item);
            cursorY += ROW_H;
        }
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        float x = this.getComputedX();
        float y = this.getComputedY();
        float localY = context.getMouseY() - y - 8.0F;

        if (context.getMouseX() >= x && context.getMouseX() < x + WIDTH && localY >= 0.0F)
        {
            this.hoveredIndex = (int) (localY / ROW_H);
            if (this.hoveredIndex >= this.bookmarks.size())
            {
                this.hoveredIndex = -1;
            }
        } else
        {
            this.hoveredIndex = -1;
        }

        if (this.hoveredIndex != -1 && context.isMousePressed() && this.onNavigate != null)
        {
            String path = this.bookmarks.get(this.hoveredIndex).getMeta("path");
            if (!path.isEmpty())
            {
                this.onNavigate.accept(path);
            }
        }
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        float x = this.getComputedX();
        float y = this.getComputedY();
        float w = this.getComputedWidth();
        float h = this.getComputedHeight();

        renderer.drawRect(x, y, w, h, BG_R, BG_G, BG_B, 1.0F);
        renderer.drawRect(x + w - 1.0F, y, 1.0F, h, 1.0F, 1.0F, 1.0F, 0.04F);

        float cursorY = y + 8.0F;
        for (int i = 0; i < this.bookmarks.size(); i++)
        {
            if (i == this.hoveredIndex)
            {
                renderer.drawRoundedRect(x + 4.0F, cursorY, w - 8.0F, ROW_H, HOVER_COLOR, HOVER_COLOR, HOVER_COLOR, 1.0F, 4.0F);
            }

            float iconColor = getIconColor(this.bookmarks.get(i).getType());
            renderer.drawRoundedRect(x + 6.0F, cursorY + 6.0F, 10.0F, 10.0F, iconColor, iconColor * 0.6F, 0.3F, 1.0F, 2.0F);

            cursorY += ROW_H;
        }
    }

    private float getIconColor(BrowserItemType type)
    {
        if (type == BrowserItemType.MODEL)
        {
            return 0.35F;
        }

        if (type == BrowserItemType.TEXTURE)
        {
            return 0.60F;
        }

        if (type == BrowserItemType.SCENE)
        {
            return 0.25F;
        }

        return 0.45F;
    }

    public void setOnNavigate(Consumer<String> callback)
    {
        this.onNavigate = callback;
    }

    public static float getWidth()
    {
        return WIDTH;
    }
}
