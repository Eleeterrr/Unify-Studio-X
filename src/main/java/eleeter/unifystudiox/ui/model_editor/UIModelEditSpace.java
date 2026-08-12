package eleeter.unifystudiox.ui.model_editor;

import eleeter.unifystudiox.assets.IModelAsset;
import eleeter.unifystudiox.editor.animation.ModelEditViewport;
import eleeter.unifystudiox.ui.UIKey;
import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.theme.UIDropShadow;
import eleeter.unifystudiox.ui.theme.UITheme;
import eleeter.unifystudiox.ui.widgets.UILabel;
import java.util.List;

public class UIModelEditSpace extends UIPanel
{
    private static final float HEADER_H = 36.0F;
    private static final float FOOTER_H = 16.0F;
    private static final float BORDER = 1.0F;
    private static final float GRIP_SIZE = 14.0F;
    private static final float INITIAL_W = 800.0F;
    private static final float INITIAL_H = 500.0F;
    private static final float MIN_W = 400.0F;
    private static final float MIN_H = 200.0F;
    private static final float MIN_VIEWPORT_PX = 4.0F;
    private static final float HIERARCHY_PANEL_W = 240.0F;
    private static final float TRANSFORM_PAD_MARGIN = 16.0F;

    private static final float ORBIT_SENSITIVITY = 0.40F;
    private static final float PAN_SENSITIVITY = 0.006F;
    private static final float ZOOM_SENSITIVITY = 1.0F;

    private float panelWidth = INITIAL_W;
    private float panelHeight = INITIAL_H;
    private float panelOffsetX = 0.0F;
    private float panelOffsetY = 0.0F;
    private float baseX = 0.0F;
    private float baseY = 0.0F;
    private boolean baseInitialized = false;

    private boolean isDragging = false;
    private boolean isResizing = false;
    private float dragStartMouseX = 0.0F;
    private float dragStartMouseY = 0.0F;
    private float dragStartOffsetX = 0.0F;
    private float dragStartOffsetY = 0.0F;
    private float dragStartW = 0.0F;
    private float dragStartH = 0.0F;
    private float gripHoverProgress = 0.0F;

    private boolean lastToggleKeyState = false;
    private int viewportTextureHandle = 0;

    private final ModelEditViewport viewport;
    private final UIModelHierarchyPanel hierarchyPanel;
    private final UIModelTransformPad transformPad;
    private final UILabel titleLabel;


    public UIModelEditSpace()
    {
        super("model_edit_space");

        this.viewport = new ModelEditViewport();

        eleeter.unifystudiox.ui.framework.render.ScrollState scrollVertical = new eleeter.unifystudiox.ui.framework.render.ScrollState(new eleeter.unifystudiox.ui.framework.render.Region());
        scrollVertical.scrollbarWidth = 6.0F;
        scrollVertical.scrollSpeed = 30.0F;

        eleeter.unifystudiox.ui.framework.render.ScrollState scrollHorizontal = new eleeter.unifystudiox.ui.framework.render.ScrollState(new eleeter.unifystudiox.ui.framework.render.Region(), 0.0F, eleeter.unifystudiox.ui.framework.render.ScrollStateDirection.HORIZONTAL);
        scrollHorizontal.scrollbarWidth = 6.0F;
        scrollHorizontal.scrollSpeed = 30.0F;

        this.hierarchyPanel = new UIModelHierarchyPanel(scrollVertical, scrollHorizontal);
        this.hierarchyPanel.setVisible(true);
        this.transformPad = new UIModelTransformPad();

        this.setBlocksInput(true);
        this.setVisible(false);
        this.setZIndex(22);
        this.getTransform().set(0.0F, 0.0F, 1.0F, 1.0F);

        this.titleLabel = new UILabel("model_edit_space_title");
        this.titleLabel.setText("Model Editor");
        this.titleLabel.setAlignment(UILabel.Align.LEFT);
        this.titleLabel.setTextColor(0.88F, 0.91F, 0.98F, 1.0F);
        this.titleLabel.getTransform().set(0.0F, 0.0F, 0.5F, 0.0F).setPixelOffset(16, 9).setPixelSize(-36, (int) HEADER_H - 12);

        this.addChild(this.titleLabel);
    }

    public UIModelHierarchyPanel getHierarchyPanel()
    {
        return this.hierarchyPanel;
    }

    public ModelEditViewport getViewport()
    {
        return this.viewport;
    }

    @Override
    public void updateLayout(float parentX, float parentY, float parentW, float parentH)
    {
        if (!this.baseInitialized)
        {
            this.baseX = parentX + (parentW - this.panelWidth) * 0.5F;
            this.baseY = parentY + (parentH - this.panelHeight) * 0.5F;
            this.baseInitialized = true;
        }

        super.updateLayout(parentX, parentY, parentW, parentH);

        this.cx = this.baseX + this.panelOffsetX;
        this.cy = this.baseY + this.panelOffsetY;
        this.cw = this.panelWidth;
        this.ch = this.panelHeight;

        this.titleLabel.markDirty();
        this.titleLabel.updateLayout(this.cx, this.cy, this.cw, this.ch);
    }

    @Override
    public boolean containsPoint(float x, float y)
    {
        if (!isVisible() || !isEnabled()) return false;
        return x >= this.cx && x < this.cx + this.cw
                && y >= this.cy && y < this.cy + this.ch;
    }

    @Override
    public void render(UIRenderer renderer)
    {
        if (!this.isVisible()) return;
        this.renderSelf(renderer);
        this.titleLabel.render(renderer);

        float contentY = this.cy + HEADER_H;
        float contentH = this.ch - HEADER_H - FOOTER_H - BORDER;
        this.hierarchyPanel.setDocked(this.cx + BORDER, contentY, HIERARCHY_PANEL_W, contentH);
        this.hierarchyPanel.syncRenderPosition(this.cx + BORDER, contentY, HIERARCHY_PANEL_W, contentH);

        this.hierarchyPanel.render(renderer);
        this.transformPad.render(renderer);
    }

    @Override
    public void updateLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        boolean toggleKeyDown = context.isKeyPressed(UIKey.V);
        if (toggleKeyDown && !this.lastToggleKeyState)
        {
            this.setVisible(!this.isVisible());
        }
        this.lastToggleKeyState = toggleKeyDown;

        if (this.isVisible())
        {
            super.updateLogic(context, deltaTime);
        }
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        float px = this.cx;
        float py = this.cy;
        float pw = this.cw;
        float ph = this.ch;

        float gripX = px + pw - GRIP_SIZE - 6.0F;
        float gripY = py + ph - FOOTER_H;
        boolean overGrip = context.getMouseX() >= gripX && context.getMouseX() <= px + pw
                && context.getMouseY() >= gripY && context.getMouseY() <= py + ph;

        float gripTarget = overGrip || this.isResizing ? 1.0F : 0.0F;
        this.gripHoverProgress = approach(this.gripHoverProgress, gripTarget, (float) deltaTime * 8.0F);

        boolean overHeader = context.getMouseX() >= px + 10.0F && context.getMouseX() <= px + pw - 10.0F && context.getMouseY() >= py + 4.0F && context.getMouseY() <= py + HEADER_H - 4.0F;

        if (context.isMousePressed() && overGrip && !this.isDragging)
        {
            this.isResizing = true;
            this.dragStartMouseX = context.getMouseX();
            this.dragStartMouseY = context.getMouseY();
            this.dragStartW = this.panelWidth;
            this.dragStartH = this.panelHeight;
        }
        if (this.isResizing)
        {
            if (context.isMouseDown())
            {
                this.panelWidth = clamp(this.dragStartW + (context.getMouseX() - this.dragStartMouseX), MIN_W, 2000.0F);
                this.panelHeight = clamp(this.dragStartH + (context.getMouseY() - this.dragStartMouseY), MIN_H, 2000.0F);
                this.markDirty();
            } else
            {
                this.isResizing = false;
            }
        }

        if (context.isMousePressed() && overHeader && !this.isResizing)
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
                this.panelOffsetX = this.dragStartOffsetX + (context.getMouseX() - this.dragStartMouseX);
                this.panelOffsetY = this.dragStartOffsetY + (context.getMouseY() - this.dragStartMouseY);
                this.markDirty();
            } else
            {
                this.isDragging = false;
            }
        }


        float contentX = px + BORDER + HIERARCHY_PANEL_W;
        float contentY = py + HEADER_H;
        float contentW = pw - BORDER * 2.0F - HIERARCHY_PANEL_W;
        float contentH = ph - HEADER_H - FOOTER_H - BORDER;

        float hierarchyX = px + BORDER;
        float hierarchyY = contentY;
        this.hierarchyPanel.setDocked(hierarchyX, hierarchyY, HIERARCHY_PANEL_W, contentH);
        this.hierarchyPanel.updateLogic(context, deltaTime);

        float transformPadW = this.transformPad.getPreferredWidth();
        float transformPadH = this.transformPad.getPreferredHeight();
        float transformPadX = contentX + contentW - transformPadW - TRANSFORM_PAD_MARGIN;
        float transformPadY = contentY + (contentH - transformPadH) * 0.5F;
        this.transformPad.setDocked(transformPadX, transformPadY, transformPadW, transformPadH);
        this.transformPad.setTarget(this.viewport.getActiveTransformTarget());
        this.transformPad.updateLogic(context, deltaTime);

        boolean insideContent = context.getMouseX() >= contentX && context.getMouseX() < contentX + contentW && context.getMouseY() >= contentY && context.getMouseY() < contentY + contentH;

        boolean gizmoActive = this.viewport.isGizmoHovered() || this.viewport.isGizmoDragging();
        boolean transformPadHot = this.transformPad.containsPoint(context.getMouseX(), context.getMouseY()) || this.transformPad.isInteracting();

        if (insideContent && !this.isDragging && !this.isResizing && !transformPadHot)
        {
            float dx = context.getMouseDeltaX();
            float dy = context.getMouseDeltaY();

            if (context.isMouseDown() && !gizmoActive)
            {
                this.viewport.getCamera().orbit(dx, dy, ORBIT_SENSITIVITY);
            }
            if (context.isMiddleMouseDown() || context.isRightMouseDown())
            {
                this.viewport.getCamera().pan(dx, dy, PAN_SENSITIVITY);
            }
            if (context.isScrolling())
            {
                this.viewport.getCamera().zoom(context.getScrollDelta(), ZOOM_SENSITIVITY);
            }
        }

        int vpW = Math.max((int) contentW, (int) MIN_VIEWPORT_PX);
        int vpH = Math.max((int) contentH, (int) MIN_VIEWPORT_PX);
        this.viewportTextureHandle = this.viewport.render(context, contentX, contentY, vpW, vpH, deltaTime);
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        float x = this.cx;
        float y = this.cy;
        float w = this.cw;
        float h = this.ch;

        UITheme.Theme theme = UITheme.get();
        float[] accent = theme.accent();
        float[] border = theme.border();

        UIDropShadow.drawRounded(renderer, x, y + 4.0F, w, h, 0.0F, 6.0F, 0.45F, 12.0F);
        renderer.drawRoundedRect(x, y, w, h, border[0], border[1], border[2], 1.0F, 12.0F);
        renderer.drawRoundedRect(x + BORDER, y + BORDER, w - BORDER * 2.0F, h - BORDER * 2.0F, 0.07F, 0.07F, 0.08F, 0.97F, 11.0F);

        float hdrR = this.isDragging ? 0.17F : 0.13F;
        float hdrG = this.isDragging ? 0.20F : 0.16F;
        float hdrB = this.isDragging ? 0.25F : 0.20F;
        renderer.drawRoundedRect(x + BORDER, y + BORDER, w - BORDER * 2.0F, HEADER_H, hdrR, hdrG, hdrB, 1.0F, 11.0F);
        renderer.drawRoundedRect(x + BORDER, y + BORDER, w - BORDER * 2.0F, 1.5F, 0.55F, 0.57F, 0.65F, 0.16F, 1.0F);
        renderer.drawRect(x + BORDER, y + HEADER_H, w - BORDER * 2.0F, 1.0F, 0.22F, 0.25F, 0.30F, 0.65F);
        renderer.drawRoundedRect(x + BORDER + 1.0F, y + BORDER + 8.0F, 3.0F, h - BORDER * 2.0F - 16.0F, accent[0], accent[1], accent[2], 0.80F, 1.5F);
        renderer.drawRoundedRect(x + BORDER, y + h - FOOTER_H, w - BORDER * 2.0F, FOOTER_H - BORDER, 0.09F, 0.10F, 0.12F, 1.0F, 3.0F);
        renderer.drawRect(x + BORDER, y + h - FOOTER_H, w - BORDER * 2.0F, 1.0F, 0.20F, 0.22F, 0.27F, 0.45F);

        float dotY = y + HEADER_H * 0.5F - 1.5F;
        float dotStartX = x + w * 0.5F - 14.0F;
        float dotAlpha = this.isDragging ? 0.60F : 0.22F;
        for (int i = 0; i < 3; i++)
        {
            renderer.drawRoundedRect(dotStartX + i * 14.0F, dotY, 5.0F, 3.0F, 0.62F, 0.65F, 0.78F, dotAlpha, 1.5F);
        }

        float gr = lerp(0.55F, accent[0], this.gripHoverProgress);
        float gg = lerp(0.58F, accent[1], this.gripHoverProgress);
        float gb = lerp(0.68F, accent[2], this.gripHoverProgress);
        float ga = lerp(0.28F, 0.90F, this.gripHoverProgress);
        float gx = x + w - 5.0F;
        float gy = y + h - 4.0F;
        renderer.drawRect(gx - 14.0F, gy - 2.0F, 10.0F, 2.0F, gr, gg, gb, ga);
        renderer.drawRect(gx - 2.0F, gy - 14.0F, 2.0F, 10.0F, gr, gg, gb, ga);

        float contentX = x + BORDER + HIERARCHY_PANEL_W;
        float contentY = y + HEADER_H;
        float contentW = w - BORDER * 2.0F - HIERARCHY_PANEL_W;
        float contentH = h - HEADER_H - FOOTER_H - BORDER;

        renderer.drawRect(contentX, contentY, contentW, contentH, 0.0F, 0.0F, 0.0F, 1.0F);

        if (this.viewportTextureHandle != 0)
        {
            renderer.drawFramebufferTexture(contentX, contentY, contentW, contentH,
                    this.viewportTextureHandle, 1.0F, 1.0F, 1.0F, 1.0F);
        }

        renderer.drawRoundedRect(contentX - 1.0F, contentY - 1.0F, contentW + 2.0F, contentH + 2.0F, 0.20F, 0.22F, 0.28F, 0.35F, 2.0F);

        renderer.drawRect(contentX - 1.0F, contentY, 1.0F, contentH, 0.22F, 0.25F, 0.30F, 0.65F);
    }

    @Override
    public void collectInteractable(List<UIElement> out)
    {
        if (!this.isVisible() || !this.isEnabled()) return;
        this.titleLabel.collectInteractable(out);
        this.hierarchyPanel.collectInteractable(out);
        this.transformPad.collectInteractable(out);
        if (this.getBlocksInput()) out.add(this);
    }

    public void setModel(IModelAsset model)
    {
        this.viewport.setModel(model);
        if (model != null)
        {
            this.titleLabel.setText("Model Editor - " + model.getDisplayName());
            this.setVisible(true);
        } else
        {
            this.titleLabel.setText("Model Editor");
            this.transformPad.setTarget(null);
            this.setVisible(false);
        }
    }

    public void cleanup()
    {
        this.transformPad.cleanup();
        this.viewport.cleanup();
    }

    private static float approach(float current, float target, float factor)
    {
        float next = current + (target - current) * factor;
        return Math.abs(target - next) < 0.01F ? target : next;
    }

    private static float clamp(float v, float min, float max)
    {
        return Math.max(min, Math.min(max, v));
    }

    private static float lerp(float a, float b, float t)
    {
        return a + (b - a) * t;
    }
}
