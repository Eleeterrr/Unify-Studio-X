package eleeter.unifystudiox.ui.framework.layout;

import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;


public abstract class UILayoutContainer extends UIPanel
{
    protected float gap = 0.0F;
    protected float paddingX = 0.0F;
    protected float paddingY = 0.0F;

    public UILayoutContainer(String id)
    {
        super(id);
    }

    public void setGap(float gap)
    {
        this.gap = gap;
        this.markDirty();
    }

    public void setPadding(float px, float py)
    {
        this.paddingX = px;
        this.paddingY = py;
        this.markDirty();
    }

    @Override
    public void updateLayout(float parentX, float parentY, float parentW, float parentH)
    {
        this.getTransform().compute(parentX, parentY, parentW, parentH);
        this.cx = this.getTransform().getComputedX();
        this.cy = this.getTransform().getComputedY();
        this.cw = this.getTransform().getComputedW();
        this.ch = this.getTransform().getComputedH();

        float contentX = this.paddingX;
        float contentY = this.paddingY;
        float contentW = this.cw - (this.paddingX * 2.0F);
        float contentH = this.ch - (this.paddingY * 2.0F);

        this.performLayout(contentX, contentY, contentW, contentH);

        for (UIElement child : this.getChildren())
        {
            child.updateLayout(this.cx, this.cy, this.cw, this.ch);
        }
    }

    protected abstract void performLayout(float x, float y, float w, float h);

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        super.renderSelf(renderer);
    }
}
