package eleeter.unifystudiox.ui.framework.render;

public enum ScrollStateDirection
{
    VERTICAL(true),
    HORIZONTAL(false);

    private final boolean vertical;

    ScrollStateDirection(boolean vertical)
    {
        this.vertical = vertical;
    }

    public int getSide(Region area)
    {
        return (int) (this.vertical ? area.h : area.w);
    }

    public float getPosition(Region area, float offset)
    {
        float base = this.vertical ? area.y : area.x;
        return base + offset;
    }

    public int getMouse(int x, int y)
    {
        return this.vertical ? y : x;
    }

    public int getScroll(Region area, ScrollState scroll, int x, int y)
    {
        float cursor = this.vertical ? y : x;
        float origin = this.vertical ? area.y : area.x;

        return (int) (cursor - origin + scroll.getScroll());
    }
}