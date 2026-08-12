package eleeter.unifystudiox.ui.framework.style;

/**
 * Immutable visual style specification values for a particular UI element state.
 * Defines colors, borders, accenting, and roundings cleanly, decoupled from rendering logic.
 */
public class UIStyleStateSpec
{

    public final float[] backgroundColor;
    public final float[] borderColor;
    public final float borderThickness;
    public final float cornerRadius;
    public final float[] accentColor;

    public UIStyleStateSpec(float[] backgroundColor, float[] borderColor, float borderThickness, float cornerRadius, float[] accentColor)
    {
        this.backgroundColor = backgroundColor.clone();
        this.borderColor = borderColor.clone();
        this.borderThickness = borderThickness;
        this.cornerRadius = cornerRadius;
        this.accentColor = accentColor.clone();
    }
}
