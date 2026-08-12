package eleeter.unifystudiox.ui.model_editor;

import eleeter.unifystudiox.editor.animation.HierarchyPanelDataSource;
import eleeter.unifystudiox.editor.animation.ModelHierarchyBuilder;
import eleeter.unifystudiox.editor.animation.ModelHierarchyNode;
import eleeter.unifystudiox.scene.Scene;
import eleeter.unifystudiox.scene.entity.HierarchicalEntity;
import eleeter.unifystudiox.scene.entity.RiggedEntity;
import eleeter.unifystudiox.scene.entity.SceneEntity;
import java.util.function.Supplier;

public class SceneHierarchyPanelDataSource implements HierarchyPanelDataSource
{
    private final Supplier<Scene> sceneSupplier;
    private final Supplier<String> activeEntityIdSupplier;


    public SceneHierarchyPanelDataSource(Supplier<Scene> sceneSupplier, Supplier<String> activeEntityIdSupplier)
    {
        this.sceneSupplier = sceneSupplier;
        this.activeEntityIdSupplier = activeEntityIdSupplier;
    }


    public SceneHierarchyPanelDataSource(Supplier<Scene> sceneSupplier)
    {
        this(sceneSupplier, null);
    }

    private Scene getScene()
    {
        return this.sceneSupplier.get();
    }

    @Override
    public String getSelectedEntityId()
    {
        if (this.activeEntityIdSupplier != null)
        {
            String activeId = this.activeEntityIdSupplier.get();
            if (activeId != null)
            {
                return activeId;
            }
        }

        Scene activeScene = this.getScene();
        if (activeScene == null)
        {
            return null;
        }

        SceneEntity selected = activeScene.getSelectedEntity();
        if (selected == null)
        {
            return null;
        }

        String id = selected.getId();
        int colonIndex = id.indexOf(':');
        return colonIndex != -1 ? id.substring(0, colonIndex) : id;
    }

    @Override
    public ModelHierarchyNode getHierarchyFor(String entityId)
    {
        if (entityId == null || entityId.isEmpty())
        {
            return null;
        }

        Scene activeScene = this.getScene();
        if (activeScene == null)
        {
            return null;
        }

        SceneEntity entity = activeScene.findEntity(entityId).orElse(null);
        if (entity == null)
        {
            return null;
        }

        return ModelHierarchyBuilder.build(entity);
    }

    @Override
    public int getSelectedBoneIndex(String entityId)
    {
        if (entityId == null || entityId.isEmpty())
        {
            return -1;
        }

        Scene activeScene = this.getScene();
        if (activeScene == null)
        {
            return -1;
        }

        SceneEntity entity = activeScene.findEntity(entityId).orElse(null);
        if (!(entity instanceof RiggedEntity rigged))
        {
            return -1;
        }

        return rigged.getSelectedBoneIndex();
    }

    @Override
    public void setSelectedBoneIndex(String entityId, int boneIndex)
    {
        if (entityId == null || entityId.isEmpty())
        {
            return;
        }

        Scene activeScene = this.getScene();
        if (activeScene == null)
        {
            return;
        }

        SceneEntity entity = activeScene.findEntity(entityId).orElse(null);
        if (!(entity instanceof RiggedEntity rigged))
        {
            return;
        }

        rigged.setSelectedBoneIndex(boneIndex);

        if (boneIndex == -1)
        {
            activeScene.selectEntity(entity);
        } else if (entity instanceof HierarchicalEntity hierarchical)
        {
            SceneEntity subEntity = hierarchical.getSubEntity(boneIndex);
            if (subEntity != null)
            {
                activeScene.selectEntity(subEntity);
            }
        }
    }
}
