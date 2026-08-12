package eleeter.unifystudiox.cubic;

public abstract class CubeElement
{
    public final String id;
    public final String name;
    public final String type;
    public CubicGroup parent;

    protected CubeElement(String id, String name, String type)
    {
        this.id = id;
        this.name = name;
        this.type = type;
    }
}
