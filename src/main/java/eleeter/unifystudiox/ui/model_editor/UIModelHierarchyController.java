package eleeter.unifystudiox.ui.model_editor;

import eleeter.unifystudiox.editor.animation.HierarchyPanelDataSource;
import eleeter.unifystudiox.editor.animation.ModelHierarchyNode;

public class UIModelHierarchyController
{
    private final UIModelHierarchyPanel panel;
    private final HierarchyPanelDataSource dataSource;

    private String lastEntityId;
    private int lastBoneIndex;

    public UIModelHierarchyController(UIModelHierarchyPanel panel, HierarchyPanelDataSource dataSource)
    {
        this.panel = panel;
        this.dataSource = dataSource;
        this.lastEntityId = null;
        this.lastBoneIndex = -2;
    }


    public void init()
    {
        this.panel.setOnBoneSelected(boneIndex ->
        {
            String entityId = this.lastEntityId;
            if (entityId == null)
            {
                return;
            }

            this.dataSource.setSelectedBoneIndex(entityId, boneIndex);
            this.lastBoneIndex = boneIndex;
        });
    }


    public void update()
    {
        String currentEntityId = this.dataSource.getSelectedEntityId();

        if (!idsMatch(currentEntityId, this.lastEntityId))
        {
            this.lastEntityId = currentEntityId;
            if (currentEntityId != null)
            {
                ModelHierarchyNode root = this.dataSource.getHierarchyFor(currentEntityId);
                this.panel.setHierarchyRoot(root);
            }
            else
            {
                this.panel.setHierarchyRoot(null);
            }
            this.lastBoneIndex = -2;
        }

        if (currentEntityId != null)
        {
            int index = this.dataSource.getSelectedBoneIndex(currentEntityId);
            if (index != this.lastBoneIndex)
            {
                this.lastBoneIndex = index;
                this.panel.selectAndReveal(index);
            }
        }
    }

    private static boolean idsMatch(String a, String b)
    {
        if (a == null)
        {
            return b == null;
        }
        return a.equals(b);
    }
}
