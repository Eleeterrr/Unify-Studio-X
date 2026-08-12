package eleeter.unifystudiox.scene.entity;

import org.joml.Matrix4f;

import eleeter.unifystudiox.ecs.EntityWorld;

public interface SceneEntity extends Texturable
{

    /**
     * Returns the source file path of the asset this entity represents.
     * Used by the resource system to find related textures, sounds, etc.
     */
    String getAssetPath();

    /**
     * identifier for selection, and timeline referencing.
     */
    String getId();

    /**
     * World-space transform for this entity.
     * The Renderer calls this to build the model matrix uniform.
     */
    Matrix4f getModelMatrix();

    /**
     * Per-tick update hook. Receives delta time so entities can be animated.
     */
    void update(double deltaTime);

    /**
     * True if this entity should be included in the current render pass.
     */
    boolean isVisible();

    void cleanup();

    default void initEcs(EntityWorld world)
    {
    }
}
