package eleeter.unifystudiox.ecs.components;

import eleeter.unifystudiox.ecs.Component;


public class VisibilityComponent implements Component
{

    public boolean visible = true;

    public VisibilityComponent()
    {
    }

    public VisibilityComponent(boolean visible)
    {
        this.visible = visible;
    }
}
