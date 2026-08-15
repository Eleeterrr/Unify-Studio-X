package eleeter.unifystudiox.graphics.math;

import java.util.ArrayList;
import java.util.List;

public class GeometryBuilder
{
    private final List<Float> vertices = new ArrayList<>();
    private final List<Integer> indices = new ArrayList<>();
    private final int stride;

    public GeometryBuilder(int stride)
    {
        if (stride <= 0)
        {
            throw new IllegalArgumentException("Stride must be greater than zero");
        }

        this.stride = stride;
    }

    public int vertex(float... values)
    {
        if (values.length != this.stride)
        {
            throw new IllegalArgumentException("Expected " + this.stride + " values, got " + values.length);
        }

        int index = this.vertices.size() / this.stride;

        for (float value : values)
        {
            this.vertices.add(value);
        }

        return index;
    }

    public GeometryBuilder triangle(int a, int b, int c)
    {
        this.indices.add(a);
        this.indices.add(b);
        this.indices.add(c);
        return this;
    }

    public GeometryBuilder quad(int a, int b, int c, int d)
    {
        return triangle(a, b, c).triangle(a, c, d);
    }

    public GeometryData build()
    {
        float[] vertexArray = new float[this.vertices.size()];
        int[] indexArray = new int[this.indices.size()];

        for (int i = 0; i < this.vertices.size(); i++)
        {
            vertexArray[i] = this.vertices.get(i);
        }

        for (int i = 0; i < this.indices.size(); i++)
        {
            indexArray[i] = this.indices.get(i);
        }

        return new GeometryData(vertexArray, indexArray, this.stride);
    }
}