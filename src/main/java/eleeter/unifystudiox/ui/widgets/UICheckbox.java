package eleeter.unifystudiox.ui.widgets;

import java.util.function.Consumer;

import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;


public class UICheckbox extends UIPanel
{
    private final eleeter.unifystudiox.ui.framework.render.context.UIInputContext context;
    private boolean checked = false;
    private Consumer<Boolean> onToggle;

    public UICheckbox(String id, eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        super(id);
        this.context = context;
        setBlocksInput(true);
    }

    public void setChecked(boolean checked)
    {
        this.checked = checked;
    }

    public boolean isChecked()
    {
        return this.checked;
    }

    public void setOnToggle(Consumer<Boolean> onToggle)
    {
        this.onToggle = onToggle;
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        if (context.isClicked(this))
        {
            this.checked = !this.checked;
            if (this.onToggle != null)
            {
                this.onToggle.accept(this.checked);
            }
        }
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {

        float x = getComputedX();
        float y = getComputedY();
        float w = getComputedWidth();
        float h = getComputedHeight();

        renderer.drawRect(x, y, w, h, 0.1f, 0.1f, 0.1f, 1.0f);

        float padding = Math.min(w, h) * 0.2f;
        
        if (this.checked)
        {
            renderer.drawRect(x + padding, y + padding, w - padding * 2, h - padding * 2, 0.2f, 0.7f, 0.3f, 1.0f);
        }
        else if (context.isHovered(this))
        {
            renderer.drawRect(x + padding, y + padding, w - padding * 2, h - padding * 2, 0.2f, 0.2f, 0.2f, 1.0f);
        }
    }
}
