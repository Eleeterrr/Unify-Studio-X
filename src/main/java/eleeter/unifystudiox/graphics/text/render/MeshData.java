package eleeter.unifystudiox.graphics.text.render;

public class MeshData
{
    private final eleeter.elfontlib.render.MeshData handle;
    public final float[] vertices;
    public final int[] indices;

    public MeshData(eleeter.elfontlib.render.MeshData handle)
    {
        this.handle = handle;
        this.vertices = handle != null ? handle.vertices : null;
        this.indices = handle != null ? handle.indices : null;
    }

    public eleeter.elfontlib.render.MeshData getHandle()
    {
        return this.handle;
    }
}
