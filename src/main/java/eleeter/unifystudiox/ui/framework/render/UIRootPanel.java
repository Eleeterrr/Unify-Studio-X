package eleeter.unifystudiox.ui.framework.render;

import eleeter.unifystudiox.ui.framework.UIElement;

public class UIRootPanel extends UIPanel
{
    public UIRootPanel()
    {
        super("root");
    }


    public void setScreenBounds(float screenW, float screenH)
    {
        boolean shi = (this.cw != screenW || this.ch != screenH);

        this.cx = 0f;
        this.cy = 0f;
        this.cw = screenW;
        this.ch = screenH;

        if (shi)
        {
            for (UIElement child : getChildren())
            {
                child.markDirty();
            }
        }
    }


    /* Removed non-standard updateLayout override to allow proper propagation of logical dimensions */
}
