package eleeter.unifystudiox.editor.animation;

import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.widgets.UINumberField;


public class UIEditableFloat extends UIPanel
{
    @FunctionalInterface
    public interface FloatChangeListener
    {
        void onValueChanged(float newValue);
    }

    private final UINumberField numberField;

    public UIEditableFloat(String id, eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        super(id);
        this.setBlocksInput(false);

        this.numberField = new UINumberField(id + "_field", context);
        this.numberField.getTransform().set(0f, 0f, 1f, 1f);
        this.numberField.setRange(-Float.MAX_VALUE, Float.MAX_VALUE);
        this.numberField.setStep(0.01f);
        this.addChild(this.numberField);
    }

    public void setValue(float value)
    {
        if (Float.isNaN(value) || Float.isInfinite(value)) return;
        this.numberField.setValue(value);
    }

    public float getValue()
    {
        return this.numberField.getValue();
    }

    public void setPrefix(String prefix)
    {
        this.numberField.setPrefix(prefix);
    }

    public void setListener(FloatChangeListener listener)
    {
        this.numberField.setOnValueChanged(listener == null ? null : listener::onValueChanged);
    }

    public void cleanup()
    {
        this.numberField.cleanup();
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
    }
}
