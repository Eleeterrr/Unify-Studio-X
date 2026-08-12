package eleeter.unifystudiox.cubic;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

public class CubicGroup
{
    public final String id;
    public final String name;
    public final Vector3f pivot;
    public final Vector3f rotation;
    public CubicGroup parent;
    public final List<CubicGroup> children = new ArrayList<>();
    public final List<CubeElement> elements = new ArrayList<>();
    public int boneIndex = -1;

    public CubicGroup(String id, String name, Vector3f pivot)
    {
        this(id, name, pivot, new Vector3f(0, 0, 0));
    }

    public CubicGroup(String id, String name, Vector3f pivot, Vector3f rotation)
    {
        this.id = id;
        this.name = name;
        this.pivot = new Vector3f(pivot);
        this.rotation = new Vector3f(rotation);
    }

    public void addChild(CubicGroup child)
    {
        child.parent = this;
        this.children.add(child);
    }

    public void addElement(CubeElement element)
    {
        element.parent = this;
        this.elements.add(element);
    }
}
