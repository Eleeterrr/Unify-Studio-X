package eleeter.unifystudiox.renderer.postprocess;

import eleeter.unifystudiox.graphics.Framebuffer;
import eleeter.unifystudiox.graphics.TextureFormatBit;

public class PingPongFBO
{
    private Framebuffer fboA;
    private Framebuffer fboB;
    private boolean readFromA = true;

    public PingPongFBO(int width, int height)
    {
        this.fboA = Framebuffer.builder(width, height).addColorAttachment(TextureFormatBit.RGBA8).build();
        this.fboB = Framebuffer.builder(width, height).addColorAttachment(TextureFormatBit.RGBA8).build();
    }

    public void swap()
    {
        this.readFromA = !this.readFromA;
    }

    public int getReadTexture()
    {
        return this.readFromA
                ? this.fboA.getColorTexture(0).getHandle()
                : this.fboB.getColorTexture(0).getHandle();
    }

    public int getWriteFBO()
    {
        return this.readFromA ? this.fboB.getHandle() : this.fboA.getHandle();
    }

    public void resize(int width, int height)
    {
        this.fboA.resize(width, height);
        this.fboB.resize(width, height);
    }

    public void dispose()
    {
        this.fboA.destroy();
        this.fboB.destroy();
    }
}
