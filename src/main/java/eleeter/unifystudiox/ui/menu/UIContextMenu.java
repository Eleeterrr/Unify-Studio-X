package eleeter.unifystudiox.ui.menu;

import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.layout.Axis;
import eleeter.unifystudiox.ui.framework.layout.UIStackLayout;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.theme.UIDropShadow;
import eleeter.unifystudiox.ui.theme.UITheme;

public class UIContextMenu extends UIElement
{
    private static final float MIN_WIDTH = 200.0F;
    private final UIStackLayout listLayout;
    private Runnable onCloseCallback;
    private float animProgress = 0.0F;
    private static final float ANIM_DURATION = 0.12F;

    private static float easeOut(float t)
    {
        float inv = 1.0F - t;
        return 1.0F - inv * inv * inv;
    }

    public UIContextMenu()
    {
        super("context_menu_root");
        this.setZIndex(10000);
        this.setBlocksInput(true);
        this.setPausesGame(false);

        this.getTransform().set(0.0F, 0.0F, 0.0F, 0.0F);

        this.listLayout = new UIStackLayout(this.getId() + "_list", Axis.VERTICAL);
        this.listLayout.getTransform().set(0.0F, 0.0F, 1.0F, 1.0F).setPixelOffset(0, 4).setPixelSize(0, -8);
        this.addChild(this.listLayout);
    }

    public UIContextMenu addItem(String title, IconDrawer icon, Runnable action)
    {
        ContextMenuItem item = new ContextMenuItem(
                this.getId() + "_item_" + this.listLayout.getChildren().size(), title, action);
        item.setIcon(icon);
        this.listLayout.addChild(item);
        this.recalculateSize();
        return this;
    }

    public UIContextMenu addDangerItem(String title, IconDrawer icon, Runnable action)
    {
        ContextMenuItem item = new ContextMenuItem(
                this.getId() + "_item_" + this.listLayout.getChildren().size(), title, action);
        item.setIcon(icon);
        item.setDanger(true);
        this.listLayout.addChild(item);
        this.recalculateSize();
        return this;
    }

    public UIContextMenu addSeparator()
    {
        ContextMenuSeparator sep = new ContextMenuSeparator(this.getId() + "_sep_" + this.listLayout.getChildren().size());
        this.listLayout.addChild(sep);
        this.recalculateSize();
        return this;
    }

    private void recalculateSize()
    {
        float width = MIN_WIDTH;
        float height = 8.0F;

        for (UIElement child : this.listLayout.getChildren())
        {
            if (child instanceof ContextMenuItem)
            {
                height += 22.0F;
            }
            else if (child instanceof ContextMenuSeparator)
            {
                height += 6.0F;
            }
            else
            {
                height += child.getTransform().computedHeight > 0 ? child.getTransform().computedHeight : 22.0F;
            }
        }

        this.getTransform().setPixelSize((int) width, (int) height);
        this.markDirty();
    }

    public void show(float x, float y, float screenW, float screenH)
    {
        float w = this.getTransform().getPixelWidth();
        float h = this.getTransform().getPixelHeight();

        if (x + w > screenW - 8.0F) x = screenW - w - 8.0F;
        if (y + h > screenH - 8.0F) y = screenH - h - 8.0F;

        x = Math.max(8.0F, x);
        y = Math.max(8.0F, y);

        this.animProgress = 0.0F;
        this.getTransform().setPixelOffset((int) x, (int) y);
        this.setVisible(true);
        this.markDirty();
    }

    public void setOnClose(Runnable onClose)
    {
        this.onCloseCallback = onClose;
    }

    public void close()
    {
        this.setVisible(false);
        if (this.onCloseCallback != null)
        {
            this.onCloseCallback.run();
        }
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        if (this.animProgress < 1.0F)
        {
            this.animProgress = Math.min(1.0F, this.animProgress + (float) (deltaTime / ANIM_DURATION));
            this.markDirty();
        }
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        float x = this.getComputedX();
        float y = this.getComputedY();
        float w = this.getComputedWidth();
        float h = this.getComputedHeight();

        float alpha = easeOut(this.animProgress);

        UITheme.Theme theme = UITheme.get();
        float[] bg     = theme.surface();
        float[] border = theme.border();
        float radius   = 6.0F;

        UIDropShadow.drawRounded(renderer, x, y, w, h, 0.0F, 6.0F, 0.4F * alpha, radius);
        renderer.drawRoundedRect(x, y, w, h, bg[0], bg[1], bg[2], 0.92F * alpha, radius);
        renderer.drawRoundedRect(x, y, w, h, border[0], border[1], border[2], 0.18F * alpha, radius);
    }
}
