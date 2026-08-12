package eleeter.unifystudiox.gltf.model;

import java.util.ArrayList;
import java.util.List;


public class GltfNode
{
    public int index;
    public String name;
    public int meshIndex = -1;
    public int skinIndex = -1;

    public float[] matrix;
    public float[] translation;
    public float[] rotation;
    public float[] scale;

    public final List<Integer> children = new ArrayList<>();
}
