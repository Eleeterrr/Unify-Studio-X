package eleeter.unifystudiox.amb;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import eleeter.unifystudiox.scene.entity.SkeletalData;


public class AmbSkeletalData implements SkeletalData
{
    private final AmbModelInstance instance;



    public AmbSkeletalData(AmbModelInstance instance)
    {
        this.instance = instance;
    }

    @Override
    public float[] getBoneMatrices()
    {
        return this.instance.boneMatrices;
    }

    @Override
    public int getBoneCount()
    {
        return this.instance.sourceModel.skeleton.bones.size();
    }

    @Override
    public Vector3f getJointPosition(int boneIndex)
    {
        if (boneIndex < 0 || boneIndex >= getBoneCount())
        {
            return new Vector3f();
        }

        AmbBone bone = this.instance.sourceModel.skeleton.bones.get(boneIndex);
        Vector3f pos = new Vector3f();
        bone.globalMatrix.getTranslation(pos);

        Vector3f correctedPos = new Vector3f();

        Vector3f worldPos = new Vector3f();
        this.instance.getModelMatrix().transformPosition(correctedPos, worldPos);
        
        return worldPos;
    }

    @Override
    public int getParentIndex(int boneIndex)
    {
        if (boneIndex < 0 || boneIndex >= getBoneCount())
        {
            return -1;
        }
        return this.instance.sourceModel.skeleton.bones.get(boneIndex).parentIndex;
    }

    @Override
    public Matrix4f getBoneWorldMatrix(int boneIndex)
    {
        if (boneIndex < 0 || boneIndex >= getBoneCount())
        {
            return new Matrix4f();
        }
        AmbBone bone = this.instance.getSkeleton().bones.get(boneIndex);
        return new Matrix4f(this.instance.getModelMatrix()).mul(bone.globalMatrix);
    }
}
