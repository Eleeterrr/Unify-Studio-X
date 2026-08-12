package eleeter.unifystudiox.ui.widgets;

import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;


public class UIProgressBar extends UIPanel
{
    private float progress = 0.0f;

    private float bgR = 0.1f, bgG = 0.1f, bgB = 0.1f, bgA = 1.0f;
    private float fgR = 0.2f, fgG = 0.7f, fgB = 0.3f, fgA = 1.0f;

    public UIProgressBar(String id)
    {
        super(id);
        setBlocksInput(false);
    }

    public void setProgress(float progress)
    {
        this.progress = Math.max(0.0f, Math.min(1.0f, progress));
    }
    
    public void setTrackColor(float r, float g, float b, float a)
    {
        this.bgR = r; this.bgG = g; this.bgB = b; this.bgA = a;
    }
    
    public void setFillColor(float r, float g, float b, float a)
    {
        this.fgR = r; this.fgG = g; this.fgB = b; this.fgA = a;
    }

    public float getProgress()
    {
        return this.progress;
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        float x = getComputedX();
        float y = getComputedY();
        float w = getComputedWidth();
        float h = getComputedHeight();

        renderer.drawRect(x, y, w, h, bgR, bgG, bgB, bgA);

        if (progress > 0.0f)
        {
            renderer.drawRect(x, y, w * progress, h, fgR, fgG, fgB, fgA);
        }
    }
}
