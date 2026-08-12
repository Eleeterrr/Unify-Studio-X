package eleeter.unifystudiox.ui.framework;

public class UITransform
{
    /* Relative layout (0.0 – 1.0 within parent) */
    private float relX = 0f;
    private float relY = 0f;
    private float relW = 1f;
    private float relH = 1f;

    /* Anchor (pivot on THIS element used for alignment) */
    private float anchorX = 0f;
    private float anchorY = 0f;

    /* Optional fine-tune in pixels */
    private int pixelOffsetX = 0;
    private int pixelOffsetY = 0;
    private float pixelWidth = 0f;
    private float pixelHeight = 0f;

    /* Computed absolute screen-space output */
    public float computedX;
    public float computedY;
    public float computedWidth;
    public float computedHeight;

    public UITransform set(float relX, float relY, float relW, float relH)
    {
        this.relX = relX;
        this.relY = relY;
        this.relW = relW;
        this.relH = relH;
        return this;
    }

    public UITransform setAnchor(float anchorX, float anchorY)
    {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        return this;
    }

    public UITransform setPixelOffset(int ox, int oy)
    {
        this.pixelOffsetX = ox;
        this.pixelOffsetY = oy;
        return this;
    }

    public UITransform setPixelSize(float pw, float ph)
    {
        this.pixelWidth = pw;
        this.pixelHeight = ph;
        return this;
    }

    public void compute(float parentX, float parentY, float parentW, float parentH)
    {
        this.computedWidth  = (parentW * this.relW) + this.pixelWidth;
        this.computedHeight = (parentH * this.relH) + this.pixelHeight;
        this.computedX = parentX + (parentW * this.relX) - (this.computedWidth  * this.anchorX) + this.pixelOffsetX;
        this.computedY = parentY + (parentH * this.relY) - (this.computedHeight * this.anchorY) + this.pixelOffsetY;
    }

    public float getRelX()
    {
        return this.relX;
    }
    public float getRelY()
    {
        return this.relY;
    }
    public float getRelW()
    {
        return this.relW;
    }
    public float getRelH()
    {
        return this.relH;
    }
    public float getAnchorX()
    {
        return this.anchorX;
    }
    public float getAnchorY()
    {
        return this.anchorY;
    }
    public int getPixelOffsetX()
    {
        return this.pixelOffsetX;
    }
    public int getPixelOffsetY()
    {
        return this.pixelOffsetY;
    }
    public float getPixelWidth()
    {
        return this.pixelWidth;
    }
    public float getPixelHeight()
    {
        return this.pixelHeight;
    }

    public float getComputedX()
    {
        return this.computedX;
    }
    public float getComputedY()
    {
        return this.computedY;
    }
    public float getComputedW()
    {
        return this.computedWidth;
    }
    public float getComputedH()
    {
        return this.computedHeight;
    }
}
