package eleeter.unifystudiox.scene.entity;

import eleeter.unifystudiox.scene.PointLightData;
import eleeter.unifystudiox.scene.io.SerializeProperty;
import org.joml.Vector3f;

public class PointLightEntity extends BaseSceneEntity implements Positionable
{
    private final String id;

    /* light data */
    @SerializeProperty
    private final PointLightData data = new PointLightData();

    public PointLightEntity(String id)
    {
        this.id = id;
        setPosition(0f, 3f, 0f);
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public void update(double deltaTime)
    {
        this.data.position.set(getPosition());
    }

    @Override
    public void cleanup()
    {
        super.cleanup();
    }

    public PointLightData getData()
    {
        return this.data;
    }

    public float getIntensity()
    {
        return this.data.intensity;
    }

    public float getRange()
    {
        return this.data.range;
    }

    public Vector3f getColor()
    {
        return this.data.color;
    }

    public PointLightEntity setPosition(float x, float y, float z)
    {
        getPosition().set(x, y, z);
        if (this.transform != null) this.transform.isDirty = true;
        return this;
    }


    public PointLightEntity setColor(float r, float g, float b)
    {
        this.data.setColor(r, g, b);
        return this;
    }

    public PointLightEntity setIntensity(float intensity)
    {
        this.data.setIntensity(intensity);
        return this;
    }

    public PointLightEntity setRange(float range)
    {
        this.data.setRange(range);
        return this;
    }

    public PointLightEntity setLightEnabled(boolean enabled)
    {
        this.data.setEnabled(enabled);
        return this;
    }

    public PointLightEntity setCastShadow(boolean castShadow)
    {
        this.data.castShadow = castShadow;
        return this;
    }


    @Override
    public void setVisible(boolean visible)
    {
        super.setVisible(visible);
    }
}
