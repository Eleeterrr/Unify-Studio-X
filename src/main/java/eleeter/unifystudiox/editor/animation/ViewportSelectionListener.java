package eleeter.unifystudiox.editor.animation;


public interface ViewportSelectionListener
{
    /**
     * Called when the user clicks a new entity/bone in the viewport
     */
    void onViewportBoneSelected(String fullId);

    /**
     * Called every frame while the gizmo is being dragged
     */
    void onGizmoTransformChanged(String fullId, float px, float py, float pz, float rx, float ry, float rz, float sx, float sy, float sz);
}
