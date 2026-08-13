package eleeter.unifystudiox.ui.framework.render;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import eleeter.unifystudiox.graphics.text.render.MeshData;
import eleeter.unifystudiox.graphics.text.render.TextMeshGenerator;
import eleeter.unifystudiox.graphics.text.render.font.Font;
import eleeter.unifystudiox.graphics.text.render.shaping.TextLayout;
import eleeter.unifystudiox.graphics.text.render.shaping.TextShaper;
import eleeter.unifystudiox.renderer.FontManager;

public class UITextRenderer
{
    private UITextRenderer() {}

    private static class CacheKey
    {
        final String text;
        final String fontKey;
        final float size;

        CacheKey(String text, String fontKey, float size)
        {
            this.text = text;
            this.fontKey = fontKey;
            this.size = size;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }

            if (!(obj instanceof CacheKey))
            {
                return false;
            }
            CacheKey k = (CacheKey) obj;
            return Float.compare(k.size, this.size) == 0 && this.text.equals(k.text) && this.fontKey.equals(k.fontKey);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(this.text, this.fontKey, this.size);
        }
    }

    private static class Entry
    {
        final CacheKey key;
        final MeshData mesh;
        final float width;
        final float height;
        final AtomicInteger refCount = new AtomicInteger(0);

        Entry(CacheKey key, MeshData mesh, float width, float height)
        {
            this.key = key;
            this.mesh = mesh;
            this.width = width;
            this.height = height;
        }
    }

    private static final Map<CacheKey, Entry> CACHE = new ConcurrentHashMap<>();


    public static TextHandle acquire(String text, String fontKey)
    {
        Font font = FontManager.getFont(fontKey);

        if (font == null || text == null || text.isEmpty())
        {
            return TextHandle.EMPTY;
        }

        return acquire(text, fontKey, font.getNativeSize());
    }

    public static TextHandle acquire(String text, String fontKey, float size)
    {
        if (text == null || text.isEmpty())
        {
            return TextHandle.EMPTY;
        }

        CacheKey key = new CacheKey(text, fontKey, size);

        Entry entry = CACHE.computeIfAbsent(key, k ->
        {
            Font font = FontManager.getFont(k.fontKey);
            if (font == null)
            {
                return null;
            }

            TextShaper shaper = new TextShaper();
            TextLayout layout = shaper.shape(k.text, font, k.size);
            MeshData data = TextMeshGenerator.generate(layout, font);

            if (data.indices.length == 0)
            {
                return null;
            }

            return new Entry(k, data, layout.getWidth(), layout.getHeight());
        });

        if (entry == null)
        {
            return TextHandle.EMPTY;
        }

        entry.refCount.incrementAndGet();
        return new TextHandle(entry);
    }

    private static void release(Entry entry)
    {
        if (entry == null)
        {
            return;
        }
        int remaining = entry.refCount.decrementAndGet();
        if (remaining <= 0)
        {
            CACHE.remove(entry.key, entry);
        }
    }

    public static void clear()
    {
        CACHE.clear();
    }


    public static class TextHandle
    {
        public static final TextHandle EMPTY = new TextHandle(null);

        private Entry entry;
        private boolean released = false;

        private TextHandle(Entry entry)
        {
            this.entry = entry;
        }

        public boolean isValid()
        {
            return this.entry != null;
        }

        public MeshData getMesh()
        {
            return this.entry != null ? this.entry.mesh : null;
        }

        public float getWidth()
        {
            return this.entry != null ? this.entry.width : 0f;
        }

        public float getHeight()
        {
            return this.entry != null ? this.entry.height : 0f;
        }

        public void release()
        {
            if (this.released || this.entry == null)
            {
                return;
            }
            this.released = true;
            UITextRenderer.release(this.entry);
        }
    }
}