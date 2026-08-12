package eleeter.unifystudiox.resource;

import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.renderer.shading.TextureSampling;
import eleeter.unifystudiox.scene.entity.SceneEntity;


public class AssetBinder implements IAssetBinder
{

    @Override
    public void bind(SceneEntity entity)
    {
        if (entity.getTexture() != null)
        {
            return;
        }

        String path = entity.getAssetPath();
        if (path != null)
        {
            String texPath = AssetLink.match(path, ".png");
            if (texPath == null)
            {
                texPath = AssetLink.match(path, ".jpg");
            }
            if (texPath == null)
            {
                texPath = AssetLink.match(path, ".jpeg");
            }

            if (texPath != null)
            {
                entity.setTexture(TextureGL.loadCached(texPath, TextureSampling.PIXEL_PERFECT));
            }
        }
    }
}
