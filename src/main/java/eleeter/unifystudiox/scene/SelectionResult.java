package eleeter.unifystudiox.scene;

import eleeter.unifystudiox.scene.entity.SceneEntity;


public record SelectionResult(
    SceneEntity entity,
    float distance,
    int subIndex,
    Object extraData
)
{
    public static SelectionResult empty()
    {
        return new SelectionResult(null, Float.MAX_VALUE, -1, null);
    }

    public boolean hasHit()
    {
        return entity != null;
    }
}
