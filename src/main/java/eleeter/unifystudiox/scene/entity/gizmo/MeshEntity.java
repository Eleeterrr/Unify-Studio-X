package eleeter.unifystudiox.scene.entity.gizmo;

import eleeter.unifystudiox.scene.entity.BaseSceneEntity;
import eleeter.unifystudiox.scene.entity.Positionable;

public class MeshEntity extends BaseSceneEntity implements Positionable
{

    private final String id;
    private final String meshPath;

    public MeshEntity(String id, String meshPath)
    {
        this.id = id;
        this.meshPath = meshPath;
    }


    @Override
    public String getId()
    {
        return id;
    }


    @Override
    public void update(double deltaTime)
    {
    }

    @Override
    public String getAssetPath()
    {
        return this.meshPath;
    }

    @Override
    public void cleanup()
    {
    }


    public MeshEntity setPosition(float x, float y, float z)
    {
        getPosition().set(x, y, z);
        if (this.transform != null) this.transform.isDirty = true;
        return this;
    }

    public MeshEntity setRotation(float angleRad, float axisX, float axisY, float axisZ)
    {
        getRotation().fromAxisAngleRad(axisX, axisY, axisZ, angleRad);
        if (this.transform != null) this.transform.isDirty = true;
        return this;
    }

    public MeshEntity setScale(float uniform)
    {
        getScale().set(uniform, uniform, uniform);
        if (this.transform != null) this.transform.isDirty = true;
        return this;
    }

    public String getMeshPath()
    {
        return this.meshPath;
    }
}
