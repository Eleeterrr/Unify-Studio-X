package eleeter.unifystudiox.ui.framework.render;

import eleeter.unifystudiox.ui.framework.render.context.UIInputContext;

public class ScrollState
{
    private static final float SMOOTH = 0.3F;
    private static final float MIN_HANDLE = 15.0F;

    private final Region trackregion = new Region();
    private final Region handleregion = new Region();

    public float scrollItemSize;
    public float scrollSize;
    public float scrollSpeed = 30.0F;
    public ScrollStateDirection direction = ScrollStateDirection.VERTICAL;
    public boolean opposite;
    public boolean cancelScrollEdge;
    public boolean scrollbar = true;
    public float scrollbarWidth = 10.0F;
    public boolean dragging;

    public final Region region;

    private float position;
    private float target;
    private float grabRatio;

    public ScrollState(Region region)
    {
        this.region = region;
        this.region.scroll = this;
    }

    public ScrollState(Region region, float itemSize)
    {
        this(region);
        this.scrollItemSize = itemSize;
    }

    public ScrollState(Region region, float itemSize, ScrollStateDirection direction)
    {
        this(region, itemSize);
        this.direction = direction;
    }

    public void setSize(int items)
    {
        this.scrollSize = items * this.scrollItemSize;
    }

    public float getScroll()
    {
        return this.position;
    }

    public void setScroll(float value)
    {
        this.position = value;
        this.target = value;
        this.clamp();
    }

    public void scrollBy(float delta)
    {
        this.scrollTo(this.target + delta);
    }

    public void scrollTo(float value)
    {
        this.target = value;
        this.clamp();
    }

    public void scrollIntoView(float pos, float bottomOffset, float topOffset)
    {
        float visible = this.direction.getSide(this.region);
        float low = this.position + topOffset;
        float high = this.position + visible - bottomOffset;

        if (pos < low)
        {
            this.scrollTo(pos - topOffset);
        }
        else if (pos > high)
        {
            this.scrollTo(pos - visible + bottomOffset);
        }
    }

    public void clamp()
    {
        float visible = this.direction.getSide(this.region);
        float overflow = this.scrollSize - visible;

        if (overflow <= 0.0F)
        {
            this.position = 0.0F;
            this.target = 0.0F;
            return;
        }

        this.position = pin(this.position, overflow);
        this.target = pin(this.target, overflow);
    }

    private static float pin(float value, float max)
    {
        if (value < 0.0F) return 0.0F;
        if (value > max) return max;
        return value;
    }

    public boolean hasScrollbar()
    {
        return this.scrollSize > this.direction.getSide(this.region);
    }

    public float getScrollbarSize()
    {
        float visible = this.direction.getSide(this.region);

        if (this.scrollSize < visible)
        {
            return 0.0F;
        }

        float covered = this.scrollSize - visible;
        float share = (this.scrollSize - covered) / this.scrollSize;
        float length = share * visible;

        return length > MIN_HANDLE ? length : MIN_HANDLE;
    }

    private float progress()
    {
        float visible = this.direction.getSide(this.region);
        float overflow = this.scrollSize - visible;

        return overflow > 0.0F ? pin(this.position / overflow, 1.0F) : 0.0F;
    }

    public Region getScrollregion()
    {
        float visible = this.direction.getSide(this.region);

        if (this.scrollSize < visible)
        {
            this.trackregion.set(0.0F, 0.0F, 0.0F, 0.0F);
            return this.trackregion;
        }

        float thickness = this.scrollbarWidth;

        if (this.direction == ScrollStateDirection.VERTICAL)
        {
            float left = this.opposite ? this.region.x : this.region.x + this.region.w - thickness;
            this.trackregion.set(left, this.region.y, thickness, this.region.h);
        }
        else
        {
            float top = this.opposite ? this.region.y : this.region.y + this.region.h - thickness;
            this.trackregion.set(this.region.x, top, this.region.w, thickness);
        }

        return this.trackregion;
    }

    public Region getScrollbarregion()
    {
        float visible = this.direction.getSide(this.region);

        if (this.scrollSize < visible)
        {
            this.handleregion.set(0.0F, 0.0F, 0.0F, 0.0F);
            return this.handleregion;
        }

        float thickness = this.scrollbarWidth;
        float length = this.getScrollbarSize();
        float p = this.progress();

        if (this.direction == ScrollStateDirection.HORIZONTAL)
        {
            float top = this.opposite ? this.region.y : this.region.ey() - thickness;
            float left = this.region.x + p * (this.region.w - length);
            this.handleregion.set(left, top, length, thickness);
        }
        else
        {
            float left = this.opposite ? this.region.x : this.region.ex() - thickness;
            float top = this.region.y + p * (this.region.h - length);
            this.handleregion.set(left, top, thickness, length);
        }

        return this.handleregion;
    }

    public boolean mouseClicked(UIInputContext context)
    {
        if (!this.scrollbar)
        {
            return false;
        }

        int mx = (int) context.getMouseX();
        int my = (int) context.getMouseY();

        if (!this.hasScrollbar() || !this.getScrollregion().isInside(mx, my))
        {
            return false;
        }

        this.dragging = true;

        Region handle = this.getScrollbarregion();

        if (handle.isInside(mx, my))
        {
            float mouseVal = this.direction.getMouse(mx, my);
            float handleStart = this.direction.getPosition(handle, 0.0F);
            float handleLength = this.direction.getSide(handle);

            this.grabRatio = handleLength != 0.0F ? (mouseVal - handleStart) / handleLength : 0.5F;
        }
        else
        {
            this.grabRatio = 0.5F;
        }

        return true;
    }

    public boolean mouseScroll(UIInputContext context)
    {
        float wheel = context.getScrollDelta();

        if (wheel == 0.0F)
        {
            return false;
        }

        boolean inside = this.region.isInside(context.getMouseX(), context.getMouseY());

        if (!inside)
        {
            return false;
        }

        float before = this.target;
        this.scrollBy(-wheel * this.scrollSpeed);

        boolean edgeLocked = this.cancelScrollEdge && this.scrollSize > this.direction.getSide(this.region);
        boolean moved = before != this.target;

        return edgeLocked || moved;
    }

    public void mouseReleased(UIInputContext context)
    {
        this.dragging = false;
    }

    public void drag(UIInputContext context)
    {
        this.clamp();

        this.position += (this.target - this.position) * SMOOTH;

        if (this.dragging)
        {
            this.followDrag(context);
        }

        this.clamp();
    }

    private void followDrag(UIInputContext context)
    {
        float handleLength = this.getScrollbarSize();
        float travel = this.direction.getSide(this.region) - handleLength;

        if (travel <= 0.0F)
        {
            this.scrollTo(0.0F);
            return;
        }

        float mouseVal = this.direction.getMouse((int) context.getMouseX(), (int) context.getMouseY());
        float trackStart = this.direction.getPosition(this.region, 0.0F);
        float grabPoint = trackStart + handleLength * this.grabRatio;

        float p = pin((mouseVal - grabPoint) / travel, 1.0F);
        float visible = this.direction.getSide(this.region);

        this.scrollTo(p * (this.scrollSize - visible));
    }
}