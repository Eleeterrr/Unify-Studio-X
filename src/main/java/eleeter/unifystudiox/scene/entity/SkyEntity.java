package eleeter.unifystudiox.scene.entity;

import eleeter.unifystudiox.renderer.core.RenderSettings;
import eleeter.unifystudiox.scene.io.SerializeProperty;
import org.joml.Vector3f;

public class SkyEntity extends BaseSceneEntity
{

    private final String id = "sys_sky";

    @SerializeProperty
    private final Vector3f topColor = new Vector3f(0.15f, 0.35f, 0.70f); // Zenith
    @SerializeProperty
    private final Vector3f bottomColor = new Vector3f(0.40f, 0.35f, 0.30f); // Nadir
    @SerializeProperty
    private final Vector3f horizonColor = new Vector3f(0.70f, 0.80f, 0.90f); // Horizon

    @SerializeProperty
    private float haze = 0.15f;
    @SerializeProperty
    private float sunSize = 1.0f;

    private final Vector3f sunDirection = new Vector3f(0, 1, 0);
    @SerializeProperty
    private SunEntity linkedSun;

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public void update(double deltaTime)
    {
        if (this.linkedSun != null)
        {
            this.sunDirection.set(this.linkedSun.getDirection());
        }
    }


    public Vector3f getTopColor()
    {
        if (RenderSettings.NIGHT_MODE) return new Vector3f(0.01f, 0.02f, 0.05f);
        return this.topColor;
    }

    public Vector3f getBottomColor()
    {
        if (RenderSettings.NIGHT_MODE) return new Vector3f(0.03f, 0.04f, 0.08f);
        return this.bottomColor;
    }

    public Vector3f getHorizonColor()
    {
        if (RenderSettings.NIGHT_MODE) return new Vector3f(0.03f, 0.05f, 0.14f);
        return this.horizonColor;
    }

    public float getHaze()
    {
        return this.haze;
    }

    public float getSunSize()
    {
        if (RenderSettings.NIGHT_MODE) return this.sunSize * 0.4f; // Smaller moon
        return this.sunSize;
    }

    public Vector3f getSunDirection()
    {
        return this.sunDirection;
    }

    public void setSun(SunEntity sun)
    {
        this.linkedSun = sun;
    }

    public void setTopColor(float r, float g, float b)
    {
        this.topColor.set(r, g, b);
    }

    public void setBottomColor(float r, float g, float b)
    {
        this.bottomColor.set(r, g, b);
    }

    public void setHorizonColor(float r, float g, float b)
    {
        this.horizonColor.set(r, g, b);
    }

    public void setHaze(float haze)
    {
        this.haze = haze;
    }

    public void setSunSize(float sunSize)
    {
        this.sunSize = sunSize;
    }
}
