package eleeter.unifystudiox.ui.framework.render.context;

import eleeter.unifystudiox.input.InputHandler;
import eleeter.unifystudiox.ui.UIKey;
import eleeter.unifystudiox.ui.framework.IUITooltip;
import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.framework.render.UIRootPanel;
import eleeter.unifystudiox.ui.menu.IContextMenuProvider;
import eleeter.unifystudiox.ui.menu.UIContextMenu;

public class UISystem
{
    private final UIRootPanel root = new UIRootPanel();
    private final UIInputContext context = new UIInputContext();
    private final UIRenderer renderer;

    private IUITooltip activeTooltip = null;
    private UIElement lastHovered = null;
    private double hoverTimer = 0.0D;
    private float tooltipAlpha = 0.0F;
    private float tooltipSpawnX = 0.0F;
    private float tooltipSpawnY = 0.0F;
    private double lastDeltaTime = 0.016D;

    private UIContextMenu activeContextMenu = null;

    private boolean rendererReady = false;
    private boolean isVisible = true;

    public UISystem(UIRenderer renderer)
    {
        this.renderer = renderer;
    }

    public void processInput(InputHandler input, float screenW, float screenH)
    {
        if (input.isKeyPressed(UIKey.TOGGLE_UI))
        {
            this.isVisible = !this.isVisible;
        }

        if (!this.isVisible)
        {
            this.context.processInput(input, new UIRootPanel(), null);
            return;
        }

        this.root.setScreenBounds(screenW, screenH);
        this.root.updateLayout(0.0F, 0.0F, screenW, screenH);


        if (this.activeContextMenu != null)
        {
            this.activeContextMenu.updateLayout(0.0F, 0.0F, screenW, screenH);
        }

        this.context.processInput(input, this.root, this.activeContextMenu);
    }

    public void update(double deltaTime, float screenW, float screenH)
    {
        this.lastDeltaTime = deltaTime;

        if (!this.isVisible)
        {
            return;
        }

        if (this.activeContextMenu != null)
        {
            this.activeContextMenu.updateLayout(0.0F, 0.0F, screenW, screenH);
            this.activeContextMenu.updateLogic(this.context, deltaTime);
        }

        this.root.updateLogic(this.context, deltaTime);

        this.root.setScreenBounds(screenW, screenH);

        this.root.updateLayout(0.0F, 0.0F, screenW, screenH);

        UIElement currentHovered = this.context.getHoveredElement();


        if (currentHovered != this.lastHovered || this.context.isMouseDragging())
        {
            this.hoverTimer = 0.0D;
            this.lastHovered = currentHovered;
        } else if (currentHovered != null && currentHovered.getTooltip() != null)
        {
            this.hoverTimer += deltaTime;
        } else
        {
            this.hoverTimer = 0.0D;
        }

        if (this.hoverTimer >= 0.5D && this.lastHovered != null && this.lastHovered.getTooltip() != null)
        {
            IUITooltip newTooltip = this.lastHovered.getTooltip();
            if (newTooltip != this.activeTooltip)
            {
                if (this.activeTooltip != null)
                {
                    this.activeTooltip.destroy();
                }
                this.activeTooltip = newTooltip;
                this.tooltipSpawnX = this.context.getMouseX();
                this.tooltipSpawnY = this.context.getMouseY();
            }
        } else
        {
            if (this.activeTooltip != null)
            {
                this.activeTooltip.destroy();
                this.activeTooltip = null;
            }
        }

        if (this.context.isRightMousePressed())
        {
            UIElement target = currentHovered;
            IContextMenuProvider provider = null;

            while (target != null)
            {
                if (target instanceof IContextMenuProvider)
                {
                    provider = (IContextMenuProvider) target;
                    break;
                }
                target = target.getParent();
            }

            if (provider != null)
            {
                UIContextMenu newMenu = provider.getContextMenu(this.context);
                if (newMenu != null)
                {
                    if (this.activeContextMenu != null)
                    {
                        this.activeContextMenu.close();
                    }
                    this.activeContextMenu = newMenu;
                    this.activeContextMenu.show(this.context.getMouseX(), this.context.getMouseY(), screenW, screenH);
                }
            } else if (this.activeContextMenu != null)
            {
                // Right clicked somewhere else, close the menu
                if (!this.context.isHoveredWithin(this.activeContextMenu))
                {
                    this.activeContextMenu.close();
                    this.activeContextMenu = null;
                }
            }
        } else if (this.context.isMousePressed())
        {
            if (this.activeContextMenu != null)
            {
                if (!this.context.isHoveredWithin(this.activeContextMenu))
                {
                    this.activeContextMenu.close();
                    this.activeContextMenu = null;
                }
            }
        }

        if (this.activeContextMenu != null && !this.activeContextMenu.isVisible())
        {
            this.activeContextMenu = null;
        }
    }

    public void render(float logicalW, float logicalH, float physicalW, float physicalH)
    {
        if (!this.isVisible)
        {
            return;
        }

        if (!this.rendererReady)
        {
            this.renderer.init();
            this.rendererReady = true;
        }

        this.renderer.beginFrame(logicalW, logicalH, physicalW, physicalH);

        this.root.setScreenBounds(logicalW, logicalH);
        this.root.updateLayout(0.0F, 0.0F, logicalW, logicalH);

        this.root.render(this.renderer);

        if (this.activeContextMenu != null)
        {
            this.activeContextMenu.updateLayout(0.0F, 0.0F, logicalW, logicalH);
            this.activeContextMenu.render(this.renderer);
        }

        if (this.activeTooltip != null && this.lastHovered != null)
        {
            this.tooltipAlpha = Math.min(1.0F, this.tooltipAlpha + (float) this.lastDeltaTime * 8.0F);

            float elemX = this.lastHovered.getComputedX();
            float elemY = this.lastHovered.getComputedY();
            float elemW = this.lastHovered.getComputedWidth();
            float elemH = this.lastHovered.getComputedHeight();

            float boxW = this.activeTooltip.getWidth();
            float boxH = this.activeTooltip.getHeight();

            float gap = 6.0F;
            float arrowH = 6.0F;

            float boxX = 0.0F;
            float boxY = 0.0F;

            float p0x = 0.0F, p0y = 0.0F;
            float p1x = 0.0F, p1y = 0.0F;
            float p2x = 0.0F, p2y = 0.0F;

            if (elemX + elemW / 2.0F < 80.0F)
            {
                boxX = elemX + elemW + gap + arrowH;
                boxY = elemY + (elemH - boxH) / 2.0F;

                boxY = Math.max(4.0F, Math.min(boxY, logicalH - boxH - 4.0F));

                p0x = elemX + elemW + gap;
                p0y = elemY + elemH / 2.0F;
                p1x = boxX;
                p1y = p0y - 5.0F;
                p2x = boxX;
                p2y = p0y + 5.0F;
            } else if (elemX + elemW / 2.0F > logicalW - 80.0F)
            {
                boxX = elemX - boxW - gap - arrowH;
                boxY = elemY + (elemH - boxH) / 2.0F;

                boxY = Math.max(4.0F, Math.min(boxY, logicalH - boxH - 4.0F));

                p0x = elemX - gap;
                p0y = elemY + elemH / 2.0F;
                p1x = boxX + boxW;
                p1y = p0y - 5.0F;
                p2x = boxX + boxW;
                p2y = p0y + 5.0F;
            }
            else
            {
                boxX = elemX + (elemW - boxW) / 2.0F;
                boxY = elemY - boxH - gap - arrowH;

                boxX = Math.max(4.0F, Math.min(boxX, logicalW - boxW - 4.0F));

                if (boxY < 4.0F)
                {
                    boxY = elemY + elemH + gap + arrowH;

                    p0x = elemX + elemW / 2.0F;
                    p0y = elemY + elemH + gap;
                    p1x = p0x - 5.0F;
                    p1y = boxY;
                    p2x = p0x + 5.0F;
                    p2y = boxY;
                } else
                {
                    p0x = elemX + elemW / 2.0F;
                    p0y = elemY - gap;
                    p1x = p0x - 5.0F;
                    p1y = boxY + boxH;
                    p2x = p0x + 5.0F;
                    p2y = boxY + boxH;
                }
            }

            this.activeTooltip.render(this.renderer, boxX, boxY, boxW, boxH, this.tooltipAlpha, p0x, p0y, p1x, p1y, p2x, p2y);
        } else
        {
            this.tooltipAlpha = 0.0F;
        }

        this.renderer.endFrame();
    }

    public void cleanup()
    {
        if (this.activeTooltip != null)
        {
            this.activeTooltip.destroy();
            this.activeTooltip = null;
        }
        this.renderer.cleanup();
    }

    public UIRootPanel getRoot()
    {
        return this.root;
    }

    public UIInputContext getContext()
    {
        return this.context;
    }

    public boolean isGamePaused()
    {
        return this.root.doesAnyChildPauseGame();
    }

    public boolean isVisible()
    {
        return this.isVisible;
    }

    public void showContextMenu(UIContextMenu menu, float x, float y, float screenW, float screenH)
    {
        if (this.activeContextMenu != null)
        {
            this.activeContextMenu.close();
        }

        this.activeContextMenu = menu;
        if (this.activeContextMenu != null)
        {
            this.activeContextMenu.show(x, y, screenW, screenH);
        }
    }
}
