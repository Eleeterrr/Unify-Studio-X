package eleeter.unifystudiox.anchor;

import org.joml.Matrix4f;

public interface BoneTarget
{
    Matrix4f getBoneWorldMatrix(String boneName);

    Matrix4f getBoneWorldMatrix(int boneIndex);

    default Matrix4f getBoneWorldMatrixOrRoot(String boneName)
    {
        Matrix4f m = getBoneWorldMatrix(boneName);
        return (m != null) ? m : getRootWorldMatrix();
    }

    Matrix4f getRootWorldMatrix();
}
