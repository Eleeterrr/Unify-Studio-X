package eleeter.unifystudiox.ui.widgets;

import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.layout.UIRect;
import eleeter.unifystudiox.ui.framework.render.Region;
import eleeter.unifystudiox.ui.framework.render.ScrollState;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.framework.render.context.UIInputContext;
import java.util.function.Consumer;

public class UIScrollContainer extends UIElement
{
    private final ScrollState scroll;

    private float contentHeight = 0.0F;

    private float scrollbarWidth = 6.0F;
    private float scrollSpeed = 40.0F;

    private boolean clipChildren = true;

    private Consumer<UIRenderer> scrollbarRenderer;

    public UIScrollContainer(String id)
    {
        super(id);

        this.scroll = new ScrollState(new Region());
        this.scroll.scrollbarWidth = this.scrollbarWidth;
        this.scroll.scrollSpeed = this.scrollSpeed;
    }

    public ScrollState getScroll()
    {
        return this.scroll;
    }

    public float getContentHeight()
    {
        return this.contentHeight;
    }

    public void setContentHeight(float height)
    {
        this.contentHeight = Math.max(0.0F, height);
        this.scroll.scrollSize = this.contentHeight;
        this.scroll.clamp();
        this.markDirty();
    }

    public void setScrollbarWidth(float width)
    {
        this.scrollbarWidth = Math.max(0.0F, width);
        this.scroll.scrollbarWidth = this.scrollbarWidth;
    }

    public void setScrollSpeed(float speed)
    {
        this.scrollSpeed = Math.max(0.0F, speed);
        this.scroll.scrollSpeed = this.scrollSpeed;
    }

    public void setClipChildren(boolean clipChildren)
    {
        this.clipChildren = clipChildren;
    }

    public void setScrollbarRenderer(Consumer<UIRenderer> renderer)
    {
        this.scrollbarRenderer = renderer;
    }

    public float getContentX()
    {
        return this.cx;
    }

    public float getContentY()
    {
        return this.cy;
    }

    public float getContentWidth()
    {
        return this.cw;
    }

    public float getContentHeightVisible()
    {
        return this.ch;
    }

    public float getScrollOffset()
    {
        return this.scroll.getScroll();
    }

    public Region getViewport()
    {
        return this.scroll.region;
    }

    @Override
    public void updateLayout(float parentX, float parentY, float parentW, float parentH)
    {
        super.updateLayout(parentX, parentY, parentW, parentH);

        updateScrollRegion();

        float scrollOffset = this.scroll.getScroll();

        for (UIElement child : getChildren())
        {
            child.markDirty();
            child.updateLayout(this.cx, this.cy - scrollOffset, this.cw, this.contentHeight
            );
        }
    }

    @Override
    protected void updateSelfLogic(UIInputContext context, double deltaTime)
    {
        updateScrollRegion();

        this.scroll.clamp();

        if (context.isMousePressed())
        {
            this.scroll.mouseClicked(context);
        }

        if (!context.isMouseDown())
        {
            this.scroll.mouseReleased(context);
        }

        this.scroll.mouseScroll(context);
        this.scroll.drag(context);
        this.scroll.clamp();
    }

    @Override
    public void render(UIRenderer renderer)
    {
        if (!isVisible())
        {
            return;
        }

        if (this.clipChildren)
        {
            renderer.pushClip(this.scroll.region.x, this.scroll.region.y, this.scroll.region.w, this.scroll.region.h);
        }

        for (UIElement child : getChildren())
        {
            child.render(renderer);
        }

        if (this.clipChildren)
        {
            renderer.popClip();
        }

        renderScrollbar(renderer);
    }

    @Override
    public void collectInteractable(java.util.List<UIElement> out)
    {
        if (!isVisible() || !isEnabled())
        {
            return;
        }

        float vx = this.scroll.region.x;
        float vy = this.scroll.region.y;
        float vw = this.scroll.region.w;
        float vh = this.scroll.region.h;

        for (UIElement child : getChildren())
        {
            if (UIRect.intersects(child.cx, child.cy, child.cw, child.ch, vx, vy, vw, vh))
            {
                child.collectInteractable(out);
            }
        }
    }

    private void updateScrollRegion()
    {
        this.scroll.region.set(this.cx, this.cy, Math.max(0.0F, this.cw - this.scrollbarWidth), this.ch
        );

        this.scroll.scrollSize = this.contentHeight;
    }

    private void renderScrollbar(UIRenderer renderer)
    {
        if (!this.scroll.hasScrollbar())
        {
            return;
        }

        if (this.scrollbarRenderer != null)
        {
            this.scrollbarRenderer.accept(renderer);
            return;
        }

        Region track = this.scroll.getScrollregion();
        Region thumb = this.scroll.getScrollbarregion();

        renderer.drawRect(track.x, track.y, track.w, track.h, 0.08F, 0.09F, 0.11F, 0.55F
        );

        float alpha = this.scroll.dragging ? 0.85F : 0.48F;

        renderer.drawRoundedRect(track.x, thumb.y, track.w, thumb.h, 0.28F, 0.50F, 0.98F, alpha, 3.0F
        );
    }
}