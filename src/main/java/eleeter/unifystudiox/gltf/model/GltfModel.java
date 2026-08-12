package eleeter.unifystudiox.gltf.model;

import java.util.ArrayList;
import java.util.List;


public class GltfModel
{
    public final List<GltfBuffer> buffers = new ArrayList<>();
    public final List<GltfBufferView> bufferViews = new ArrayList<>();
    public final List<GltfAccessor> accessors = new ArrayList<>();
    public final List<GltfMesh> meshes = new ArrayList<>();
    public final List<GltfNode> nodes = new ArrayList<>();
    public final List<GltfSkin> skins = new ArrayList<>();
    public final List<Integer> sceneRoots = new ArrayList<>();
}
