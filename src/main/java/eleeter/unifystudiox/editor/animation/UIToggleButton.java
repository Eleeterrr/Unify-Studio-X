package eleeter.unifystudiox.editor.animation;

import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.widgets.UIToggle;

public class UIToggleButton extends UIPanel
{
    @FunctionalInterface
    public interface BooleanChangeListener
    {
        void onToggled(boolean newState);
    }

    private final UIToggle toggle;

    public UIToggleButton(String id, eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        super(id);
        this.setBlocksInput(false);

        this.toggle = new UIToggle(id + "_inner", context);
        this.toggle.getTransform().set(0f, 0f, 1f, 1f);
        this.toggle.setText("");
        this.addChild(this.toggle);
    }

    public void setState(boolean state)
    {
        this.toggle.setDefaultState(state);
    }

    public boolean getState()
    {
        return this.toggle.isChecked();
    }

    public void setListener(BooleanChangeListener listener)
    {
        this.toggle.setOnToggle(listener == null ? null : listener::onToggled);
    }

    public void cleanup()
    {
        this.toggle.cleanup();
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {

    }
}
