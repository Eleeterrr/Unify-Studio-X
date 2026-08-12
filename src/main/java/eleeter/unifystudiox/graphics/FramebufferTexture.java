package eleeter.unifystudiox.graphics;

import static org.lwjgl.opengl.GL45C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL45C.GL_COMPARE_REF_TO_TEXTURE;
import static org.lwjgl.opengl.GL45C.GL_LEQUAL;
import static org.lwjgl.opengl.GL45C.GL_LINEAR;
import static org.lwjgl.opengl.GL45C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL45C.GL_TEXTURE_2D_MULTISAMPLE;
import static org.lwjgl.opengl.GL45C.GL_TEXTURE_COMPARE_FUNC;
import static org.lwjgl.opengl.GL45C.GL_TEXTURE_COMPARE_MODE;
import static org.lwjgl.opengl.GL45C.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL45C.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL45C.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL45C.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL45C.glBindTextureUnit;
import static org.lwjgl.opengl.GL45C.glCreateTextures;
import static org.lwjgl.opengl.GL45C.glDeleteTextures;
import static org.lwjgl.opengl.GL45C.glTextureParameteri;
import static org.lwjgl.opengl.GL45C.glTextureStorage2D;
import static org.lwjgl.opengl.GL45C.glTextureStorage2DMultisample;

import eleeter.unifystudiox.graphics.api.ITexture;

public class FramebufferTexture implements ITexture
{
    private final int handle;
    private final int width;
    private final int height;
    private final int samples;
    private final int target;
    private final TextureFormatBit format;

    public FramebufferTexture(int width, int height, TextureFormatBit format)
    {
        this(width, height, format, 1);
    }

    public FramebufferTexture(int width, int height, TextureFormatBit format, int samples)
    {
        this.width = width;
        this.height = height;
        this.format = format;
        this.samples = samples;
        this.target = (samples > 1) ? GL_TEXTURE_2D_MULTISAMPLE : GL_TEXTURE_2D;

        this.handle = glCreateTextures(this.target);

        if (samples > 1)
        {
            glTextureStorage2DMultisample(this.handle, samples, format.internalFormat, width, height, true);
        }
        else
        {
            glTextureStorage2D(this.handle, 1, format.internalFormat, width, height);

            // Standard FBO texture parameters
            glTextureParameteri(this.handle, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTextureParameteri(this.handle, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTextureParameteri(this.handle, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTextureParameteri(this.handle, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        }
        if (format == TextureFormatBit.DEPTH24 || format == TextureFormatBit.DEPTH24_STENCIL8)
        {
            glTextureParameteri(this.handle, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
            glTextureParameteri(this.handle, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
        }
    }

    /**
     * Binds the texture to a specific texture unit.
     */
    public void bind(int unit)
    {
        glBindTextureUnit(unit, this.handle);
    }

    public void unbind(int unit)
    {
        glBindTextureUnit(unit, 0);
    }

    @Override
    public void unbind()
    {
        glBindTextureUnit(0, 0);
    }

    public void destroy()
    {
        glDeleteTextures(this.handle);
    }

    public int getHandle()
    {
        return this.handle;
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return this.height;
    }

    public int getSamples()
    {
        return this.samples;
    }

    public int getTarget()
    {
        return this.target;
    }

    public TextureFormatBit getFormat()
    {
        return this.format;
    }
}
