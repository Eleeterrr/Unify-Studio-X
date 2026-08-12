package eleeter.unifystudiox.scene.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public interface SkeletalData
{
    float[] getBoneMatrices();
    
    int getBoneCount();
    
    Vector3f getJointPosition(int boneIndex);

    int getParentIndex(int boneIndex);

    default Matrix4f getBoneWorldMatrix(int boneIndex)
    {
        return new Matrix4f();
    }
}
