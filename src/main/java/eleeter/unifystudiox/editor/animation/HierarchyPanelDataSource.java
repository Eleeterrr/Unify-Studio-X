package eleeter.unifystudiox.editor.animation;


public interface HierarchyPanelDataSource
{

    String getSelectedEntityId();

    ModelHierarchyNode getHierarchyFor(String entityId);

    /**
     * Gets the active selected bone index for the specified entity, or -1 if nothing is selected
     */
    int getSelectedBoneIndex(String entityId);

    void setSelectedBoneIndex(String entityId, int boneIndex);
}
