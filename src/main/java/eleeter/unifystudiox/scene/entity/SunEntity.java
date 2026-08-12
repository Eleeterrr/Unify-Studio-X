package eleeter.unifystudiox.scene.entity;

import eleeter.unifystudiox.scene.io.SerializeProperty;
import org.joml.Vector3f;

public class SunEntity extends BaseSceneEntity
{

    private final String id = "sys_sun";
    @SerializeProperty
    private final Vector3f direction = new Vector3f(0.5f, 20.0f, -0.5f).normalize();
    @SerializeProperty
    private final Vector3f color = new Vector3f(1.0f, 0.95f, 0.8f);
    @SerializeProperty
    private float size = 50.0f;
    @SerializeProperty
    private float intensity = 1.2f;

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public void update(double deltaTime)
    {
    }

    public Vector3f getDirection()
    {
        return this.direction;
    }

    public Vector3f getColor()
    {
        return this.color;
    }

    public float getSize()
    {
        return this.size;
    }

    public float getIntensity()
    {
        return this.intensity;
    }

    public void setDirection(float x, float y, float z)
    {
        this.direction.set(x, y, z).normalize();
    }

    public void setColor(float r, float g, float b)
    {
        this.color.set(r, g, b);
    }

    public void setSize(float size)
    {
        this.size = size;
    }

    public void setIntensity(float intensity)
    {
        this.intensity = intensity;
    }
}
