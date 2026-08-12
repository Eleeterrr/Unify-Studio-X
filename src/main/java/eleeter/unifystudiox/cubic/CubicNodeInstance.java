package eleeter.unifystudiox.cubic;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import eleeter.unifystudiox.cubic.render.CubeRuntimeNode;
import eleeter.unifystudiox.scene.entity.BaseSceneEntity;
import eleeter.unifystudiox.scene.entity.Positionable;

public class CubicNodeInstance extends BaseSceneEntity implements Positionable
{
    private final String id;
    private final CubeRuntimeNode node;
    private final CubicModelInstance parent;

    public CubicNodeInstance(String modelId, CubeRuntimeNode node, CubicModelInstance parent)
    {
        this.id = modelId + ":" + node.id;
        this.node = node;
        this.parent = parent;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public Matrix4f getModelMatrix()
    {

        return this.node.worldMatrix;
    }

    @Override
    public void update(double deltaTime)
    {
    }

    @Override
    public boolean isVisible()
    {
        return true;
    }

    @Override
    public Vector3f getPosition()
    {
        Matrix4f worldBase = this.parent.getWorldMatrix();
        Vector3f pivotPos = new Vector3f();
        this.node.worldMatrix.transformPosition(this.node.pivot, pivotPos);

        Vector3f worldPivot = new Vector3f();
        worldBase.transformPosition(pivotPos, worldPivot);

        return worldPivot;
    }

    @Override
    public void setPosition(Vector3f position)
    {
        Matrix4f worldBase = this.parent.getWorldMatrix();
        Matrix4f parentWorld = worldBase;
        if (this.node.parent != null)
        {
            parentWorld = new Matrix4f(worldBase).mul(this.node.parent.worldMatrix);
        }

        Vector3f localTarget = new Vector3f();
        parentWorld.invert(new Matrix4f()).transformPosition(position, localTarget);

        this.node.translation.set(localTarget.x - this.node.pivot.x, localTarget.y - this.node.pivot.y, localTarget.z - this.node.pivot.z);
        this.node.isManuallyControlled = true;

        this.parent.update(0);
    }

    @Override
    public Quaternionf getRotation()
    {

        Quaternionf worldRot = new Quaternionf(this.node.rotation);

        CubeRuntimeNode current = this.node.parent;
        while (current != null)
        {
            new Quaternionf(current.rotation).mul(worldRot, worldRot);
            current = current.parent;
        }

        return new Quaternionf(this.parent.getRotation()).mul(worldRot);
    }

    @Override
    public void setRotation(Quaternionf rotation)
    {
        Quaternionf parentWorldRot = new Quaternionf();

        CubeRuntimeNode current = this.node.parent;
        while (current != null)
        {
            new Quaternionf(current.rotation).mul(parentWorldRot, parentWorldRot);
            current = current.parent;
        }
        new Quaternionf(this.parent.getRotation()).mul(parentWorldRot, parentWorldRot);

        parentWorldRot.invert().mul(rotation, this.node.rotation);

        this.node.isManuallyControlled = true;
        this.parent.update(0);
    }

    @Override
    public Vector3f getScale()
    {
        Vector3f worldScale = new Vector3f();
        Matrix4f entityTransform = new Matrix4f(this.parent.getModelMatrix()).scale(0.0625f);
        Matrix4f fullWorld = new Matrix4f(entityTransform).mul(this.node.worldMatrix);
        fullWorld.getScale(worldScale);
        return worldScale;
    }

    @Override
    public void setScale(Vector3f scale)
    {
        Vector3f parentWorldScale = new Vector3f();
        Matrix4f entityTransform = new Matrix4f(this.parent.getModelMatrix()).scale(0.0625f);

        if (this.node.parent != null)
        {
            new Matrix4f(entityTransform).mul(this.node.parent.worldMatrix).getScale(parentWorldScale);
        } else
        {
            entityTransform.getScale(parentWorldScale);
        }

        this.node.scale.set(scale.x / parentWorldScale.x, scale.y / parentWorldScale.y, scale.z / parentWorldScale.z);
        this.node.isManuallyControlled = true;

        this.parent.update(0);
    }

    @Override
    public Vector3f getLocalPosition()
    {
        return new Vector3f(this.node.translation);
    }

    @Override
    public void setLocalPosition(Vector3f position)
    {
        this.node.translation.set(position);
        this.node.isManuallyControlled = true;
        this.parent.update(0);
    }

    @Override
    public Quaternionf getLocalRotation()
    {
        return new Quaternionf(this.node.rotation);
    }

    @Override
    public void setLocalRotation(Quaternionf rotation)
    {
        this.node.rotation.set(rotation);
        this.node.isManuallyControlled = true;
        this.parent.update(0);
    }

    @Override
    public Vector3f getLocalScale()
    {
        return new Vector3f(this.node.scale);
    }

    @Override
    public void setLocalScale(Vector3f scale)
    {
        this.node.scale.set(scale);
        this.node.isManuallyControlled = true;
        this.parent.update(0);
    }

    @Override
    public void cleanup()
    {
    }

    public CubeRuntimeNode getNode()
    {
        return this.node;
    }
}
