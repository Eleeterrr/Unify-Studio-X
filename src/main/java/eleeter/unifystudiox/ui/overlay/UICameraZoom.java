package eleeter.unifystudiox.ui.overlay;

import eleeter.unifystudiox.i18n.list.Keys;
import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.widgets.UILabel;

public class UICameraZoom extends UIElement
{
    private final UILabel label;

    public UICameraZoom()
    {
        super("camera_zoom_hud");


        this.getTransform().set(0.985F, 0.970F, 0.18F, 0.042F).setAnchor(1.0F, 1.0F);

        this.setVisible(false);

        this.label = new UILabel(this.getId() + "_label");
        this.label.setAlignment(UILabel.Align.CENTER);
        this.label.setTextColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.label.getTransform().set(0.0F, 0.0F, 1.0F, 1.0F);
        this.addChild(this.label);

        this.label.setText(Keys.HUD_CAMERA_ZOOM_SPEED.format(1.0F));
    }


    public void setCtrlHeld(boolean isCtrlHeld)
    {
        this.setVisible(isCtrlHeld);
    }

    public void setMultiplier(float multiplier)
    {
        this.label.setText(Keys.HUD_CAMERA_ZOOM_SPEED.format(multiplier));
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
    }
}
