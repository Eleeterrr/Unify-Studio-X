package eleeter.unifystudiox.gltf.model;

import java.util.ArrayList;
import java.util.List;


public class GltfSkin
{
    public int index;
    public String name;
    public int inverseBindMatricesAccessorIndex = -1;
    public int skeletonNodeIndex = -1;
    public final List<Integer> joints = new ArrayList<>();
}
