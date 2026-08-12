package eleeter.unifystudiox.ui.theme;

import eleeter.unifystudiox.ui.framework.UIElement;
import java.util.EnumMap;
import java.util.Map;
import org.lwjgl.glfw.GLFW;


public class UICursorManager
{
    private static long windowHandle;
    private static final Map<CursorType, Long> CURSOR_HANDLES = new EnumMap<>(CursorType.class);
    private static CursorType currentType = CursorType.DEFAULT;

    private UICursorManager()
    {
        /* Static utility class */
    }

    public static void init(long handle)
    {
        UICursorManager.windowHandle = handle;

        UICursorManager.CURSOR_HANDLES.put(CursorType.DEFAULT, 0L);
        UICursorManager.CURSOR_HANDLES.put(CursorType.HAND, GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR));
        UICursorManager.CURSOR_HANDLES.put(CursorType.RESIZE_EW, GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR));
        UICursorManager.CURSOR_HANDLES.put(CursorType.IBEAM, GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR));
        UICursorManager.CURSOR_HANDLES.put(CursorType.CROSSHAIR, GLFW.glfwCreateStandardCursor(GLFW.GLFW_CROSSHAIR_CURSOR));
    }

    public static void update(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        if (UICursorManager.windowHandle == 0L)
        {
            return;
        }

        UIElement hovered = context.isAnyUIHovered() ? UICursorManager.findCursorOwner(context) : null;
        CursorType targetType = (hovered != null) ? UIWidgetData.getCursor(hovered) : CursorType.DEFAULT;

        if (targetType != UICursorManager.currentType)
        {
            long handle = UICursorManager.CURSOR_HANDLES.getOrDefault(targetType, 0L);
            GLFW.glfwSetCursor(UICursorManager.windowHandle, handle);
            UICursorManager.currentType = targetType;
        }
    }

    private static UIElement findCursorOwner(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        return null;
    }

    public static void updateFromHovered(UIElement hovered)
    {
        if (UICursorManager.windowHandle == 0L)
        {
            return;
        }

        CursorType targetType = (hovered != null) ? UIWidgetData.getCursor(hovered) : CursorType.DEFAULT;

        if (targetType != UICursorManager.currentType)
        {
            long handle = UICursorManager.CURSOR_HANDLES.getOrDefault(targetType, 0L);
            GLFW.glfwSetCursor(UICursorManager.windowHandle, handle);
            UICursorManager.currentType = targetType;
        }
    }

    public static void cleanup()
    {
        for (long handle : UICursorManager.CURSOR_HANDLES.values())
        {
            if (handle != 0L)
            {
                GLFW.glfwDestroyCursor(handle);
            }
        }
    }
}
