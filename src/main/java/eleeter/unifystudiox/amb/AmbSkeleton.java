package eleeter.unifystudiox.amb;

import java.util.ArrayList;
import java.util.List;

import org.joml.*;

public class AmbSkeleton
{
    public List<AmbBone> bones = new ArrayList<>();

    public AmbBone getBone(int index)
    {

        if (index < 0 || index >= this.bones.size())
        {
            return null;
        }
        return this.bones.get(index);
    }

    public void calculateInverseBindMatrices()
    {
        calculateGlobalBindTransforms();
        for (int i = 0; i < this.bones.size(); i++)
        {
            AmbBone bone = this.bones.get(i);
            bone.inverseBindMatrix.set(bone.globalMatrix).invert();
        }
    }

    public void calculateGlobalBindTransforms()
    {
        for (int i = 0; i < this.bones.size(); i++)
        {
            AmbBone bone = this.bones.get(i);
            bone.globalMatrix.set(bone.bindLocalMatrix);
        }
    }

    public void convertGlobalToLocalBind()
    {

        List<Matrix4f> globalMics = new ArrayList<>();
        for (AmbBone bone : this.bones)
        {
            globalMics.add(new Matrix4f(bone.bindLocalMatrix));
        }

        for (int i = 0; i < this.bones.size(); i++)
        {
            AmbBone bone = this.bones.get(i);
            if (bone.parentIndex != -1)
            {
                Matrix4f parentGlobal = globalMics.get(bone.parentIndex);
                Matrix4f invParent = new Matrix4f(parentGlobal).invert();
                invParent.mul(bone.bindLocalMatrix, bone.bindLocalMatrix);
            }

            bone.localTranslation.set(0, 0, 0);
            bone.localRotation.set(0, 0, 0);
            bone.localScale.set(1, 1, 1);
        }
    }

    public void calculateGlobalTransforms()
    {
        boolean[] updated = new boolean[this.bones.size()];
        for (int i = 0; i < this.bones.size(); i++)
        {
            updateBone(i, updated);
        }
    }

    private void updateBone(int index, boolean[] updated)
    {
        if (updated[index])
        {
            return;
        }

        AmbBone bone = this.bones.get(index);
        if (bone.parentIndex != -1)
        {
            updateBone(bone.parentIndex, updated);
        }

        Matrix4f userOffset = new Matrix4f().translation(bone.localTranslation)
                .rotateXYZ(bone.localRotation.x, bone.localRotation.y, bone.localRotation.z).scale(bone.localScale);

        Matrix4f local = new Matrix4f(bone.bindLocalMatrix).mul(userOffset);

        if (bone.parentIndex == -1)
        {
            bone.globalMatrix.set(local);
        }
        else
        {
            AmbBone parent = this.bones.get(bone.parentIndex);
            parent.globalMatrix.mul(local, bone.globalMatrix);
        }

        updated[index] = true;
    }

    public AmbSkeleton copy()
    {
        AmbSkeleton copy = new AmbSkeleton();
        for (AmbBone bone : this.bones)
        {
            copy.bones.add(bone.copy());
        }
        return copy;
    }
}
