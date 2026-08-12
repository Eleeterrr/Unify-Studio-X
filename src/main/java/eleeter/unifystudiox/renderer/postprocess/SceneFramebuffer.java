package eleeter.unifystudiox.renderer.postprocess;

import eleeter.unifystudiox.graphics.Framebuffer;
import eleeter.unifystudiox.graphics.TextureFormatBit;

public class SceneFramebuffer
{
    private Framebuffer fbo;

    public SceneFramebuffer(int width, int height)
    {
        this.fbo = Framebuffer.builder(width, height).addColorAttachment(TextureFormatBit.RGBA8).addDepthAttachment(TextureFormatBit.DEPTH24).build();
    }

    public int getColorTextureHandle()
    {
        return this.fbo.getColorTexture(0).getHandle();
    }

    public int getDepthTextureHandle()
    {
        return this.fbo.getDepthTexture().getHandle();
    }

    public int getFBOHandle()
    {
        return this.fbo.getHandle();
    }

    public void resize(int width, int height)
    {
        this.fbo.resize(width, height);
    }

    public void dispose()
    {
        this.fbo.destroy();
    }
}
