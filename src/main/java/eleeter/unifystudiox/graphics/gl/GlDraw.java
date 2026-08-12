package eleeter.unifystudiox.graphics.gl;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL45C;

public class GlDraw
{
    private GlDraw()
    {
    }

    public static void drawArrays(int mode, int first, int count)
    {
        GL11C.glDrawArrays(mode, first, count);
    }

    public static void drawElements(int mode, int count, int type, long indices)
    {
        GL11C.glDrawElements(mode, count, type, indices);
    }
    public static void drawArraysInstanced(int i, int i1, int i2, int i3)
    {
        GL45C.glDrawArraysInstanced(i, i1, i2, i3);
    }
}
