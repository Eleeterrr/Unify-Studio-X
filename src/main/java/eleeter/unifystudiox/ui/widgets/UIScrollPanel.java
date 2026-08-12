package eleeter.unifystudiox.ui.widgets;

import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;

public class UIScrollPanel extends UIPanel
{
    private final eleeter.unifystudiox.ui.framework.render.ScrollState scroll;

    private static final float BAR_WIDTH = 6.0F;
    private static final float BAR_COLOR = 0.35F;

    private final UIPanel content;

    public UIScrollPanel(String id)
    {
        super(id);
        setBlocksInput(false); 

        this.scroll = new eleeter.unifystudiox.ui.framework.render.ScrollState(new eleeter.unifystudiox.ui.framework.render.Region());
        this.scroll.scrollbarWidth = BAR_WIDTH;

        // Standard UI Engine Pattern: A Viewport (this) contains a Content Container
        this.content = new UIPanel(id + "_content");
        this.content.getTransform().set(0, 0, 1.0F, 0); // Fill width, height set by content
        super.addChild(this.content);
    }

    public UIPanel getContent()
    {
        return this.content;
    }

    @Override
    public void addChild(UIElement child)
    {
        this.content.addChild(child);
    }

    @Override
    public void clearChildren()
    {
        this.content.clearChildren();
    }

    public void setMaxContentHeight(float height)
    {
        this.scroll.scrollSize = height;
        this.content.getTransform().setPixelSize(0, height);
        this.content.markDirty();
    }

    public void setScrollSpeed(float speed)
    {
        this.scroll.scrollSpeed = speed;
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        float viewX = getComputedX();
        float viewY = getComputedY();
        float viewW = getComputedWidth();
        float viewH = getComputedHeight();

        /* Update scroll area bounds dynamically */
        this.scroll.region.set(viewX, viewY, viewW, viewH);

        this.scroll.clamp();

        float oldScrollY = this.scroll.getScroll();

        float mx = context.getMouseX();
        float my = context.getMouseY();
        boolean isMouseOver = mx >= viewX && mx < viewX + viewW && my >= viewY && my < viewY + viewH;

        if (isMouseOver && context.isMousePressed())
        {
            this.scroll.mouseClicked(context);
        }

        if (!context.isMouseDown())
        {
            this.scroll.mouseReleased(context);
        }

        if (isMouseOver)
        {
            this.scroll.mouseScroll(context);
        }

        this.scroll.drag(context);

        this.scroll.clamp();

        float newScrollY = this.scroll.getScroll();

        if (Math.abs(newScrollY - oldScrollY) > 0.0001F)
        {
            this.content.getTransform().setPixelOffset(0, (int)-newScrollY);
            this.content.markDirty();
        }
    }

    @Override
    public void render(UIRenderer renderer)
    {
        if (!isVisible()) return;

        float x = getComputedX();
        float y = getComputedY();
        float w = getComputedWidth();
        float h = getComputedHeight();

        renderer.pushClip(x, y, w, h);

        renderSelf(renderer);
        
        for (UIElement child : getChildren())
        {
            child.render(renderer);
        }

        if (this.scroll.hasScrollbar())
        {
            eleeter.unifystudiox.ui.framework.render.Region trackArea = this.scroll.getScrollregion();
            eleeter.unifystudiox.ui.framework.render.Region thumbArea = this.scroll.getScrollbarregion();

            renderer.drawRect(trackArea.x, trackArea.y, trackArea.w, trackArea.h, 0.1F, 0.1F, 0.1F, 0.3F);

            float alpha = this.scroll.dragging ? 0.8F : 0.5F;
            renderer.drawRoundedRect(trackArea.x, thumbArea.y, BAR_WIDTH, thumbArea.h, BAR_COLOR, BAR_COLOR, BAR_COLOR, alpha, 3.0F);
        }

        renderer.popClip();
    }
}
