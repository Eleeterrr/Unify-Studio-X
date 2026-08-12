package eleeter.unifystudiox.ui.framework;

import eleeter.unifystudiox.ui.framework.render.UIRenderer;

/**
 * Interface representing a dynamic, floating tooltip overlay.
 */
public interface IUITooltip
{
    /**
     * Renders the custom tooltip elements onto the screen-space overlay.
     */
    void render(UIRenderer renderer, float x, float y, float width, float height, float alpha,
                float p0x, float p0y, float p1x, float p1y, float p2x, float p2y);

    /**
     * Returns the physical width of the tooltip box.
     */
    float getWidth();

    /**
     * Returns the physical height of the tooltip box.
     */
    float getHeight();

    /**
     * Frees any GPU resources allocated by the tooltip when it is hidden.
     */
    default void destroy() {}
}
