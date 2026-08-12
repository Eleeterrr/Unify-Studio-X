package eleeter.unifystudiox.ui.framework.render;

import eleeter.unifystudiox.ui.framework.style.UIStyleStateSpec;

/**
 * Decoupled, swappable painting interface for rendering standard geometric UI components
 * using an abstract layout area boundary and visual style specification.
 */
public interface IUIPainter
{

    void drawPanel(Region area, UIStyleStateSpec style);

    void drawHeader(Region area, UIStyleStateSpec style);

    void drawScrollbar(Region track, Region thumb, UIStyleStateSpec style);

    void drawTile(Region area, UIStyleStateSpec style, boolean isHovered, boolean isSelected);

    void drawLine(float x1, float y1, float x2, float y2, float thickness, float[] color);
}
