package eleeter.unifystudiox.ui.framework.layout;

import eleeter.unifystudiox.ui.framework.UIElement;


public class UIStackLayout extends UILayoutContainer
{
    private Axis axis = Axis.VERTICAL;

    public UIStackLayout(String id)
    {
        super(id);
    }

    public UIStackLayout(String id, Axis axis)
    {
        super(id);
        this.axis = axis;
    }

    public void setAxis(Axis axis)
    {
        this.axis = axis;
        this.markDirty();
    }

    @Override
    protected void performLayout(float x, float y, float w, float h)
    {
        float currentPos = 0.0F;

        for (UIElement child : this.getChildren())
        {
            if (!child.isVisible())
            {
                continue;
            }

            if (this.axis == Axis.VERTICAL)
            {
                float childH = child.getTransform().getPixelHeight();
                
                child.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F).setAnchor(0.0F, 0.0F).setPixelOffset((int) x, (int) (y + currentPos)).setPixelSize(0, childH);
                
                currentPos += childH + this.gap;
            }
            else
            {
                float childW = child.getTransform().getPixelWidth();
                
                child.getTransform().set(0.0F, 0.0F, 0.0F, 1.0F).setAnchor(0.0F, 0.0F).setPixelOffset((int) (x + currentPos), (int) y).setPixelSize(childW, 0);
                
                currentPos += childW + this.gap;
            }
        }
    }
}
