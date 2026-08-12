package eleeter.unifystudiox.scene.entity;

import eleeter.unifystudiox.graphics.TextureGL;

/**
 * Interface for entities that support having a 2D texture applied to them
 */
public interface Texturable
{
    void setTexture(TextureGL texture);
    TextureGL getTexture();
}
