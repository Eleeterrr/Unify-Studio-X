package eleeter.unifystudiox.graphics.api;

public interface IVertexBuffer
{
    void bind();

    void unbind();

    void destroy();

    int getHandle();
}
