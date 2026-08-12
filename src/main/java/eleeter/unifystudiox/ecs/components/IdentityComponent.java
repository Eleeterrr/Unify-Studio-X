package eleeter.unifystudiox.ecs.components;

import eleeter.unifystudiox.ecs.Component;


public class IdentityComponent implements Component
{

    public String id;
    public String assetPath;

    public IdentityComponent()
    {
    }

    public IdentityComponent(String id, String assetPath)
    {
        this.id = id;
        this.assetPath = assetPath;
    }
}
