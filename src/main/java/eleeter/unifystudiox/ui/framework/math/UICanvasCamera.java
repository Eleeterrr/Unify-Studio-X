package eleeter.unifystudiox.ui.framework.math;


public class UICanvasCamera
{

    private float panX = 0.0F;
    private float panY = 0.0F;
    private float zoom = 1.0F;

    private float targetPanX = 0.0F;
    private float targetPanY = 0.0F;
    private float targetZoom = 1.0F;

    private float velocityX = 0.0F;
    private float velocityY = 0.0F;

    private float zoomMin = 0.05F;
    private float zoomMax = 20.0F;
    private float lerpFactor = 15.0F;
    private float friction = 0.9F;

    public UICanvasCamera()
    {
    }

    public UICanvasCamera(float panX, float panY, float zoom)
    {
        this.set(panX, panY, zoom);
    }

    public void set(float panX, float panY, float zoom)
    {
        this.panX = panX;
        this.panY = panY;
        this.zoom = zoom;
        this.targetPanX = panX;
        this.targetPanY = panY;
        this.targetZoom = zoom;
        this.velocityX = 0.0F;
        this.velocityY = 0.0F;
    }

    public float toLocalX(float screenX)
    {
        return (screenX - this.panX) / this.zoom;
    }

    public float toLocalY(float screenY)
    {
        return (screenY - this.panY) / this.zoom;
    }


    public float toScreenX(float localX)
    {
        return (localX * this.zoom) + this.panX;
    }

    public float toScreenY(float localY)
    {
        return (localY * this.zoom) + this.panY;
    }


    public void zoomTowards(float screenMouseX, float screenMouseY, float zoomDelta)
    {
        float localMouseX = this.toLocalX(screenMouseX);
        float localMouseY = this.toLocalY(screenMouseY);

        this.targetZoom = Math.max(this.zoomMin, Math.min(this.zoomMax, this.targetZoom + zoomDelta));

        this.targetPanX = screenMouseX - (localMouseX * this.targetZoom);
        this.targetPanY = screenMouseY - (localMouseY * this.targetZoom);

        this.velocityX = 0.0F;
        this.velocityY = 0.0F;
    }


    public void drag(float dx, float dy)
    {
        this.targetPanX += dx;
        this.targetPanY += dy;
        this.velocityX = dx;
        this.velocityY = dy;
    }


    public void update(double deltaTime)
    {
        float dt = (float) deltaTime;
        if (dt <= 0.0F)
        {
            return;
        }

        float zoomDiff = this.targetZoom - this.zoom;
        if (Math.abs(zoomDiff) > 0.0001F)
        {
            this.zoom += zoomDiff * Math.min(1.0F, this.lerpFactor * dt);
        } else
        {
            this.zoom = this.targetZoom;
        }

        if (Math.abs(this.velocityX) > 0.1F || Math.abs(this.velocityY) > 0.1F)
        {
            this.targetPanX += this.velocityX;
            this.targetPanY += this.velocityY;
            this.panX = this.targetPanX;
            this.panY = this.targetPanY;

            this.velocityX *= Math.pow(this.friction, dt * 60.0F);
            this.velocityY *= Math.pow(this.friction, dt * 60.0F);
        } else
        {
            this.velocityX = 0.0F;
            this.velocityY = 0.0F;

            float panXDiff = this.targetPanX - this.panX;
            float panYDiff = this.targetPanY - this.panY;

            if (Math.abs(panXDiff) > 0.01F)
            {
                this.panX += panXDiff * Math.min(1.0F, this.lerpFactor * dt);
            } else
            {
                this.panX = this.targetPanX;
            }

            if (Math.abs(panYDiff) > 0.01F)
            {
                this.panY += panYDiff * Math.min(1.0F, this.lerpFactor * dt);
            } else
            {
                this.panY = this.targetPanY;
            }
        }
    }

    public float getPanX()
    {
        return this.panX;
    }

    public float getPanY()
    {
        return this.panY;
    }

    public float getZoom()
    {
        return this.zoom;
    }

    public float getTargetPanX()
    {
        return this.targetPanX;
    }

    public float getTargetPanY()
    {
        return this.targetPanY;
    }

    public float getTargetZoom()
    {
        return this.targetZoom;
    }

    public void setZoomLimits(float min, float max)
    {
        this.zoomMin = min;
        this.zoomMax = max;
    }

    public void setLerpFactor(float lerp)
    {
        this.lerpFactor = lerp;
    }

    public void setFriction(float friction)
    {
        this.friction = friction;
    }
}
