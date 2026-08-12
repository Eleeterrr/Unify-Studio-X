package eleeter.unifystudiox.ui.framework.render;

import eleeter.unifystudiox.ui.framework.UIElement;

public class UIPanel extends UIElement
{
    private float bgR = 0.0f;
    private float bgG = 0.0f;
    private float bgB = 0.0f;
    private float bgA = 0.0f; // Default transparent

    public UIPanel(String id)
    {
        super(id);
    }

    public void setBackgroundColor(float r, float g, float b, float a)
    {
        this.bgR = r;
        this.bgG = g;
        this.bgB = b;
        this.bgA = a;
    }

    @Override
    public void render(UIRenderer renderer)
    {
        if (!isVisible()) return;

        // Push clip before rendering self and children so nothing bleeds outside the panel.
        renderer.pushClip(getComputedX(), getComputedY(), getComputedWidth(), getComputedHeight());
        super.render(renderer);
        renderer.popClip();
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        if (this.bgA > 0.0f)
        {
            renderer.drawRect(getComputedX(), getComputedY(), getComputedWidth(), getComputedHeight(), 
                             this.bgR, this.bgG, this.bgB, this.bgA);
        }
    }

    /** Makes this panel (and its children) visible and interactive. Horror! */
    public void enable()
    {
        setEnabled(true);
        setVisible(true);
    }

    /** Hides this panel (and its children) from rendering and input. */
    public void disable()
    {
        setEnabled(false);
        setVisible(false);
    }
}
