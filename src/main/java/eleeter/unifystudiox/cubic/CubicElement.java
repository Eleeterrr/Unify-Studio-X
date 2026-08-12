package eleeter.unifystudiox.cubic;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.joml.Vector3f;

public class CubicElement extends CubeElement
{
    public final Vector3f from;
    public final Vector3f to;
    public final Vector3f origin;
    public final Vector3f rotation;
    public final Map<String, CubicFace> faces;

    public CubicElement(String id, String name, Vector3f from, Vector3f to, Vector3f origin, Vector3f rotation, Map<String, CubicFace> faces)
    {
        super(id, name, "cube");
        this.from = new Vector3f(from);
        this.to = new Vector3f(to);
        this.origin = new Vector3f(origin);
        this.rotation = new Vector3f(rotation);
        this.faces = Collections.unmodifiableMap(new LinkedHashMap<>(faces));
    }
}
