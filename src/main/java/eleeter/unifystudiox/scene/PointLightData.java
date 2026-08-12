package eleeter.unifystudiox.scene;

import org.joml.Vector3f;

import eleeter.unifystudiox.scene.io.SerializeProperty;

public class PointLightData
{

    public final Vector3f position = new Vector3f(0f, 5f, 0f);

    @SerializeProperty
    public final Vector3f color = new Vector3f(1f, 1f, 1f);

    @SerializeProperty
    public float intensity = 3.0f;


    @SerializeProperty
    public float range = 10.0F;

    @SerializeProperty
    public boolean enabled = true;

    @SerializeProperty
    public boolean castShadow = false;

    public PointLightData setPosition(float x, float y, float z)
    {
        this.position.set(x, y, z);
        return this;
    }

    public PointLightData setColor(float r, float g, float b)
    {
        this.color.set(r, g, b);
        return this;
    }

    public PointLightData setIntensity(float intensity)
    {
        this.intensity = intensity;
        return this;
    }

    public PointLightData setRange(float range)
    {
        this.range = range;
        return this;
    }

    public PointLightData setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }
}
