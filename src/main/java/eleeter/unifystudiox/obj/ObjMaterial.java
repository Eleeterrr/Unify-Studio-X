package eleeter.unifystudiox.obj;

public class ObjMaterial
{
    private final String name;
    private String diffuseTexturePath;

    public ObjMaterial(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

    public String getDiffuseTexturePath()
    {
        return this.diffuseTexturePath;
    }

    public void setDiffuseTexturePath(String diffuseTexturePath)
    {
        this.diffuseTexturePath = diffuseTexturePath;
    }
}
