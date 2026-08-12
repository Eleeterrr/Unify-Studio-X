package eleeter.unifystudiox.graphics;

import static org.lwjgl.opengl.GL45C.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL45C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL45C.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL45C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL45C.GL_DEPTH_STENCIL_ATTACHMENT;
import static org.lwjgl.opengl.GL45C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL45C.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL45C.GL_LINEAR;
import static org.lwjgl.opengl.GL45C.GL_NEAREST;
import static org.lwjgl.opengl.GL45C.GL_READ_FRAMEBUFFER;
import static org.lwjgl.opengl.GL45C.GL_RGBA;
import static org.lwjgl.opengl.GL45C.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL45C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL45C.glBindFramebuffer;
import static org.lwjgl.opengl.GL45C.glBlitNamedFramebuffer;
import static org.lwjgl.opengl.GL45C.glCheckNamedFramebufferStatus;
import static org.lwjgl.opengl.GL45C.glCreateFramebuffers;
import static org.lwjgl.opengl.GL45C.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL45C.glNamedFramebufferDrawBuffers;
import static org.lwjgl.opengl.GL45C.glNamedFramebufferTexture;
import static org.lwjgl.opengl.GL45C.glReadPixels;
import static org.lwjgl.opengl.GL45C.glViewport;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL45C;

import eleeter.unifystudiox.graphics.api.BlitFilter;
import eleeter.unifystudiox.graphics.api.BlitMask;
import eleeter.unifystudiox.graphics.api.IFramebuffer;

public class Framebuffer implements IFramebuffer
{
    private int handle;
    private int width;
    private int height;
    private final int samples;

    private final List<TextureFormatBit> colorFormats;
    private final TextureFormatBit depthFormat;

    private final List<FramebufferTexture> colorAttachments;
    private FramebufferTexture depthAttachment;

    private Framebuffer(Builder builder)
    {
        this.width = builder.width;
        this.height = builder.height;
        this.samples = builder.samples;
        this.colorFormats = new ArrayList<>(builder.colorFormats);
        this.depthFormat = builder.depthFormat;
        this.colorAttachments = new ArrayList<>();

        this.init();
    }

    private void init()
    {
        this.handle = glCreateFramebuffers();

        int[] drawBuffers = new int[colorFormats.size()];

        for (int i = 0; i < colorFormats.size(); i++)
        {
            FramebufferTexture tex = new FramebufferTexture(width, height, colorFormats.get(i), samples);
            this.colorAttachments.add(tex);

            glNamedFramebufferTexture(this.handle, GL_COLOR_ATTACHMENT0 + i, tex.getHandle(), 0);
            drawBuffers[i] = GL_COLOR_ATTACHMENT0 + i;
        }

        if (drawBuffers.length > 0)
        {
            glNamedFramebufferDrawBuffers(this.handle, drawBuffers);
        }
        else
        {
            GL45C.glNamedFramebufferDrawBuffer(this.handle, GL45C.GL_NONE);
            GL45C.glNamedFramebufferReadBuffer(this.handle, GL45C.GL_NONE);
        }

        if (this.depthFormat != null)
        {
            this.depthAttachment = new FramebufferTexture(this.width, this.height, this.depthFormat, samples);
            int attachmentPoint = this.depthFormat == TextureFormatBit.DEPTH24_STENCIL8 ? GL_DEPTH_STENCIL_ATTACHMENT
                    : GL_DEPTH_ATTACHMENT;

            glNamedFramebufferTexture(this.handle, attachmentPoint, depthAttachment.getHandle(), 0);
        }

        int status = glCheckNamedFramebufferStatus(this.handle, GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE)
        {
            throw new RuntimeException("Framebuffer is incomplete! Status: " + status);
        }
    }

    public void bind()
    {
        glBindFramebuffer(GL_FRAMEBUFFER, this.handle);
        glViewport(0, 0, this.width, this.height);
    }

    public void unbind()
    {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void blitTo(Framebuffer target, BlitMask mask, BlitFilter filter)
    {
        int dstW = (target == null) ? width : target.width;
        int dstH = (target == null) ? height : target.height;
        blitTo(target, 0, 0, dstW, dstH, mask, filter);
    }

    @Override
    public void blitTo(IFramebuffer target, BlitMask mask, BlitFilter filter)
    {
        blitTo((Framebuffer) target, mask, filter);
    }

    public void blitTo(Framebuffer target, int dstX1, int dstY1, int dstX2, int dstY2, BlitMask mask, BlitFilter filter)
    {
        int dstHandle = (target == null) ? 0 : target.handle;
        int glMask = 0;
        if (mask != null)
        {
            switch (mask)
            {
                case COLOR_BUFFER -> glMask = GL_COLOR_BUFFER_BIT;
                case DEPTH_BUFFER -> glMask = GL_DEPTH_BUFFER_BIT;
                case STENCIL_BUFFER -> glMask = GL_STENCIL_BUFFER_BIT;
                case COLOR_AND_DEPTH -> glMask = GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT;
            }
        }
        int glFilter = (filter == BlitFilter.NEAREST) ? GL_NEAREST : GL_LINEAR;
        glBlitNamedFramebuffer(this.handle, dstHandle, 0, 0, this.width, this.height, dstX1, dstY1, dstX2, dstY2, glMask,
                glFilter);
    }

    @Override
    public void blitTo(IFramebuffer target, int dstX1, int dstY1, int dstX2, int dstY2, BlitMask mask, BlitFilter filter)
    {
        blitTo((Framebuffer) target, dstX1, dstY1, dstX2, dstY2, mask, filter);
    }

    public void readPixels(int x, int y, int w, int h, ByteBuffer buffer)
    {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, this.handle);
        glReadPixels(x, y, w, h, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
    }

    public void resize(int newWidth, int newHeight)
    {
        if (newWidth < 1 || newHeight < 1)
            return;
        if (newWidth == this.width && newHeight == this.height)
            return;

        this.destroy();

        this.width = newWidth;
        this.height = newHeight;
        this.colorAttachments.clear();

        this.init();
    }

    public void destroy()
    {
        this.colorAttachments.forEach(FramebufferTexture::destroy);
        if (this.depthAttachment != null)
            this.depthAttachment.destroy();
        glDeleteFramebuffers(this.handle);
    }

    @Override
    public FramebufferTexture getColorTexture(int index)
    {
        return this.colorAttachments.get(index);
    }

    @Override
    public FramebufferTexture getDepthTexture()

    {
        return this.depthAttachment;
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return this.height;
    }

    public int getHandle()
    {
        return this.handle;
    }

    public int getSamples()
    {
        return this.samples;
    }

    /**
     * A new builder for the specified dimensions.
     */
    public static Builder builder(int width, int height)
    {
        return new Builder(width, height);
    }

    public static final class Builder
    {
        private int width, height;
        private int samples = 1;
        private final List<TextureFormatBit> colorFormats = new ArrayList<>();
        private TextureFormatBit depthFormat;

        private Builder(int width, int height)
        {
            this.width = width;
            this.height = height;
        }

        public Builder addColorAttachment(TextureFormatBit format)
        {
            this.colorFormats.add(format);
            return this;
        }

        public Builder addDepthAttachment(TextureFormatBit format)
        {
            this.depthFormat = format;
            return this;
        }

        public Builder withSamples(int count)
        {
            this.samples = count;
            return this;
        }

        public Framebuffer build()
        {
            return new Framebuffer(this);
        }
    }
}
