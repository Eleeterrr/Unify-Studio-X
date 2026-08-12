package eleeter.unifystudiox.ui.framework;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import eleeter.unifystudiox.ui.framework.render.UIRenderer;

public abstract class UIElement
{
    private final String id;
    private boolean visible = true;
    private boolean enabled = true;
    private int zIndex = 0;
    private boolean blocksInput = false;
    private boolean pausesGame = false;

    /** Set to true whenever layout must be recomputed. */
    boolean transformDirty = true;

    private final UITransform transform = new UITransform();

    /* Computed absolute screen-space bounds in simple words (pixels) */
    public float cx, cy, cw, ch;

    private UIElement parent;
    private final List<UIElement> children = new ArrayList<>();

    public UIElement(String id)
    {
        this.id = id;
    }

    public void addChild(UIElement child)
    {
        if (child.parent != null)
        {
            child.parent.children.remove(child);
        }
        child.parent = this;
        this.children.add(child);
        this.sortChildren();
        child.markDirty();
    }

    /** Detaches a direct child. Does nothing if not a direct child. */
    public void removeChild(UIElement child)
    {
        if (this.children.remove(child))
        {
            child.parent = null;
            markDirty();
        }
    }

    /** Detaches and removes all direct children. */
    public void clearChildren()
    {
        for (UIElement child : this.children)
        {
            child.parent = null;
        }
        this.children.clear();
        markDirty();
    }

    public void markDirty()
    {
        this.transformDirty = true;
        for (UIElement child : this.children)
        {
            child.markDirty();
        }
    }

    public void updateLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        if (!this.visible || !this.enabled)
            return;

        updateSelfLogic(context, deltaTime);

        List<UIElement> snapshot = new ArrayList<>(this.children);
        for (UIElement child : snapshot)
        {
            if (this.children.contains(child))
            {
                child.updateLogic(context, deltaTime);
            }
        }
    }

    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
    }

    public void updateLayout(float parentX, float parentY, float parentW, float parentH)
    {
        if (this.transformDirty)
        {
            this.transform.compute(parentX, parentY, parentW, parentH);
            this.cx = this.transform.computedX;
            this.cy = this.transform.computedY;
            this.cw = this.transform.computedWidth;
            this.ch = this.transform.computedHeight;
            this.transformDirty = false;
        }

        for (UIElement child : this.children)
        {
            child.updateLayout(this.cx, this.cy, this.cw, this.ch);
        }
    }

    /** Renders this element and all children if visible. */
    public void render(UIRenderer renderer)
    {
        if (!this.visible)
            return;
        renderSelf(renderer);
        for (UIElement child : this.children)
        {
            child.render(renderer);
        }
    }

    protected void renderSelf(UIRenderer renderer)
    {
    }

    public void collectInteractable(List<UIElement> out)
    {
        if (!this.visible || !this.enabled)
            return;

        // Add children FIRST so they are earlier in the hit list and get priority
        for (UIElement child : this.children)
        {
            child.collectInteractable(out);
        }

        if (this.blocksInput)
        {
            out.add(this);
        }
    }

    public void setVisible(boolean horror)
    {
        if (this.visible != horror)
        {
            this.visible = horror;
            markDirty();
        }
    }

    public boolean containsPoint(float x, float y)
    {
        return this.visible && this.enabled && x >= this.cx && x < this.cx + this.cw & y >= this.cy
                && y < this.cy + this.ch;
    }

    public void setEnabled(boolean e)
    {
        this.enabled = e;
    }

    public void setBlocksInput(boolean b)
    {
        this.blocksInput = b;
    }

    public void setPausesGame(boolean p)
    {
        this.pausesGame = p;
    }

    public void setZIndex(int z)
    {
        if (this.zIndex != z)
        {
            this.zIndex = z;
            markDirty();
            if (this.parent != null)
            {
                this.parent.sortChildren();
            }
        }
    }

    public void sortChildren()
    {
        this.children.sort(Comparator.comparingInt(UIElement::getZIndex));
    }

    public String getId()
    {
        return this.id;
    }

    public UITransform getTransform()
    {
        return this.transform;
    }

    public boolean isVisible()
    {
        return this.visible;
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public boolean getBlocksInput()
    {
        return this.blocksInput;
    }

    public boolean getPausesGame()
    {
        return this.pausesGame;
    }

    public int getZIndex()
    {
        return this.zIndex;
    }

    public UIElement getParent()
    {
        return this.parent;
    }

    public List<UIElement> getChildren()
    {
        return this.children;
    }

    public float getComputedX()
    {
        return this.cx;
    }

    public float getComputedY()
    {
        return this.cy;
    }

    public float getComputedWidth()
    {
        return this.cw;
    }

    public float getComputedHeight()
    {
        return this.ch;
    }

    public boolean doesAnyChildPauseGame()
    {
        if (!this.visible)
        {
            return false;
        }

        if (this.pausesGame)
        {
            return true;
        }

        for (UIElement child : this.children)
        {
            if (child.doesAnyChildPauseGame())
            {
                return true;
            }
        }
        return false;
    }
    private IUITooltip tooltip = null;
    private EventPropagation mousePropagation = EventPropagation.PASS;
    private EventPropagation keyboardPropagation = EventPropagation.PASS;

    /* Tooltip Methods */

    public UIElement tooltip(IUITooltip tooltip)
    {
        this.tooltip = tooltip;
        return this;
    }

    public UIElement tooltip(String text)
    {
        this.tooltip = new TextTooltip(text);
        return this;
    }

    public UIElement removeTooltip()
    {
        this.tooltip = null;
        return this;
    }

    public IUITooltip getTooltip()
    {
        return this.tooltip;
    }

    /* Event Propagation Methods */

    public UIElement setMousePropagation(EventPropagation propagation)
    {
        this.mousePropagation = propagation;
        return this;
    }

    public UIElement setKeyboardPropagation(EventPropagation propagation)
    {
        this.keyboardPropagation = propagation;
        return this;
    }

    public EventPropagation getMousePropagation()
    {
        return this.mousePropagation;
    }

    public EventPropagation getKeyboardPropagation()
    {
        return this.keyboardPropagation;
    }

    /* Hierarchy Traversal Helpers */

    @SuppressWarnings("unchecked")
    public <T extends UIElement> T getParent(Class<T> clazz)
    {
        UIElement current = this.parent;
        while (current != null)
        {
            if (clazz.isAssignableFrom(current.getClass()))
            {
                return (T) current;
            }
            current = current.parent;
        }
        return null;
    }

    public boolean isDescendantOf(UIElement element)
    {
        UIElement current = this.parent;
        while (current != null)
        {
            if (current == element)
            {
                return true;
            }
            current = current.parent;
        }
        return false;
    }

    public <T> List<T> findChildrenByClass(Class<T> clazz)
    {
        List<T> result = new ArrayList<>();
        this.findChildrenByClass(clazz, result);
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> void findChildrenByClass(Class<T> clazz, List<T> outList)
    {
        for (UIElement child : this.children)
        {
            if (clazz.isAssignableFrom(child.getClass()))
            {
                outList.add((T) child);
            }
            child.findChildrenByClass(clazz, outList);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void walkChildren(Class<T> clazz, Consumer<T> action)
    {
        for (UIElement child : this.children)
        {
            if (clazz.isAssignableFrom(child.getClass()))
            {
                action.accept((T) child);
            }
            child.walkChildren(clazz, action);
        }
    }

}
