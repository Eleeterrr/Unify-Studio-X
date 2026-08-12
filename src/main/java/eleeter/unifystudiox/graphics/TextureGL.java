package eleeter.unifystudiox.graphics;

import java.util.HashMap;
import java.util.Map;

import eleeter.unifystudiox.graphics.api.ITexture;
import eleeter.unifystudiox.graphics.render.TextureRender;
import eleeter.unifystudiox.renderer.shading.TextureSampling;

public class TextureGL implements ITexture
{
    private final String resourcePath;
    private final TextureSampling samplingMode;
    private final TextureRender textureRender = new TextureRender();
    
    private boolean isLoaded = false;

    private static final Map<String, TextureGL> CACHE = new HashMap<>();

    public static TextureGL loadCached(String resourcePath)
    {
        return CACHE.computeIfAbsent(resourcePath, TextureGL::new);
    }

    public static TextureGL loadCached(String resourcePath, TextureSampling samplingMode)
    {
        return CACHE.computeIfAbsent(resourcePath, p -> new TextureGL(p, samplingMode));
    }

    public static TextureGL getCached(String resourcePath)
    {
        return CACHE.get(resourcePath);
    }

    /** Removes a cached texture so the next {@link #loadCached} reloads from disk. */
    public static void evictCached(String resourcePath)
    {
        TextureGL removed = CACHE.remove(resourcePath);

        if (removed != null)
        {
            removed.cleanup();
        }
    }

    public TextureGL(String resourcePath)
    {
        this(resourcePath, TextureSampling.TRILINEAR);
    }

    public TextureGL(String resourcePath, TextureSampling samplingMode)
    {
        this.resourcePath = resourcePath;
        this.samplingMode = samplingMode;
    }

    private void load()
    {
        if (this.isLoaded)
        {
            return;
        }

        this.textureRender.load(this.resourcePath);
        this.isLoaded = true;
        
        System.out.println("[Texture] Loaded & Corrected: " + this.resourcePath + " (" + this.textureRender.getWidth() + "x" + this.textureRender.getHeight() + ")");
    }


    public void bind(int unit)
    {
        if (!this.isLoaded)
        {
            this.load();
        }
        
        this.textureRender.bind(unit, this.samplingMode);
    }

    public void unbind()
    {
        this.textureRender.unbind();
    }

    public void cleanup()
    {
        if (this.isLoaded)
        {
            this.textureRender.cleanup();
            this.isLoaded = false;
        }
    }

    public String getResourcePath()
    {
        return this.resourcePath;
    }

    public void reload()
    {
        if (this.isLoaded)
        {
            this.textureRender.cleanup();
            this.isLoaded = false;
        }
        this.load();
    }

    public int getWidth()
    {
        return this.textureRender.getWidth();
    }

    public int getHeight()
    {
        return this.textureRender.getHeight();
    }

    @Override
    public void destroy()
    {
        cleanup();
    }

    @Override
    public int getHandle()
    {
        return this.textureRender.getHandle();
    }
}
