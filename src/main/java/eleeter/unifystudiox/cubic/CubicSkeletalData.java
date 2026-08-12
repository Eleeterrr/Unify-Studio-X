package eleeter.unifystudiox.cubic;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import eleeter.unifystudiox.cubic.render.CubeRuntimeModel;
import eleeter.unifystudiox.cubic.render.CubeRuntimeNode;
import eleeter.unifystudiox.scene.entity.SkeletalData;

public class CubicSkeletalData implements SkeletalData
{
    private final CubeRuntimeModel model;
    private final float[] boneMatrices;
    private final Matrix4f worldBase;

    public CubicSkeletalData(CubeRuntimeModel model, float[] boneMatrices, Matrix4f worldBase)
    {
        this.model = model;
        this.boneMatrices = boneMatrices;
        this.worldBase = worldBase;
    }

    @Override
    public float[] getBoneMatrices()
    {
        return this.boneMatrices;
    }

    @Override
    public int getBoneCount()
    {
        return Math.min(100, this.model.flattenedNodes.size());
    }

    @Override
    public Vector3f getJointPosition(int boneIndex)
    {
        if (boneIndex < 0 || boneIndex >= this.model.flattenedNodes.size())
        {
            return new Vector3f();
        }

        CubeRuntimeNode node = this.model.flattenedNodes.get(boneIndex);
        if (node == null)
        {
            return new Vector3f();
        }

        Vector3f pixelPivot = new Vector3f();
        node.worldMatrix.transformPosition(node.pivot, pixelPivot);

        pixelPivot.mul(0.0625f);

        Vector3f worldPivot = new Vector3f();
        this.worldBase.transformPosition(pixelPivot, worldPivot);
        return worldPivot;
    }

    @Override
    public int getParentIndex(int boneIndex)
    {
        if (boneIndex < 0 || boneIndex >= this.model.flattenedNodes.size())
        {
            return -1;
        }

        CubeRuntimeNode node = this.model.flattenedNodes.get(boneIndex);
        if (node == null || node.parent == null)
        {
            return -1;
        }

        return node.parent.boneIndex;
    }

    @Override
    public Matrix4f getBoneWorldMatrix(int boneIndex)
    {
        if (boneIndex < 0 || boneIndex >= this.model.flattenedNodes.size())
        {
            return new Matrix4f();
        }
        CubeRuntimeNode node = this.model.flattenedNodes.get(boneIndex);
        if (node == null)
        {
            return new Matrix4f();
        }
        return new Matrix4f(this.worldBase).scale(0.0625f).mul(node.worldMatrix).translate(node.pivot);
    }
}
