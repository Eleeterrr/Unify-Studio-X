package eleeter.unifystudiox.obj;

import java.util.ArrayList;
import java.util.List;

public class ObjModel
{
    private final String sourcePath;
    private final List<ObjMesh> meshes;

    public ObjModel(String sourcePath)
    {
        this.sourcePath = sourcePath;
        this.meshes = new ArrayList<>();
    }

    public String getSourcePath()
    {
        return this.sourcePath;
    }

    public List<ObjMesh> getMeshes()
    {
        return this.meshes;
    }

    public void addMesh(ObjMesh mesh)
    {
        this.meshes.add(mesh);
    }
}
