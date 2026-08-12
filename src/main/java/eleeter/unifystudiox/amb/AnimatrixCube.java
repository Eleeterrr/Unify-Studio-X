package eleeter.unifystudiox.amb;

import java.util.Map;

import org.joml.Vector3f;

public class AnimatrixCube
{
    public Vector3f origin;
    public Vector3f from;
    public Vector3f size;
    public Map<String, float[]> uvs;
    public float offset;
    public AnimatrixMesh mesh;
    public AnimatrixGroup parent;

    public AnimatrixCube(Vector3f origin, Vector3f from, Vector3f size, float offset, Map<String, float[]> uvs)
    {
        this.origin = origin;
        this.from = from;
        this.size = size;
        this.offset = offset;
        this.uvs = uvs;
    }
}
