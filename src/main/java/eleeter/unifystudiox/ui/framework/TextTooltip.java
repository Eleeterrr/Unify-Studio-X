package eleeter.unifystudiox.ui.framework;

import java.util.ArrayList;
import java.util.List;

import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.graphics.Vao;
import eleeter.unifystudiox.graphics.VertexBuffer;
import eleeter.unifystudiox.graphics.buffer.GpuBufferUsage;
import eleeter.unifystudiox.graphics.layout.AttributeType;
import eleeter.unifystudiox.graphics.layout.BufferLayout;
import eleeter.unifystudiox.graphics.text.render.MeshData;
import eleeter.unifystudiox.graphics.text.render.TextMeshGenerator;
import eleeter.unifystudiox.graphics.text.render.font.Font;
import eleeter.unifystudiox.graphics.text.render.shaping.TextLayout;
import eleeter.unifystudiox.graphics.text.render.shaping.TextShaper;
import eleeter.unifystudiox.renderer.FontManager;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.theme.UIDropShadow;

/**
 * Concrete implementation of IUITooltip that shapes and renders standard text labels.
 */
public class TextTooltip implements IUITooltip
{
    private final String text;
    private final String fontKey;
    private final float scale;

    private float width = 0.0F;
    private float height = 0.0F;
    private boolean initialized = false;

    private final List<TooltipLine> tooltipLines = new ArrayList<>();

    private static class TooltipLine
    {
        MeshData data;
        float width;
        float height;
    }

    public TextTooltip(String text)
    {
        this(text, "inter", 0.42F);
    }

    public TextTooltip(String text, String fontKey, float scale)
    {
        this.text = text;
        this.fontKey = fontKey;
        this.scale = scale;
    }

    private void init()
    {
        if (this.initialized)
        {
            return;
        }
        this.initialized = true;

        Font font = FontManager.getFont(this.fontKey);
        if (font == null)
        {
            return;
        }

        TextShaper shaper = new TextShaper();

        // Dynamic word wrap based on maximum logical width to avoid bleeding out of bounds
        float maxAllowedWidth = 220.0F;
        List<String> wrappedStrings = new ArrayList<>();
        
        String[] rawLines = this.text.split("\n", -1);
        for (String rawLine : rawLines)
        {
            StringBuilder currentLine = new StringBuilder();
            String[] words = rawLine.split(" ", -1);
            for (int i = 0; i < words.length; i++)
            {
                String word = words[i];
                if (word.isEmpty() && i > 0 && i < words.length - 1) continue;
                
                String testLine = currentLine.length() == 0 ? word : currentLine.toString() + " " + word;
                TextLayout testLayout = shaper.shape(testLine, font, font.getNativeSize());
                float testWidth = testLayout.getWidth() * this.scale;

                if (testWidth > maxAllowedWidth && currentLine.length() > 0)
                {
                    wrappedStrings.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else
                {
                    if (currentLine.length() > 0)
                    {
                        currentLine.append(" ");
                    }
                    currentLine.append(word);
                }
            }
            if (currentLine.length() > 0)
            {
                wrappedStrings.add(currentLine.toString());
            }
        }

        if (wrappedStrings.isEmpty())
        {
            wrappedStrings.add("");
        }

        float totalWidth = 0.0F;
        float totalHeight = 0.0F;
        float lineSpacing = 4.0F;

        BufferLayout layoutSpec = BufferLayout.builder()
                .add(0, 3, AttributeType.FLOAT)
                .add(1, 2, AttributeType.FLOAT)
                .build();

        for (int i = 0; i < wrappedStrings.size(); i++)
        {
            String lineStr = wrappedStrings.get(i);
            TextLayout layout = shaper.shape(lineStr, font, font.getNativeSize());
            MeshData data = TextMeshGenerator.generate(layout, font);

            if (data.indices.length == 0)
            {
                continue;
            }

            TooltipLine line = new TooltipLine();
            line.width = layout.getWidth();
            line.height = layout.getHeight();
            line.data = data;

            this.tooltipLines.add(line);

            if (line.width > totalWidth)
            {
                totalWidth = line.width;
            }
            totalHeight += line.height;
            if (i > 0)
            {
                totalHeight += lineSpacing / this.scale;
            }
        }

        this.width = totalWidth;
        this.height = totalHeight;
    }

    private Vao arrowVao = null;
    private VertexBuffer arrowVbo = null;
    private VertexBuffer arrowEbo = null;

    private Vao shadowVao = null;
    private VertexBuffer shadowVbo = null;
    private VertexBuffer shadowEbo = null;

    private float lastP0x = 0f, lastP0y = 0f;
    private float lastP1x = 0f, lastP1y = 0f;
    private float lastP2x = 0f, lastP2y = 0f;

    private void updateArrowMeshes(float p0x, float p0y, float p1x, float p1y, float p2x, float p2y)
    {
        if (this.arrowVao != null && this.shadowVao != null &&
            this.lastP0x == p0x && this.lastP0y == p0y &&
            this.lastP1x == p1x && this.lastP1y == p1y &&
            this.lastP2x == p2x && this.lastP2y == p2y)
            {
            return;
        }

        this.destroyArrow();

        float ar = 0.08F, ag = 0.08F, ab = 0.10F, aa = 0.95F;
        float[] arrowVerts =
        {
            p0x, p0y, 0.0F, 0.0F, 0.0F, 0.0F, ar, ag, ab, aa, 0.0F, 0.0F,
            p1x, p1y, 0.0F, 0.0F, 0.0F, 0.0F, ar, ag, ab, aa, 0.0F, 0.0F,
            p2x, p2y, 0.0F, 0.0F, 0.0F, 0.0F, ar, ag, ab, aa, 0.0F, 0.0F
        };

        float[] shadowVerts =
        {
            p0x, p0y + 3.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.35F, 0.0F, 0.0F,
            p1x, p1y + 3.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.35F, 0.0F, 0.0F,
            p2x, p2y + 3.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.35F, 0.0F, 0.0F
        };

        int[] indices = { 0, 1, 2 };

        BufferLayout spec = BufferLayout.builder()
            .add(0, 3, AttributeType.FLOAT)
            .add(1, 3, AttributeType.FLOAT)
            .add(2, 4, AttributeType.FLOAT)
            .add(3, 2, AttributeType.FLOAT)
            .build();

        this.arrowVbo = new VertexBuffer(arrowVerts, GpuBufferUsage.STATIC);
        this.arrowEbo = new VertexBuffer(indices, GpuBufferUsage.STATIC);
        this.arrowVao = Vao.builder()
            .bindVertexBuffer(this.arrowVbo, spec)
            .elementBuffer(this.arrowEbo)
            .build();

        this.shadowVbo = new VertexBuffer(shadowVerts, GpuBufferUsage.STATIC);
        this.shadowEbo = new VertexBuffer(indices, GpuBufferUsage.STATIC);
        this.shadowVao = Vao.builder()
            .bindVertexBuffer(this.shadowVbo, spec)
            .elementBuffer(this.shadowEbo)
            .build();

        this.lastP0x = p0x; this.lastP0y = p0y;
        this.lastP1x = p1x; this.lastP1y = p1y;
        this.lastP2x = p2x; this.lastP2y = p2y;
    }

    private void destroyArrow()
    {
        if (this.arrowVao != null)
        {
            this.arrowVao.destroy();
            this.arrowVao = null;
        }
        if (this.arrowVbo != null)
        {
            this.arrowVbo.destroy();
            this.arrowVbo = null;
        }
        if (this.arrowEbo != null)
        {
            this.arrowEbo.destroy();
            this.arrowEbo = null;
        }
        if (this.shadowVao != null)
        {
            this.shadowVao.destroy();
            this.shadowVao = null;
        }
        if (this.shadowVbo != null)
        {
            this.shadowVbo.destroy();
            this.shadowVbo = null;
        }
        if (this.shadowEbo != null)
        {
            this.shadowEbo.destroy();
            this.shadowEbo = null;
        }
    }

    @Override
    public float getWidth()
    {
        this.init();
        return this.width * this.scale + 12.0F;
    }

    @Override
    public float getHeight()
    {
        this.init();
        return this.height * this.scale + 8.0F;
    }

    @Override
    public void render(UIRenderer renderer, float x, float y, float width, float height, float alpha,
                        float p0x, float p0y, float p1x, float p1y, float p2x, float p2y)
                        {
        this.init();
        if (this.tooltipLines.isEmpty())
        {
            return;
        }

        this.updateArrowMeshes(p0x, p0y, p1x, p1y, p2x, p2y);

        if (this.shadowVao != null)
        {
            renderer.drawGeometry(this.shadowVao, 3, null);
        }

        UIDropShadow.drawRounded(renderer, x, y, width, height, 0.0F, 3.0F, 0.35F * alpha, 4.0F);

        renderer.drawRoundedRect(x, y, width, height, 0.08F, 0.08F, 0.10F, 0.95F * alpha, 4.0F);
        
        if (this.arrowVao != null)
        {
            renderer.drawGeometry(this.arrowVao, 3, null);
        }

        renderer.drawRoundedRect(x, y, width, 1.0F, 0.2F, 0.55F, 1.0F, 0.2F * alpha, 0.0F);

        TextureGL atlas = FontManager.getAtlas(this.fontKey);
        if (atlas != null)
        {
            float tx = x + 6.0F;
            float ty = y + 4.0F;

            Font font = FontManager.getFont(this.fontKey);
            float baselineOffset = 0.0F;
            if (font != null)
            {
                baselineOffset = font.getBaseline() * font.getNativeSize() * this.scale;
            }

            float lineSpacing = 4.0F;
            float currentY = ty;

            for (TooltipLine line : this.tooltipLines)
            {
                if (line.data != null)
                {
                    renderer.drawText(line.data, atlas, tx, currentY + baselineOffset, this.scale, 
                            0.9F, 0.92F, 0.95F, 1.0F * alpha);
                }
                currentY += line.height * this.scale + lineSpacing;
            }
        }
    }

    @Override
    public void destroy()
    {
        this.tooltipLines.clear();
        this.destroyArrow();
        this.initialized = false;
    }
}
