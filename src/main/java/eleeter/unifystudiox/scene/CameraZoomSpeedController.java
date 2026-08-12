package eleeter.unifystudiox.scene;

import eleeter.unifystudiox.input.InputHandler;
import eleeter.unifystudiox.ui.UIKey;
import eleeter.unifystudiox.ui.overlay.UICameraZoom;

public class CameraZoomSpeedController
{
    public static final float MIN_MULTIPLIER = 0.1F;
    public static final float MAX_MULTIPLIER = 10.0F;
    public static final float STEP = 0.1F;
    private static final float DEFAULT_MULTIPLIER = 1.0F;

    private float multiplier = DEFAULT_MULTIPLIER;

    
    public void update(InputHandler input, UICameraZoom indicator)
    {
        boolean isCtrlHeld = input.isKeyHeld(UIKey.LEFT_CONTROL)
                || input.isKeyHeld(UIKey.RIGHT_CONTROL);

        if (isCtrlHeld)
        {
            float scroll = (float) input.getScrollDelta();

            if (scroll != 0.0F)
            {
                this.multiplier = Math.max(MIN_MULTIPLIER,
                        Math.min(MAX_MULTIPLIER, this.multiplier + scroll * STEP));
            }
        }

        if (indicator != null)
        {
            indicator.setCtrlHeld(isCtrlHeld);
            indicator.setMultiplier(this.multiplier);
        }
    }

    
    public float getMultiplier()
    {
        return this.multiplier;
    }
}
