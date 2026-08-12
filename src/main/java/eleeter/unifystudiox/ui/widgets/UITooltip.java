package eleeter.unifystudiox.ui.widgets;

import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.theme.UITheme;


public class UITooltip extends UIElement
{
    private final UILabel label;

    public UITooltip()
    {
        super("global_tooltip");
        this.setVisible(false);
        this.setZIndex(9999);

        this.label = new UILabel(this.getId() + "_text");
        this.label.setAlignment(UILabel.Align.LEFT);
        this.label.getTransform().set(0.0F, 0.0F, 1.0F, 1.0F).setPixelOffset(8, 0);
        this.addChild(this.label);
    }

    public void setText(String text)
    {
        this.label.setText(text);
        
        float width = text.length() * 8.0F + 16.0F;
        this.getTransform().setPixelSize((int) width, 24);
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        UITheme.Theme theme = UITheme.get();
        float[] bg = theme.surface();
        float[] border = theme.border();

        renderer.drawRoundedRect(this.cx, this.cy, this.cw, this.ch, bg[0], bg[1], bg[2], 0.95F, theme.radiusSm());
        renderer.drawRoundedRect(this.cx - 1.0F, this.cy - 1.0F, this.cw + 2.0F, this.ch + 2.0F, border[0], border[1], border[2], 0.5F, theme.radiusSm());
    }
}
