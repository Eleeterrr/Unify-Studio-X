package eleeter.unifystudiox.ui;

import java.util.ArrayList;
import java.util.List;

import eleeter.unifystudiox.renderer.core.RenderSettings;
import eleeter.unifystudiox.settings.SettingEntry;
import eleeter.unifystudiox.settings.SettingsRegistry;
import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.render.UIOverlay;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.settings.SettingsBuilder;
import eleeter.unifystudiox.ui.theme.UIDropShadow;
import eleeter.unifystudiox.ui.widgets.UIDropdown;
import eleeter.unifystudiox.ui.widgets.UIScrollPanel;

public class SettingsPanel extends UIOverlay
{
    private static final float HEADER_H = 28.0F;
    private static final float BORDER = 1.0F;

    private boolean lastEscState;
    private final UIScrollPanel scrollPanel;
    private SettingsBuilder currentBuilder;
    private int lastRegistrySize = -1;
    private boolean dirtyRebuild = false;

    public SettingsPanel(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        super("settings_panel", context, 600.0F, 500.0F, 400.0F, 300.0F);
        this.setBlocksInput(true);
        this.setVisible(false);
        this.setZIndex(100);

        this.scrollPanel = new UIScrollPanel("settings_scroll");
        this.scrollPanel.getTransform().set(0.0F, 0.0F, 0.0F, 0.0F);
        this.addChild(this.scrollPanel);
    }


    @Override
    protected float getHeaderDragHeight()
    {
        return HEADER_H;
    }

    public void setLastEscState(boolean state)
    {
        this.lastEscState = state;
    }

    public void requestRebuild()
    {
        this.dirtyRebuild = true;
    }

    @Override
    public void setZIndex(int z)
    {
        super.setZIndex(z);
        if (this.scrollPanel != null)
        {
            this.scrollPanel.setZIndex(z + 1);
            if (this.scrollPanel.getContent() != null)
            {
                this.scrollPanel.getContent().setZIndex(z + 2);
                List<UIElement> allChildren = this.scrollPanel.getContent().findChildrenByClass(UIElement.class);
                for (UIElement child : allChildren)
                {
                    child.setZIndex(z + 3);
                }
            }
        }
    }

    @Override
    protected void onUpdateSelf(float deltaTime)
    {
        int ox = 50 + (int) this.panelOffsetX + (int) BORDER;
        int oy = 50 + (int) this.panelOffsetY + (int) HEADER_H + 1;
        int sw = (int) this.panelWidth - (int) (BORDER * 2.0F);
        int sh = (int) this.panelHeight - (int) HEADER_H - 15;

        this.scrollPanel.getTransform().setPixelOffset(ox, oy).setPixelSize(sw, sh);
        this.scrollPanel.markDirty();

        List<SettingEntry<?>> allSettings = SettingsRegistry.getAllSettings();

        if (this.dirtyRebuild || this.lastRegistrySize != allSettings.size())
        {
            this.dirtyRebuild = false;
            this.lastRegistrySize = allSettings.size();
            this.scrollPanel.clearChildren();

            this.currentBuilder = new SettingsBuilder(this.scrollPanel, this.context);

            for (SettingEntry<?> entry : allSettings)
            {
                this.currentBuilder.addSetting(entry);
            }

            this.scrollPanel.setMaxContentHeight(this.currentBuilder.getTotalHeight());

            this.setZIndex(this.getZIndex());
        }

        if (this.currentBuilder != null)
        {
            this.currentBuilder.update();
        }

        if (this.scrollPanel != null && this.scrollPanel.getContent() != null)
        {
            List<UIElement> children = this.scrollPanel.getContent().findChildrenByClass(UIElement.class);
            boolean enabledState = !RenderSettings.VSYNC_ENABLED;
            for (UIElement child : children)
            {
                if ("graphics.fps_limit_widget".equals(child.getId()) || "graphics.fps_limit_label".equals(child.getId()))
                {
                    child.setEnabled(enabledState);
                }
            }
        }
    }

    @Override
    public void updateLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        boolean escDown = context.isKeyPressed(UIKey.ESCAPE);
        if (escDown && !this.lastEscState)
        {
            this.setVisible(!this.isVisible());
        }
        this.lastEscState = escDown;

        if (this.isVisible())
        {
            super.updateLogic(context, deltaTime);
        }
    }


    @Override
    public void render(UIRenderer renderer)
    {
        if (!this.isVisible())
        {
            return;
        }

        float x = this.getComputedX() + 50.0F + this.panelOffsetX;
        float y = this.getComputedY() + 50.0F + this.panelOffsetY;

        renderer.pushClip(x, y, this.panelWidth, this.panelHeight);

        this.renderSelf(renderer);

        List<UIElement> snapshot = new ArrayList<>(this.getChildren());
        for (UIElement child : snapshot)
        {
            child.render(renderer);
        }

        renderer.popClip();

        List<UIDropdown> dropdowns = this.scrollPanel.findChildrenByClass(UIDropdown.class);
        for (UIDropdown dropdown : dropdowns)
        {
            dropdown.renderPopup(renderer);
        }
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        float currentX = this.getComputedX();
        float currentY = this.getComputedY();

        float x = currentX + 50.0F + this.panelOffsetX;
        float y = currentY + 50.0F + this.panelOffsetY;
        float w = this.panelWidth;
        float h = this.panelHeight;

        /* Deep ambient shadow */
        UIDropShadow.drawRounded(renderer, x, y + 4.0F, w, h, 0.0F, 7.0F, 0.50F, 14.0F);

        /* Outer border ring */
        renderer.drawRoundedRect(x, y, w, h, 0.13F, 0.14F, 0.17F, 1.0F, 13.0F);

        /* Main dark body */
        renderer.drawRoundedRect(x + BORDER, y + BORDER, w - BORDER * 2.0F, h - BORDER * 2.0F, 0.10F, 0.11F, 0.13F, 0.97F, 12.0F);

        /* Header zone */
        renderer.drawRoundedRect(x + BORDER, y + BORDER, w - BORDER * 2.0F, HEADER_H, 0.13F, 0.16F, 0.20F, 1.0F, 11.0F);

        /* Glass rim highlight at very top */
        renderer.drawRoundedRect(x + BORDER, y + BORDER, w - BORDER * 2.0F, 1.5F, 0.55F, 0.57F, 0.65F, 0.16F, 1.0F);

        /* Header / content separator */
        renderer.drawRect(x + BORDER, y + HEADER_H, w - BORDER * 2.0F, 1.0F, 0.22F, 0.25F, 0.30F, 0.65F);

        /* Left accent stripe */
        renderer.drawRoundedRect(x + BORDER + 1.0F, y + BORDER + 8.0F, 3.0F, h - BORDER * 2.0F - 16.0F, 0.28F, 0.52F, 0.98F, 0.80F, 1.5F);

        /* Drag dots in header centre */
        float dotY = y + HEADER_H * 0.5F - 1.5F;
        float dotStartX = x + w * 0.5F - 14.0F;
        float dotAlpha = this.isDragging() ? 0.60F : 0.22F;
        for (int i = 0; i < 3; i++)
        {
            renderer.drawRoundedRect(dotStartX + (float) i * 14.0F, dotY, 5.0F, 3.0F, 0.62F, 0.65F, 0.78F, dotAlpha, 1.5F);
        }

        /* Bottom Right Resize Grip lines */
        float gripAlpha = this.isResizing() ? 0.90F : 0.30F;
        renderer.drawRect(x + w - 6.0F, y + h - 16.0F, 2.0F, 10.0F, 1.0F, 1.0F, 1.0F, gripAlpha);
        renderer.drawRect(x + w - 11.0F, y + h - 6.0F, 10.0F, 2.0F, 1.0F, 1.0F, 1.0F, gripAlpha);
    }
}
