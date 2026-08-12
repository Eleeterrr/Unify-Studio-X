package eleeter.unifystudiox.amb;

import java.util.ArrayList;
import java.util.List;

public class AmbModel
{
    public String filePath;
    public List<AmbMesh> meshes = new ArrayList<>();
    public AmbSkeleton skeleton = new AmbSkeleton();

    public AmbModel(String filePath)
    {
        this.filePath = filePath;
    }
}
