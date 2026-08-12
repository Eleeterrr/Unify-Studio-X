package eleeter.unifystudiox.ui.menu;

import eleeter.unifystudiox.settings.menu.MenuAction;
import eleeter.unifystudiox.settings.menu.MenuCategory;
import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.layout.Axis;
import eleeter.unifystudiox.ui.framework.layout.UIStackLayout;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.theme.UIDropShadow;
import eleeter.unifystudiox.ui.theme.UITheme;


public class UIMenuDropdown extends UIElement
{
    private static final float ROW_HEIGHT = 22.0F;
    private static final float MIN_WIDTH = 190.0F;

    private MenuCategory category = null;
    private final UIStackLayout listLayout;

    public UIMenuDropdown()
    {
        super("menu_dropdown");
        this.setVisible(false);
        this.setZIndex(1001);
        this.setBlocksInput(true);

        this.listLayout = new UIStackLayout(this.getId() + "_list", Axis.VERTICAL);
        this.listLayout.getTransform().set(0.0F, 0.0F, 1.0F, 1.0F).setPixelOffset(0, 2).setPixelSize(0, -4);
        this.addChild(this.listLayout);
    }

    public void setup(MenuCategory category, float x, float y)
    {
        this.category = category;
        float width = MIN_WIDTH;

        for (MenuAction action : category.getActions())
        {
            float estimatedWidth = (float) action.getTitle().length() * 8.5F + (float) action.getShortcut().length() * 8.0F + 40.0F;
            if (estimatedWidth > width)
            {
                width = estimatedWidth;
            }
        }

        float height = (float) category.getActions().size() * ROW_HEIGHT + 4.0F;
        this.getTransform().set(0.0F, 0.0F, 0.0F, 0.0F).setAnchor(0.0F, 0.0F).setPixelOffset((int) x, (int) y).setPixelSize((int) width, (int) height);

        this.markDirty();
        this.listLayout.clearChildren();

        for (int i = 0; i < category.getActions().size(); i++)
        {
            MenuAction action = category.getActions().get(i);
            UIMenuItem item = new UIMenuItem(this.getId() + "_item_" + i, action);
            item.getTransform().setPixelSize(0, (int) ROW_HEIGHT);
            this.listLayout.addChild(item);
        }
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        if (this.category == null)
        {
            return;
        }

        float x = this.getComputedX();
        float y = this.getComputedY();
        float w = this.getComputedWidth();
        float h = this.getComputedHeight();

        UITheme.Theme theme = UITheme.get();
        float[] bg = theme.surface();
        float[] border = theme.border();
        float radius = theme.radiusSm();

        /* 1. Draw Drop Shadow */
        UIDropShadow.drawRounded(renderer, x, y, w, h, 0.0F, 4.0F, 0.4F, radius);

        /* 2. Draw Background */
        renderer.drawRoundedRect(x, y, w, h, bg[0], bg[1], bg[2], 0.96F, radius);

        /* 3. Draw Border */
        renderer.drawRoundedRect(x - 1.0F, y - 1.0F, w + 2.0F, h + 2.0F, border[0], border[1], border[2], 0.2F, radius);
    }
}
