package eleeter.unifystudiox.assets;

import eleeter.unifystudiox.scene.entity.SceneEntity;

public interface IModelAssetAdapter
{
    IModelAsset tryCreate(SceneEntity entity);
}
