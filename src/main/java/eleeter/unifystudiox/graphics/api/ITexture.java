package eleeter.unifystudiox.graphics.api;

public interface ITexture
{
    void bind(int slot);

    void unbind();

    void destroy();

    int getHandle();

    int getWidth();

    int getHeight();
}
