package eleeter.unifystudiox.ui.theme;

import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.widgets.UITooltip;


public class UIThemeSystem extends UIElement
{
    private final UITooltip tooltipWidget;
    private double hoverStartTime = 0.0D;
    private UIElement lastHovered = null;

    public UIThemeSystem()
    {
        super("ui_polish_orchestrator");
        this.tooltipWidget = new UITooltip();
        this.addChild(this.tooltipWidget);
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        UITweenSystem.update(deltaTime);

        UIElement hovered = context.isAnyUIHovered() ? getHoveredElement(context) : null;
        UICursorManager.updateFromHovered(hovered);

        this.handleTooltipLogic(context, deltaTime, hovered);
    }

    private void handleTooltipLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime, UIElement hovered)
    {
        if (hovered != this.lastHovered)
        {
            this.lastHovered = hovered;
            this.hoverStartTime = 0.0D;
            this.tooltipWidget.setVisible(false);
        }

        if (hovered != null)
        {
            String text = UIWidgetData.getTooltip(hovered);
            if (text != null && !text.isEmpty())
            {
                this.hoverStartTime += deltaTime;
                if (this.hoverStartTime >= 0.5D)
                {
                    this.tooltipWidget.setText(text);
                    this.tooltipWidget.setVisible(true);
                    
                    float tx = context.getMouseX() + 12.0F;
                    float ty = context.getMouseY() + 12.0F;
                    
                    if (tx + 150.0F > this.cw)
                    {
                        tx = context.getMouseX() - 160.0F;
                    }

                    if (ty + 40.0F > this.ch)
                    {
                        ty = context.getMouseY() - 50.0F;
                    }
                    
                    this.tooltipWidget.getTransform().setPixelOffset((int) tx, (int) ty).setPixelSize(150, 30);
                }
            }
        }
    }

    private UIElement findHoveredElement(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, UIElement root)
    {
        if (context.isHovered(root))
        {
            return root;
        }

        for (UIElement child : root.getChildren())
        {
            UIElement found = this.findHoveredElement(context, child);
            if (found != null)
            {
                return found;
            }
        }

        return null;
    }

    private UIElement getHoveredElement(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        UIElement root = this.getParent();
        if (root == null)
        {
            return null;
        }
        
        return this.findHoveredElement(context, root);
    }
}
