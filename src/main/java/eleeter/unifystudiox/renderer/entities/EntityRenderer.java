package eleeter.unifystudiox.renderer.entities;

import eleeter.unifystudiox.graphics.api.IShaderProgram;
import eleeter.unifystudiox.renderer.core.RenderContext;
import eleeter.unifystudiox.scene.entity.SceneEntity;

public interface EntityRenderer<T extends SceneEntity>
{
    Class<T> getSupportedType();

    default void render(T entity, RenderContext context) {}
    
    default void submitCommands(T entity, RenderContext context) {}
    
    default void setupUniforms(IShaderProgram shader, T entity, int customId, RenderContext context) {}

    void cleanup();
}
