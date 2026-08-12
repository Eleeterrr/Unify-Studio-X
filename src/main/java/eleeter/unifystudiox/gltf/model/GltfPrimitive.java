package eleeter.unifystudiox.gltf.model;

import java.util.HashMap;
import java.util.Map;


public class GltfPrimitive
{
    public int indicesAccessorIndex = -1;
    public int materialIndex = -1;
    public int mode = 4; // 4 = TRIANGLES

    public final Map<String, Integer> attributes = new HashMap<>();
}
