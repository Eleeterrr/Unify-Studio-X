package eleeter.unifystudiox.scene.entity;

import eleeter.unifystudiox.scene.SpotlightData;
import eleeter.unifystudiox.scene.io.SerializeProperty;
import org.joml.Vector3f;

public class SpotlightEntity extends BaseSceneEntity implements Positionable
{
    private final String id;

    @SerializeProperty
    private final SpotlightData data = new SpotlightData();

    public SpotlightEntity(String id)
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

        Vector3f localForward = new Vector3f(0f, -1f, 0f);
        getRotation().transform(localForward, this.data.direction);
        this.data.direction.normalize();

        this.data.updateLightSpaceMatrix();
    }

    @Override
    public void cleanup()
    {
        super.cleanup();
    }

    public SpotlightData getData()
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

    public SpotlightEntity setPosition(float x, float y, float z)
    {
        getPosition().set(x, y, z);
        if (this.transform != null) this.transform.isDirty = true;
        return this;
    }

    public SpotlightEntity setColor(float r, float g, float b)
    {
        this.data.setColor(r, g, b);
        return this;
    }

    public SpotlightEntity setIntensity(float intensity)
    {
        this.data.setIntensity(intensity);
        return this;
    }

    public SpotlightEntity setCutoff(float innerDeg, float outerDeg)
    {
        this.data.setCutoff(innerDeg, outerDeg);
        return this;
    }

    public SpotlightEntity setRange(float range)
    {
        this.data.setRange(range);
        return this;
    }

    public SpotlightEntity setLightEnabled(boolean enabled)
    {
        this.data.setEnabled(enabled);
        return this;
    }

    public SpotlightEntity setCastShadow(boolean castShadow)
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
