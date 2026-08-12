package eleeter.unifystudiox.amb;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import eleeter.unifystudiox.scene.entity.BaseSceneEntity;
import eleeter.unifystudiox.scene.entity.Positionable;

public class AmbBoneInstance extends BaseSceneEntity implements Positionable
{
    private final String id;
    private final AmbBone bone;
    private final AmbModelInstance parent;
    private Vector3f worldPivot;

    public AmbBoneInstance(String modelId, AmbBone bone, AmbModelInstance parent)
    {
        this.id = modelId + ":" + bone.name;
        this.bone = bone;
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
        return this.bone.globalMatrix;
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
        this.worldPivot = new Vector3f();
        Matrix4f fullWorld = new Matrix4f(this.parent.getModelMatrix()).mul(this.bone.globalMatrix);
        fullWorld.transformPosition(new Vector3f(0, 0, 0), worldPivot);

        return worldPivot;
    }

    @Override
    public void setPosition(Vector3f position)
    {
        Matrix4f worldToModel = new Matrix4f(this.parent.getModelMatrix()).invert();
        Vector3f modelPos = new Vector3f();
        worldToModel.transformPosition(position, modelPos);

        Matrix4f parentGlobalInv = new Matrix4f();
        if (this.bone.parentIndex != -1)
        {
            AmbBone parentBone = this.parent.getSkeleton().bones.get(this.bone.parentIndex);
            parentBone.globalMatrix.invert(parentGlobalInv);
        } else
        {
            parentGlobalInv.identity();
        }

        Vector3f boneParentSpacePos = new Vector3f();
        parentGlobalInv.transformPosition(modelPos, boneParentSpacePos);

        Matrix4f invBindLocal = new Matrix4f(this.bone.bindLocalMatrix).invert();
        Vector3f offsetPos = new Vector3f();
        invBindLocal.transformPosition(boneParentSpacePos, offsetPos);

        this.bone.localTranslation.set(offsetPos);
        this.parent.update();
    }

    @Override
    public Quaternionf getRotation()
    {
        Matrix4f fullWorld = new Matrix4f(this.parent.getModelMatrix()).mul(this.bone.globalMatrix);
        Quaternionf worldRot = new Quaternionf();
        fullWorld.getNormalizedRotation(worldRot);
        return worldRot;
    }

    @Override
    public void setRotation(Quaternionf rotation)
    {
        Quaternionf modelWorldRot = new Quaternionf();
        this.parent.getModelMatrix().getNormalizedRotation(modelWorldRot);

        Quaternionf parentBoneGlobalRot = new Quaternionf();
        if (this.bone.parentIndex != -1)
        {
            AmbBone parentBone = this.parent.getSkeleton().bones.get(this.bone.parentIndex);
            parentBone.globalMatrix.getNormalizedRotation(parentBoneGlobalRot);
        } else
        {
            parentBoneGlobalRot.identity();
        }

        Quaternionf modelSpaceRot = new Quaternionf(modelWorldRot).invert().mul(rotation);
        Quaternionf boneParentSpaceRot = new Quaternionf(parentBoneGlobalRot).invert().mul(modelSpaceRot);

        Quaternionf bindLocalRot = new Quaternionf();
        this.bone.bindLocalMatrix.getNormalizedRotation(bindLocalRot);

        Quaternionf offsetRot = new Quaternionf(bindLocalRot).invert().mul(boneParentSpaceRot);
        offsetRot.getEulerAnglesXYZ(this.bone.localRotation);
        this.parent.update();
    }

    @Override
    public Vector3f getScale()
    {
        Vector3f worldScale = new Vector3f();
        Matrix4f fullWorld = new Matrix4f(this.parent.getModelMatrix()).mul(this.bone.globalMatrix);
        fullWorld.getScale(worldScale);
        return worldScale;
    }

    @Override
    public void setScale(Vector3f scale)
    {
        Vector3f parentWorldScale = new Vector3f();
        Matrix4f parentWorld = new Matrix4f(this.parent.getModelMatrix());

        if (this.bone.parentIndex != -1)
        {
            AmbBone parentBone = this.parent.getSkeleton().bones.get(this.bone.parentIndex);
            parentWorld.mul(parentBone.globalMatrix);
        }
        parentWorld.getScale(parentWorldScale);

        this.bone.localScale.set(scale.x / parentWorldScale.x, scale.y / parentWorldScale.y,
                scale.z / parentWorldScale.z);
        this.parent.update();
    }

    @Override
    public Vector3f getLocalPosition()
    {
        return new Vector3f(this.bone.localTranslation);
    }

    @Override
    public Quaternionf getLocalRotation()
    {
        return new Quaternionf().rotationXYZ(this.bone.localRotation.x, this.bone.localRotation.y,
                this.bone.localRotation.z);
    }

    @Override
    public Vector3f getLocalScale()
    {
        return new Vector3f(this.bone.localScale);
    }

    @Override
    public void cleanup()
    {
    }

    public AmbBone getBone()
    {
        return this.bone;
    }
}
