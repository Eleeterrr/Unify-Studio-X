package eleeter.unifystudiox.amb;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import eleeter.unifystudiox.scene.io.SerializeProperty;

public class AnimatrixGroup
{
    public String name;
    public Vector3f origin;
    public AnimatrixGroup parent;
    public List<AnimatrixGroup> children = new ArrayList<>();
    public List<AnimatrixCube> cubes = new ArrayList<>();

    @SerializeProperty
    public Vector3f translation = new Vector3f(0);
    @SerializeProperty
    public Vector3f rotation = new Vector3f(0);
    @SerializeProperty
    public Vector3f scale = new Vector3f(1, 1, 1);

    public Matrix4f localTransform = new Matrix4f();
    public Matrix4f worldTransform = new Matrix4f();

    public AnimatrixGroup(String name, Vector3f origin)
    {
        this.name = name;
        this.origin = origin;
    }
}
