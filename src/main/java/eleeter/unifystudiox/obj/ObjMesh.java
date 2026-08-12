package eleeter.unifystudiox.obj;

import org.joml.Vector3f;

public class ObjMesh
{
    private final String name;
    private final float[] vertexData;
    private final int[] indices;
    private ObjMaterial material;

    private final Vector3f aabbMin = new Vector3f(Float.MAX_VALUE);
    private final Vector3f aabbMax = new Vector3f(-Float.MAX_VALUE);

    public ObjMesh(String name, float[] vertexData, int[] indices)
    {
        this.name = name;
        this.vertexData = vertexData;
        this.indices = indices;
        

        int stride = 8;
        for (int i = 0; i < vertexData.length; i += stride)
        {
            float x = vertexData[i];
            float y = vertexData[i + 1];
            float z = vertexData[i + 2];
            
            this.aabbMin.x = Math.min(this.aabbMin.x, x);
            this.aabbMin.y = Math.min(this.aabbMin.y, y);
            this.aabbMin.z = Math.min(this.aabbMin.z, z);
            
            this.aabbMax.x = Math.max(this.aabbMax.x, x);
            this.aabbMax.y = Math.max(this.aabbMax.y, y);
            this.aabbMax.z = Math.max(this.aabbMax.z, z);
        }
    }

    public String getName()
    {
        return this.name;
    }

    public float[] getVertexData()
    {
        return this.vertexData;
    }

    public int[] getIndices()
    {
        return this.indices;
    }

    public ObjMaterial getMaterial()
    {
        return this.material;
    }

    public void setMaterial(ObjMaterial material)
    {
        this.material = material;
    }

    public Vector3f getAabbMin()
    {
        return this.aabbMin;
    }

    public Vector3f getAabbMax()
    {
        return this.aabbMax;
    }
}
