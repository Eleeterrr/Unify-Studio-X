package eleeter.unifystudiox.amb;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class AmbBone
{
    public String name;
    public int parentIndex;
    public Matrix4f transform;

    public Vector3f localTranslation = new Vector3f(0, 0, 0);
    public Vector3f localRotation = new Vector3f(0, 0, 0);
    public Vector3f localScale = new Vector3f(1, 1, 1);

    public Matrix4f bindLocalMatrix = new Matrix4f();
    public Matrix4f globalMatrix = new Matrix4f();
    public Matrix4f inverseBindMatrix = new Matrix4f();

    public AmbBone(String name, int parentIndex, Matrix4f transform)
    {
        this.name = name;
        this.parentIndex = parentIndex;
        this.transform = new Matrix4f(transform);
        this.bindLocalMatrix = new Matrix4f(transform);

        transform.getTranslation(this.localTranslation);
        transform.getEulerAnglesXYZ(this.localRotation);
        transform.getScale(this.localScale);
    }

    public AmbBone copy()
    {
        AmbBone bone = new AmbBone(this.name, this.parentIndex, this.transform);
        bone.localTranslation.set(this.localTranslation);
        bone.localRotation.set(this.localRotation);
        bone.localScale.set(this.localScale);
        bone.bindLocalMatrix.set(this.bindLocalMatrix);
        bone.globalMatrix.set(this.globalMatrix);
        bone.inverseBindMatrix.set(this.inverseBindMatrix);
        return bone;
    }
}
