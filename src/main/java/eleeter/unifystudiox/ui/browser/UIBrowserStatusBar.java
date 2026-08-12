package eleeter.unifystudiox.ui.browser;


import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.widgets.UILabel;


public class UIBrowserStatusBar extends UIElement
{
    private static final float HEIGHT = 22.0F;
    private static final float BG_R = 0.07F;
    private static final float BG_G = 0.07F;
    private static final float BG_B = 0.08F;

    private final UILabel statusLabel;
    private int totalItems = 0;
    private BrowserItem selectedItem = null;

    public UIBrowserStatusBar(String id)
    {
        super(id);
        this.getTransform().set(0.0F, 1.0F, 1.0F, 0.0F).setPixelSize(0, (int) HEIGHT).setPixelOffset(0, (int) -HEIGHT);

        this.statusLabel = new UILabel(id + "_lbl");
        this.statusLabel.setText("0 items");
        this.statusLabel.setAlignment(UILabel.Align.LEFT);
        this.statusLabel.setTextColor(0.55F, 0.57F, 0.60F, 1.0F);
        this.statusLabel.getTransform().set(0.0F, 0.0F, 1.0F, 1.0F).setPixelOffset(10, 0).setPixelSize(-10, 0);
        this.addChild(this.statusLabel);
    }

    /** Called by the panel whenever the visible item list changes */
    public void setItemCount(int count)
    {
        this.totalItems = count;
        this.refreshLabel();
    }

    public void setSelectedItem(BrowserItem item)
    {
        this.selectedItem = item;
        this.refreshLabel();
    }

    private void refreshLabel()
    {
        if (this.selectedItem != null)
        {
            this.statusLabel.setText(this.selectedItem.getName() + "  —  " + this.selectedItem.getType().name());
            this.statusLabel.setTextColor(0.80F, 0.82F, 0.85F, 1.0F);
        }
        else
        {
            this.statusLabel.setText(this.totalItems + " item" + (this.totalItems == 1 ? "" : "s"));
            this.statusLabel.setTextColor(0.55F, 0.57F, 0.60F, 1.0F);
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
        renderer.drawRect(x, y, w, 1.0F, 1.0F, 1.0F, 1.0F, 0.04F);
    }

    public static float getHeight()
    {
        return HEIGHT;
    }
}
