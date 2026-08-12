package eleeter.unifystudiox.resource;

import eleeter.unifystudiox.scene.entity.SceneEntity;

public interface IAssetBinder
{
    /**
     * Attempts to find and apply related assets to the given entity.
     */
    void bind(SceneEntity entity);
}
