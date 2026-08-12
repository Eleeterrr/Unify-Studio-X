package eleeter.unifystudiox.ui.theme;

import org.lwjgl.glfw.GLFW;

import eleeter.unifystudiox.ui.framework.render.UIRootPanel;


public class UIShell
{
    private static boolean booted = false;

    private UIShell()
    {
    }

    public static void boot(UIRootPanel root)
    {
        if (UIShell.booted)
        {
            return;
        }

        long window = GLFW.glfwGetCurrentContext(); 
        UICursorManager.init(window);

        root.addChild(new UIThemeSystem());

        UIShell.booted = true;
    }
}
