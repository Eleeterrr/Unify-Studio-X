package eleeter.unifystudiox.ui.menu;

import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.theme.UITheme;

public class ContextMenuSeparator extends UIElement
{
    public ContextMenuSeparator(String id)
    {
        super(id);
        this.getTransform().setPixelSize(0, 6);
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        float midY = this.cy + this.ch / 2.0F;
        float[] border = UITheme.get().border();
        
        // Very subtle line
        renderer.drawRect(this.cx + 8.0F, midY, this.cw - 16.0F, 1.0F, border[0], border[1], border[2], 0.3F);
    }
}
