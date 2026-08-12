package eleeter.unifystudiox.ui.menu;

import eleeter.unifystudiox.ui.ShapeDraw;
import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.theme.CursorType;
import eleeter.unifystudiox.ui.theme.UITheme;
import eleeter.unifystudiox.ui.theme.UIWidgetAnimator;
import eleeter.unifystudiox.ui.theme.UIWidgetData;
import eleeter.unifystudiox.ui.widgets.UILabel;

public class ContextMenuItem extends UIElement
{

    private static final float ICON_SLOT  = 24.0F;  // was 20
    private static final float ICON_SIZE  = 14.0F;  // was 12
    private static final float ICON_THICK = 1.5F;   // was 1.0

    private final UILabel  titleLabel;
    private final Runnable action;
    private float          hoverAlpha = 0.0F;
    private boolean        isDanger   = false;
    private IconDrawer     iconDrawer = null; // <-- new

    public ContextMenuItem(String id, String title, Runnable action)
    {
        super(id);
        this.action = action;
        this.setBlocksInput(true);
        this.setZIndex(10005);
        this.getTransform().setPixelSize(0, 22);

        this.titleLabel = new UILabel(this.getId() + "_title");
        this.titleLabel.setText(title);
        // offset will be set dynamically in setIcon / constructor
        this.titleLabel.getTransform().set(0.0F, 0.0F, 1.0F, 1.0F).setPixelOffset(12, 0);
        this.addChild(this.titleLabel);

        UIWidgetData.setCursor(this, CursorType.HAND);
        UIWidgetAnimator.createHoverColor(this, "hover",
                this::getHoverAlpha, this::setHoverAlpha, 0.0F, 1.0F);
    }

    /** Attach a ShapeDraw icon to this item */
    public ContextMenuItem setIcon(IconDrawer drawer)
    {
        this.iconDrawer = drawer;
        this.titleLabel.getTransform().setPixelOffset((int)(ICON_SLOT + 6), 0);  // was +4
        return this;
    }

    public ContextMenuItem setDanger(boolean danger)
    {
        this.isDanger = danger;
        return this;
    }

    private float getHoverAlpha() { return this.hoverAlpha; }
    private void  setHoverAlpha(float a) { this.hoverAlpha = a; }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        UIWidgetAnimator.updateHoverState(context, this, "hover", 0.0F, 1.0F);

        if (context.isHovered(this) && (context.isMousePressed() || context.isRightMousePressed()))
        {
            if (this.action != null)
            {
                this.action.run();

                UIElement parent = this.getParent();
                while (parent != null && !(parent instanceof UIContextMenu))
                    parent = parent.getParent();

                if (parent instanceof UIContextMenu)
                    ((UIContextMenu) parent).close();
            }
        }
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        UITheme.Theme theme = UITheme.get();

        float[] textCol = this.isDanger
                ? new float[]{0.9F, 0.3F, 0.3F, 1.0F}
                : theme.textPrimary();
        this.titleLabel.setTextColor(textCol[0], textCol[1], textCol[2], textCol[3]);

        if (this.hoverAlpha > 0.001F)
        {
            float[] hoverBg    = this.isDanger ? new float[]{0.9F, 0.3F, 0.3F, 1.0F} : theme.accent();
            float   targetAlpha = this.isDanger ? 0.15F : 0.18F;
            renderer.drawRoundedRect(
                    this.cx + 4.0F, this.cy + 1.0F,
                    this.cw - 8.0F, this.ch - 2.0F,
                    hoverBg[0], hoverBg[1], hoverBg[2],
                    this.hoverAlpha * targetAlpha, 4.0F
            );
        }

        // Draw icon if present
        // Draw icon if present
        if (this.iconDrawer != null)
        {
            float iconCx = this.cx + ICON_SLOT * 0.5F;          // remove snapHalfPixel
            float iconCy = this.cy + this.ch * 0.5F;
            float[] col  = this.isDanger ? new float[]{0.9F, 0.3F, 0.3F, 1.0F} : theme.textMuted();

            ShapeDraw sd = new ShapeDraw(renderer.getMatrixStack());
            this.iconDrawer.draw(sd, iconCx, iconCy, ICON_SIZE, ICON_THICK);
            renderer.drawShapeGeometry(sd.getVertices(), sd.getIndices(), col[0], col[1], col[2], 1.0F);
        }
    }

    private static float snapHalfPixel(float value)
    {
        return (float) Math.floor(value) + 0.5F;
    }
}
