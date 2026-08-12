package eleeter.unifystudiox.resource;

import eleeter.unifystudiox.amb.AmbLoader;
import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.obj.ObjLoader;
import eleeter.unifystudiox.scene.Scene;
import eleeter.unifystudiox.scene.entity.SceneEntity;
import eleeter.unifystudiox.util.log.AniLogger;

public class SceneResourceOrchestrator implements AssetListener
{
    private final Scene scene;
    private final IAssetBinder assetBinder;

    public SceneResourceOrchestrator(Scene scene, IAssetBinder assetBinder)
    {
        this.scene = scene;
        this.assetBinder = assetBinder;
    }

    @Override
    public void onAssetAdded(String path)
    {
        handleAsset(path, false);
    }

    @Override
    public void onAssetModified(String path)
    {
        handleAsset(path, true);
    }

    @Override
    public void onAssetRemoved(String path)
    {
        handleAsset(path, true);
    }

    private void handleAsset(String path, boolean isModified)
    {
        String lowerPath = path.toLowerCase();
        
        if (lowerPath.endsWith(".png") || lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg"))
        {
            TextureGL tex = TextureGL.getCached(path);
            if (tex != null)
            {
                AniLogger.info("SceneResourceOrchestrator", "Hot-reloading texture: " + path);
                tex.reload();
            }

            if (!isModified)
            {
                for (SceneEntity entity : this.scene.getEntities())
                {
                    if (entity.getTexture() == null)
                    {
                        this.assetBinder.bind(entity);
                    }
                }
            }
        }
        /* pretty bad hardcoded for testing stuff */
        else if (lowerPath.endsWith(".obj"))
        {
            ObjLoader.loadAndAddToScene(path, this.scene);
        }
        else if (lowerPath.endsWith(".amb"))
        {
            AmbLoader.loadAndAddToScene(path, this.scene);
        }
    }
}
