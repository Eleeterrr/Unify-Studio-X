package eleeter.unifystudiox.scene.entity;


public interface HierarchicalEntity extends SceneEntity
{
    SceneEntity getSubEntity(int index);
}
