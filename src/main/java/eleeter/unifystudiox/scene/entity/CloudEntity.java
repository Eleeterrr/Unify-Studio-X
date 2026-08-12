package eleeter.unifystudiox.scene.entity;

import org.joml.Vector3f;

import eleeter.unifystudiox.renderer.core.RenderSettings;

public class CloudEntity extends BaseSceneEntity
{
    private final String id;
    private final Vector3f windDir = new Vector3f(1.0f, 0.0f, 0.3f).normalize();

    public CloudEntity(String id)
    {
        this.id = id;
    }

    @Override public String getId()
    {
        return this.id;
    }

    @Override public void update(double dt)
    {}

    @Override public boolean isVisible()
    {
        return RenderSettings.CLOUDS_ENABLED;
    }

    public Vector3f getWindDir()
    {
        return this.windDir;
    }

    public CloudEntity setWindDir(float x, float y, float z)
    {
        this.windDir.set(x, y, z).normalize();
        return this;
    }
}
