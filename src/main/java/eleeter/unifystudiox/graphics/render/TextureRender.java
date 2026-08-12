package eleeter.unifystudiox.graphics.render;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.lwjgl.system.MemoryStack;

import eleeter.unifystudiox.graphics.gl.GlConstants;
import eleeter.unifystudiox.graphics.gl.GlTexture;
import eleeter.unifystudiox.graphics.stb.ImageDecoder;
import eleeter.unifystudiox.renderer.shading.Sampler;
import eleeter.unifystudiox.renderer.shading.TextureSampling;
import eleeter.unifystudiox.util.log.AniLogger;

public class TextureRender
{
    private static final int INVALID_TEXTURE_ID = -1;
    private static final int INITIAL_BUFFER_SIZE = 8 * 1024;
    private static final int BUFFER_GROWTH_FACTOR = 2;
    private static final int RGBA_CHANNELS = 4;

    private int textureId;
    private int width;
    private int height;
    private GlTexture glTexture;

    public TextureRender()
    {
        this.textureId = INVALID_TEXTURE_ID;
        this.glTexture = new GlTexture();
    }

    public GlTexture getGlTexture()
    {
        return this.glTexture;
    }

    private static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException
    {
        ByteBuffer buffer;
        InputStream source = TextureRender.class.getResourceAsStream(resource);

        if (source == null)
        {
            File file = new File(resource.startsWith("/") ? resource.substring(1) : resource);
            if (file.exists())
            {
                source = new FileInputStream(file);
            }
        }

        if (source == null)
        {
            throw new IOException("Resource not found: " + resource);
        }

        try
        {
            try (ReadableByteChannel rbc = Channels.newChannel(source))
            {
                buffer = GlTexture.ByteBuffer(bufferSize);
                while (true)
                {
                    int bytes = rbc.read(buffer);
                    if (bytes == -1) break;
                    if (buffer.remaining() == 0)
                    {
                        buffer = resizeBuffer(buffer, buffer.capacity() * BUFFER_GROWTH_FACTOR);
                    }
                }
            }
        } finally
        {
            source.close();
        }

        buffer.flip();
        return buffer;
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity)
    {
        ByteBuffer newBuffer = GlTexture.ByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    /**
     * Loads and uploads a texture from the given resource path to the GPU.
     */
    public void load(String resourcePath)
    {
        ByteBuffer imageBuffer;
        try
        {
            imageBuffer = ioResourceToByteBuffer(resourcePath, INITIAL_BUFFER_SIZE);
        } catch (IOException e)
        {
            AniLogger.warn("TextureRender", "Could not load texture resource, using fallback: " + resourcePath);
            loadFallbackTexture();
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            ImageDecoder.setFlipVerticallyOnLoad(true);

            ByteBuffer data = ImageDecoder.loadFromMemory(imageBuffer, w, h, comp, RGBA_CHANNELS);
            if (data == null)
            {
                throw new RuntimeException("Failed to decode image [" + resourcePath + "]: " + ImageDecoder.getFailureReason());
            }

            this.width = w.get();
            this.height = h.get();

            this.textureId = this.glTexture.genTextures();
            this.glTexture.bindTexture(GlConstants.GL_TEXTURE_2D, this.textureId);

            this.glTexture.texParameteri(GlConstants.GL_TEXTURE_2D, GlConstants.GL_TEXTURE_WRAP_S, GlConstants.GL_REPEAT);
            this.glTexture.texParameteri(GlConstants.GL_TEXTURE_2D, GlConstants.GL_TEXTURE_WRAP_T, GlConstants.GL_REPEAT);

            this.glTexture.texParameteri(GlConstants.GL_TEXTURE_2D, GlConstants.GL_TEXTURE_MIN_FILTER, GlConstants.GL_LINEAR_MIPMAP_LINEAR);
            this.glTexture.texParameteri(GlConstants.GL_TEXTURE_2D, GlConstants.GL_TEXTURE_MAG_FILTER, GlConstants.GL_LINEAR);

            this.glTexture.texImage2D(GlConstants.GL_TEXTURE_2D, 0, GlConstants.GL_SRGB8_ALPHA8, this.width, this.height, 0, GlConstants.GL_RGBA, GlConstants.GL_UNSIGNED_BYTE, data);

            this.glTexture.generateMipmap(GlConstants.GL_TEXTURE_2D);

            ImageDecoder.freeImage(data);
        }
    }

    private void loadFallbackTexture()
    {
        this.width = 2;
        this.height = 2;

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            ByteBuffer data = stack.malloc(16);
            data.put((byte) 255).put((byte) 0).put((byte) 255).put((byte) 255);
            data.put((byte) 0).put((byte) 0).put((byte) 0).put((byte) 255);
            data.put((byte) 0).put((byte) 0).put((byte) 0).put((byte) 255);
            data.put((byte) 255).put((byte) 0).put((byte) 255).put((byte) 255);
            data.flip();

            this.textureId = this.glTexture.genTextures();
            this.glTexture.bindTexture(GlConstants.GL_TEXTURE_2D, this.textureId);
            this.glTexture.texParameteri(GlConstants.GL_TEXTURE_2D, GlConstants.GL_TEXTURE_WRAP_S, GlConstants.GL_REPEAT);
            this.glTexture.texParameteri(GlConstants.GL_TEXTURE_2D, GlConstants.GL_TEXTURE_WRAP_T, GlConstants.GL_REPEAT);
            this.glTexture.texParameteri(GlConstants.GL_TEXTURE_2D, GlConstants.GL_TEXTURE_MIN_FILTER, GlConstants.GL_NEAREST);
            this.glTexture.texParameteri(GlConstants.GL_TEXTURE_2D, GlConstants.GL_TEXTURE_MAG_FILTER, GlConstants.GL_NEAREST);
            this.glTexture.texImage2D(GlConstants.GL_TEXTURE_2D, 0, GlConstants.GL_SRGB8_ALPHA8, this.width, this.height, 0, GlConstants.GL_RGBA, GlConstants.GL_UNSIGNED_BYTE, data);
        }
    }

    public void bind(int unit, TextureSampling samplingMode)
    {
        this.glTexture.activeTexture(GlConstants.GL_TEXTURE0 + unit);
        this.glTexture.bindTexture(GlConstants.GL_TEXTURE_2D, this.textureId);

        Sampler.bind(unit, samplingMode);
    }

    public void unbind()
    {
        this.glTexture.bindTexture(GlConstants.GL_TEXTURE_2D, 0);
    }

    public void cleanup()
    {
        if (this.textureId != INVALID_TEXTURE_ID)
        {
            this.glTexture.deleteTextures(this.textureId);
            this.textureId = INVALID_TEXTURE_ID;
        }
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
        return this.textureId;
    }
}
