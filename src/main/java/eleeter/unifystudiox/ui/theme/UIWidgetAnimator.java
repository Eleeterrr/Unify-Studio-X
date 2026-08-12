package eleeter.unifystudiox.ui.theme;

import java.util.function.Consumer;
import java.util.function.Supplier;

import eleeter.unifystudiox.ui.framework.UIElement;


public class UIWidgetAnimator
{
    private UIWidgetAnimator()
    {
    }

    public static void createHoverColor(UIElement widget, String key, Supplier<Float> getter, Consumer<Float> setter, float normalVal, float hoverVal)
    {
        UITweenSystem.register(widget, key, getter, setter, normalVal, 10.0F);
    }

    public static void updateHoverState(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, UIElement widget, String key, float normalVal, float hoverVal)
    {
        boolean hovered = context.isHovered(widget);
        UITweenSystem.setTarget(widget, key, hovered ? hoverVal : normalVal);
    }
}
