package eleeter.unifystudiox.ecs.components;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import eleeter.unifystudiox.ecs.Component;
import eleeter.unifystudiox.scene.io.SerializeProperty;


public class TransformComponent implements Component
{

    @SerializeProperty
    public final Vector3f localPosition = new Vector3f();
    @SerializeProperty
    public final Quaternionf localRotation = new Quaternionf();
    @SerializeProperty
    public final Vector3f localScale = new Vector3f(1.0f, 1.0f, 1.0f);

    public final Vector3f worldPosition = new Vector3f();
    public final Quaternionf worldRotation = new Quaternionf();
    public final Vector3f worldScale = new Vector3f(1.0f, 1.0f, 1.0f);

    public final Matrix4f modelMatrix = new Matrix4f();

    public boolean isDirty = true;

    public TransformComponent()
    {
    }

    public TransformComponent(Vector3f position, Quaternionf rotation, Vector3f scale)
    {
        this.localPosition.set(position);
        this.localRotation.set(rotation);
        this.localScale.set(scale);
    }
}
