package eleeter.unifystudiox.ui.menu;

import eleeter.unifystudiox.ui.ShapeDraw;

@FunctionalInterface
public interface IconDrawer
{
    void draw(ShapeDraw shape, float cx, float cy, float size, float thickness);
}
