package eleeter.unifystudiox.ui.theme;

import java.util.WeakHashMap;

import eleeter.unifystudiox.ui.framework.UIElement;


public class UIWidgetData
{
    private static final WeakHashMap<UIElement, String> TOOLTIPS = new WeakHashMap<>();
    private static final WeakHashMap<UIElement, CursorType> CURSORS = new WeakHashMap<>();

    private UIWidgetData()
    {
    }

    public static void setTooltip(UIElement element, String text)
    {
        UIWidgetData.TOOLTIPS.put(element, text);
    }

    public static String getTooltip(UIElement element)
    {
        return UIWidgetData.TOOLTIPS.get(element);
    }

    public static void setCursor(UIElement element, CursorType type)
    {
        UIWidgetData.CURSORS.put(element, type);
    }

    public static CursorType getCursor(UIElement element)
    {
        return UIWidgetData.CURSORS.getOrDefault(element, CursorType.DEFAULT);
    }
}
