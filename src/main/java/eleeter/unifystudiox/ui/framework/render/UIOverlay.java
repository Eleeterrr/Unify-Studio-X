package eleeter.unifystudiox.ui.framework.render;

/**
 * A mathematical model and base class that tracks the size, offset, and dragging/resizing
 * state of a 2D overlay panel. Contains NO rendering, OpenGL buffers, or draw logic.
 */
public class UIOverlay extends UIPanel
{

    protected final eleeter.unifystudiox.ui.framework.render.context.UIInputContext context;

    protected float panelWidth;
    protected float panelHeight;
    protected float panelOffsetX = 0.0F;
    protected float panelOffsetY = 0.0F;

    private boolean isDragging = false;
    private boolean isResizing = false;

    private float dragStartMouseX = 0.0F;
    private float dragStartMouseY = 0.0F;
    private float dragStartOffsetX = 0.0F;
    private float dragStartOffsetY = 0.0F;
    private float dragStartWidth = 0.0F;
    private float dragStartHeight = 0.0F;

    private final float minWidth;
    private final float minHeight;

    public UIOverlay(String id, eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, float initialWidth, float initialHeight, float minWidth, float minHeight)
    {
        super(id);
        this.context = context;
        this.panelWidth = initialWidth;
        this.panelHeight = initialHeight;
        this.minWidth = minWidth;
        this.minHeight = minHeight;
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        float currentX = getComputedX();
        float currentY = getComputedY();

        float activeX = currentX + 50.0F + this.panelOffsetX;
        float activeY = currentY + 50.0F + this.panelOffsetY;

        Region activeArea = new Region(activeX, activeY, this.panelWidth, this.panelHeight);

        // 1. Resize Grip Hit-Test (Bottom-right corner, 15x15 pixels)
        float gripSize = 15.0F;
        Region gripArea = new Region(activeArea.ex() - gripSize - 5.0F, activeArea.ey() - gripSize - 5.0F, gripSize, gripSize);
        boolean isMouseOverResize = gripArea.isInside(context);

        // 2. Header Drag Hit-Test (Top banner area, offset by 15px margins, 60px height)
        Region headerArea = new Region(activeArea.x + 15.0F, activeArea.y + 15.0F, activeArea.w - 30.0F, 60.0F);
        boolean isMouseOverHeader = headerArea.isInside(context);

        // --- RESIZING STATE MACHINE ---
        if (context.isClicked(this) && isMouseOverResize && !this.isDragging)
        {
            this.isResizing = true;
            this.dragStartMouseX = context.getMouseX();
            this.dragStartMouseY = context.getMouseY();
            this.dragStartWidth = this.panelWidth;
            this.dragStartHeight = this.panelHeight;
        }

        if (this.isResizing)
        {
            if (context.isMouseDown())
            {
                this.panelWidth = UIBoxMath.calculateResize(this.dragStartWidth, this.dragStartMouseX, context.getMouseX(), this.minWidth, Float.MAX_VALUE);
                this.panelHeight = UIBoxMath.calculateResize(this.dragStartHeight, this.dragStartMouseY, context.getMouseY(), this.minHeight, Float.MAX_VALUE);
            } else
            {
                this.isResizing = false;
            }
        }

        // --- DRAGGING STATE MACHINE ---
        if (context.isClicked(this) && isMouseOverHeader && !this.isResizing)
        {
            this.isDragging = true;
            this.dragStartMouseX = context.getMouseX();
            this.dragStartMouseY = context.getMouseY();
            this.dragStartOffsetX = this.panelOffsetX;
            this.dragStartOffsetY = this.panelOffsetY;
        }

        if (this.isDragging)
        {
            if (context.isMouseDown())
            {
                this.panelOffsetX = UIBoxMath.calculateDrag(this.dragStartOffsetX, this.dragStartMouseX, context.getMouseX());
                this.panelOffsetY = UIBoxMath.calculateDrag(this.dragStartOffsetY, this.dragStartMouseY, context.getMouseY());
            } else
            {
                this.isDragging = false;
            }
        }

        // Delegate logical updates to subclass
        onUpdateSelf((float) deltaTime);
    }

    /**
     * Subclasses can override this to implement custom frame-by-frame logical updates.
     */
    protected void onUpdateSelf(float deltaTime)
    {
    }

    protected float getHeaderDragHeight()
    {
        return 60.0F;
    }

    @Override
    public boolean containsPoint(float x, float y)
    {
        if (!isVisible() || !isEnabled())
        {
            return false;
        }

        float px = getComputedX() + 50.0F + this.panelOffsetX;
        float py = getComputedY() + 50.0F + this.panelOffsetY;

        Region activeArea = new Region(px, py, this.panelWidth, this.panelHeight);
        return activeArea.isInside(x, y);
    }

    public float getPanelWidth()
    {
        return this.panelWidth;
    }

    public float getPanelHeight()
    {
        return this.panelHeight;
    }

    public float getPanelOffsetX()
    {
        return this.panelOffsetX;
    }

    public float getPanelOffsetY()
    {
        return this.panelOffsetY;
    }

    public boolean isDragging()
    {
        return this.isDragging;
    }

    public boolean isResizing()
    {
        return this.isResizing;
    }
}
