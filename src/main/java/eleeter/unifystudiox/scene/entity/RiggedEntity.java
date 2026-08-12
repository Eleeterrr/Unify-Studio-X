package eleeter.unifystudiox.scene.entity;


public interface RiggedEntity extends SceneEntity
{
    /**
     * Returns the skeletal data for this entity.
     */
    SkeletalData getSkeletalData();

    /**
     * the currently hovered bone index for debugging.
     */
    default int getHoveredBoneIndex()
    {
        return -1;
    }

    /**
     * Sets the currently hovered bone index.
     */
    default void setHoveredBoneIndex(int index) {}

    /**
     * the visibly selected bone index for debugging logic.
     */
    default int getSelectedBoneIndex() 
    {
        return -1;
    }

    /**
     * Sets the explicitly selected bone index.
     */
    default void setSelectedBoneIndex(int index) {}
}
