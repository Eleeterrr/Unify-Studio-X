package eleeter.unifystudiox.ui.framework.layout;

public class UIRect
{
    private UIRect()
    {
    }

    public static boolean contains(float x, float y, float rx, float ry, float rw, float rh)
    {
        return x >= rx && x < rx + rw && y >= ry && y < ry + rh;
    }

    public static boolean intersects(float ax, float ay, float aw, float ah, float bx, float by, float bw, float bh)
    {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }
}