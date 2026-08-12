package eleeter.unifystudiox.anchor;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class AnchorDefinition
{
    public final BoneTarget target;
    public final String boneName;
    public final int boneIndex;

    public final Vector3f offsetTranslation = new Vector3f(0, 0, 0);
    public final Quaternionf offsetRotation = new Quaternionf();
    public final Vector3f offsetScale = new Vector3f(1, 1, 1);

    AnchorDefinition(BoneTarget target, String boneName, int boneIndex)
    {
        this.target = target;
        this.boneName = boneName;
        this.boneIndex = boneIndex;
    }

    public Matrix4f resolvePureBoneWorldMatrix()
    {
        Matrix4f boneMatrix;

        if (this.boneIndex >= 0)
        {
            boneMatrix = this.target.getBoneWorldMatrix(this.boneIndex);
        } else if (this.boneName != null)
        {
            boneMatrix = this.target.getBoneWorldMatrixOrRoot(this.boneName);
        } else
        {
            boneMatrix = this.target.getRootWorldMatrix();
        }

        if (boneMatrix == null) boneMatrix = this.target.getRootWorldMatrix();

        return new Matrix4f(boneMatrix);
    }

    public Matrix4f resolveWorldMatrix()
    {
        Matrix4f boneMatrix = resolvePureBoneWorldMatrix();

        Matrix4f offset = new Matrix4f()
                .translate(this.offsetTranslation)
                .rotate(this.offsetRotation)
                .scale(this.offsetScale);

        return boneMatrix.mul(offset);
    }
}
