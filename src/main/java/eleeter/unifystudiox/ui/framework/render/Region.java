package eleeter.unifystudiox.ui.framework.render;

public class Region
{
    public static final Region SHARED = new Region();

    public float x, y, w, h;
    public ScrollState scroll;

    public Region()
    {
    }

    public Region(Region source)
    {
        this.copy(source);
    }

    public Region(float x, float y, float w, float h)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public float ex()
    {
        return this.x + this.w;
    }

    public float ey()
    {
        return this.y + this.h;
    }

    public float x(float anchor)
    {
        return this.x + this.w * anchor;
    }

    public float x(float anchor, float size)
    {
        return this.x + (this.w - size) * anchor;
    }

    public float y(float anchor)
    {
        return this.y + this.h * anchor;
    }

    public float y(float anchor, float size)
    {
        return this.y + (this.h - size) * anchor;
    }

    public boolean isInside(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        return this.isInside(context.getMouseX(), context.getMouseY());
    }

    public boolean isInside(float px, float py)
    {
        if (px < this.x || py < this.y)
        {
            return false;
        }

        return px <= this.ex() && py <= this.ey();
    }

    public void clamp(Region target)
    {
        float minX = this.x;
        float minY = this.y;
        float maxX = this.ex();
        float maxY = this.ey();

        float x1 = restrict(target.x, minX, maxX);
        float y1 = restrict(target.y, minY, maxY);
        float x2 = restrict(target.ex(), minX, maxX);
        float y2 = restrict(target.ey(), minY, maxY);

        target.setPoints(x1, y1, x2, y2);
    }

    private static float restrict(float v, float lo, float hi)
    {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }

    public void offset(float amount)
    {
        this.offsetX(amount);
        this.offsetY(amount);
    }

    public void offsetX(float amount)
    {
        this.x -= amount;
        this.w += amount + amount;
    }

    public void offsetY(float amount)
    {
        this.y -= amount;
        this.h += amount + amount;
    }

    public void set(float x, float y, float w, float h)
    {
        this.setPos(x, y);
        this.setSize(w, h);
    }

    public void setPos(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public void setSize(float w, float h)
    {
        this.w = w;
        this.h = h;
    }

    public void setPoints(float x1, float y1, float x2, float y2)
    {
        this.setPoints(x1, y1, x2, y2, 0.0F);
    }

    public void setPoints(float x1, float y1, float x2, float y2, float padding)
    {
        float left = x1 < x2 ? x1 : x2;
        float top = y1 < y2 ? y1 : y2;
        float right = x1 > x2 ? x1 : x2;
        float bottom = y1 > y2 ? y1 : y2;

        this.x = left - padding;
        this.y = top - padding;
        this.w = (right - left) + padding;
        this.h = (bottom - top) + padding;
    }

    public void copy(Region source)
    {
        this.x = source.x;
        this.y = source.y;
        this.w = source.w;
        this.h = source.h;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Region))
        {
            return false;
        }

        Region r = (Region) obj;

        return this.x == r.x && this.y == r.y && this.w == r.w && this.h == r.h;
    }

    @Override
    public int hashCode()
    {
        int h1 = Float.floatToIntBits(this.x);
        int h2 = Float.floatToIntBits(this.y);
        int h3 = Float.floatToIntBits(this.w);
        int h4 = Float.floatToIntBits(this.h);

        return ((h1 * 31 + h2) * 31 + h3) * 31 + h4;
    }

    @Override
    public String toString()
    {
        return this.x + " " + this.y + " " + this.w + " " + this.h;
    }
}