package eleeter.unifystudiox.ui.menu;

import eleeter.unifystudiox.settings.menu.MenuAction;
import eleeter.unifystudiox.ui.ShapeDraw;
import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.theme.CursorType;
import eleeter.unifystudiox.ui.theme.UITheme;
import eleeter.unifystudiox.ui.theme.UIWidgetAnimator;
import eleeter.unifystudiox.ui.theme.UIWidgetData;
import eleeter.unifystudiox.ui.widgets.UILabel;


public class UIMenuItem extends UIElement
{
    private static final float ICON_SLOT = 20.0F;
    private static final float ICON_SIZE = 12.0F;
    private static final float ICON_THICKNESS = 1.0F;

    private final MenuAction action;
    private final UILabel titleLabel;
    private final UILabel shortcutLabel;
    private float hoverAlpha = 0.0F;

    public UIMenuItem(String id, MenuAction action)
    {
        super(id);
        this.action = action;

        this.titleLabel = new UILabel(this.getId() + "_title");
        this.titleLabel.setText(action.getTitle());
        int titleOffset = this.action.getIcon() != null ? (int) (ICON_SLOT + 8.0F) : 12;
        this.titleLabel.getTransform().set(0.0F, 0.0F, 1.0F, 1.0F).setPixelOffset(titleOffset, 0);
        this.addChild(this.titleLabel);

        this.shortcutLabel = new UILabel(this.getId() + "_shortcut");
        this.shortcutLabel.setText(action.getShortcut());
        this.shortcutLabel.setAlignment(UILabel.Align.RIGHT);
        this.shortcutLabel.getTransform().set(0.0F, 0.0F, 1.0F, 1.0F).setPixelOffset(0, 0).setPixelSize(-12, 0);
        this.addChild(this.shortcutLabel);

        if (!action.isSeparator())
        {
            this.setBlocksInput(true);
            this.setZIndex(1002);
            UIWidgetData.setCursor(this, CursorType.HAND);
            UIWidgetAnimator.createHoverColor(this, "hover", this::getHoverAlpha, this::setHoverAlpha, 0.0F, 0.15F);

            if (action.getTooltipDescription() != null && !action.getTooltipDescription().isEmpty())
            {
                this.tooltip(action.getTooltipDescription());
            } else
            {
                this.tooltip("Trigger '" + action.getTitle() + "' action" + (action.getShortcut().isEmpty() ? "" : " [" + action.getShortcut() + "]"));
            }
        }
    }

    private float getHoverAlpha()
    {
        return this.hoverAlpha;
    }

    private void setHoverAlpha(float alpha)
    {
        this.hoverAlpha = alpha;
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        if (this.action.isSeparator())
        {
            return;
        }

        UIWidgetAnimator.updateHoverState(context, this, "hover", 0.0F, 0.15F);

        if (context.isHovered(this) && context.isMousePressed())
        {
            if (this.action.isEnabled() && this.action.getAction() != null)
            {
                this.action.getAction().run();

                UIElement p = this.getParent();
                while (p != null && !(p instanceof UIMenuDropdown))
                {
                    p = p.getParent();
                }

                if (p != null)
                {
                    p.setVisible(false);
                }
            }
        }
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        UITheme.Theme theme = UITheme.get();

        float[] textCol = this.action.isEnabled() ? theme.textPrimary() : theme.textMuted();
        this.titleLabel.setTextColor(textCol[0], textCol[1], textCol[2], textCol[3]);

        float[] muted = theme.textMuted();
        this.shortcutLabel.setTextColor(muted[0], muted[1], muted[2], muted[3]);
        this.shortcutLabel.setVisible(!this.action.getShortcut().isEmpty());

        if (this.hoverAlpha > 0.001F)
        {
            float[] accent = theme.accent();
            renderer.drawRoundedRect(this.cx + 4.0F, this.cy + 2.0F, this.cw - 8.0F, this.ch - 4.0F, accent[0], accent[1], accent[2], this.hoverAlpha, 4.0F
            );
        }

        if (this.action.getIcon() != null)
        {
            float iconCx = snapHalfPixel(this.cx + ICON_SLOT * 0.5F);
            float iconCy = snapHalfPixel(this.cy + this.ch * 0.5F);
            float[] col = this.action.isEnabled() ? theme.textMuted() : theme.textMuted();

            ShapeDraw sd = new ShapeDraw(renderer.getMatrixStack());
            this.action.getIcon().draw(sd, iconCx, iconCy, ICON_SIZE, ICON_THICKNESS);
            renderer.drawShapeGeometry(sd.getVertices(), sd.getIndices(), col[0], col[1], col[2], this.action.isEnabled() ? 1.0F : 0.4F);
        }
    }

    private static float snapHalfPixel(float value)
    {
        return (float) Math.floor(value) + 0.5F;
    }
}
