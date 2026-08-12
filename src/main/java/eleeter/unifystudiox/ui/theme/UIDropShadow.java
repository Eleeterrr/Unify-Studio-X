package eleeter.unifystudiox.ui.theme;

import eleeter.unifystudiox.ui.framework.render.UIRenderer;


public class UIDropShadow
{
    private static final int LAYERS = 5;

    private UIDropShadow()
    {
    }

    public static void draw(UIRenderer renderer, float x, float y, float w, float h, float offsetX, float offsetY, float alpha)
    {
        for (int i = 0; i < UIDropShadow.LAYERS; i++)
        {
            float spread = (float) (i + 1) * 2.0F;
            float layerAlpha = alpha * (1.0F - (float) i / (float) UIDropShadow.LAYERS);
            
            renderer.drawRect(
                x + offsetX - spread, 
                y + offsetY - spread, 
                w + spread * 2.0F, 
                h + spread * 2.0F, 
                0.0F, 0.0F, 0.0F, layerAlpha
            );
        }
    }

    public static void drawRounded(UIRenderer renderer, float x, float y, float w, float h, float offsetX, float offsetY, float alpha, float radius)
    {
        for (int i = 0; i < UIDropShadow.LAYERS; i++)
        {
            float spread = (float) (i + 1) * 2.0F;
            float layerAlpha = alpha * (1.0F - (float) i / (float) UIDropShadow.LAYERS);
            
            renderer.drawRoundedRect(
                x + offsetX - spread, 
                y + offsetY - spread, 
                w + spread * 2.0F, 
                h + spread * 2.0F, 
                0.0F, 0.0F, 0.0F, layerAlpha,
                radius + spread
            );
        }
    }
}
