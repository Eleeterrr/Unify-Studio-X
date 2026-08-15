package eleeter.unifystudiox.graphics.math;

public class GeometryData
{
    private final float[] vertices;
    private final int[] indices;
    private final int vertexStride;

    public GeometryData(float[] vertices, int[] indices, int vertexStride)
    {
        this.vertices = vertices;
        this.indices = indices;
        this.vertexStride = vertexStride;
    }

    public float[] vertices()
    {
        return this.vertices;
    }

    public int[] indices()
    {
        return this.indices;
    }

    public int vertexStride()
    {
        return this.vertexStride;
    }

    public int vertexCount()
    {
        return this.vertices.length / this.vertexStride;
    }

    public int indexCount()
    {
        return this.indices.length;
    }
}