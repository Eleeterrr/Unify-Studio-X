package eleeter.unifystudiox.amb;

public class AnimatrixMesh
{
    public float[] vertices;
    public float[] uvs;
    public int[] indices;

    public AnimatrixMesh(float[] vertices, float[] uvs, int[] indices)
    {
        this.vertices = vertices;
        this.uvs = uvs;
        this.indices = indices;
    }
}
