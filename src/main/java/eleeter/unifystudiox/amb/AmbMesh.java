package eleeter.unifystudiox.amb;

public class AmbMesh
{

    public String name;

    public float[] vertexData;

    public int[] indices;

    public AmbMesh(String name, float[] vertexData, int[] indices)
    {
        this.name = name;
        this.vertexData = vertexData;
        this.indices = indices;
    }
}
