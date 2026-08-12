package eleeter.unifystudiox.graphics.api;

public interface IFramebuffer
{
    void bind();

    void unbind();

    /**
     * Blits this framebuffer to the target framebuffer.
     */
    void blitTo(IFramebuffer target, BlitMask mask, BlitFilter filter);

    /**
     * Blits a specific region of this framebuffer to the target framebuffer.
     */
    void blitTo(IFramebuffer target, int dstX1, int dstY1, int dstX2, int dstY2, BlitMask mask, BlitFilter filter);

    void resize(int newWidth, int newHeight);

    void destroy();

    ITexture getColorTexture(int index);

    ITexture getDepthTexture();

    int getWidth();

    int getHeight();

    int getSamples();

    int getHandle();
}
