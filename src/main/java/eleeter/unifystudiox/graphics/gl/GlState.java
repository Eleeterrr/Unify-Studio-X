package eleeter.unifystudiox.graphics.gl;

import org.lwjgl.opengl.GL11C;

public class GlState
{
    private GlState()
    {
    }

    public static void enable(int target)
    {
        GL11C.glEnable(target);
    }

    public static void disable(int target)
    {
        GL11C.glDisable(target);
    }

    public static boolean isEnabled(int target)
    {
        return GL11C.glIsEnabled(target);
    }

    public static void blendFunc(int sfactor, int dfactor)
    {
        GL11C.glBlendFunc(sfactor, dfactor);
    }

    public static void scissor(int x, int y, int width, int height)
    {
        GL11C.glScissor(x, y, width, height);
    }

    public static void viewport(int x, int y, int width, int height)
    {
        GL11C.glViewport(x, y, width, height);
    }

    public static void lineWidth(float width)
    {
        GL11C.glLineWidth(width);
    }

    public static void pointSize(float size)
    {
        GL11C.glPointSize(size);
    }

    public static int getInteger(int pname)
    {
        return GL11C.glGetInteger(pname);
    }

    public static void getIntegerv(int pname, int[] params)
    {
        GL11C.glGetIntegerv(pname, params);
    }
}
