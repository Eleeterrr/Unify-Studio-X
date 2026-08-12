package eleeter.unifystudiox.ui.widgets;

import org.joml.Vector4f;

import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.graphics.layout.AttributeType;
import eleeter.unifystudiox.graphics.layout.BufferLayout;
import eleeter.unifystudiox.graphics.text.render.MeshData;
import eleeter.unifystudiox.graphics.text.render.TextMeshGenerator;
import eleeter.unifystudiox.graphics.text.render.font.Font;
import eleeter.unifystudiox.graphics.text.render.shaping.TextLayout;
import eleeter.unifystudiox.graphics.text.render.shaping.TextShaper;
import eleeter.unifystudiox.i18n.I18nKey;
import eleeter.unifystudiox.renderer.FontManager;
import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;

public class UIButton extends UIPanel
{
    private final eleeter.unifystudiox.ui.framework.render.context.UIInputContext context;
    private Runnable onClick;

    private float baseR = 0.220f, baseG = 0.220f, baseB = 0.220f, baseA = 1.0f;
    private float hoverR = 0.270f, hoverG = 0.270f, hoverB = 0.270f, hoverA = 1.0f;
    private float pressR = 0.180f, pressG = 0.380f, pressB = 0.600f, pressA = 1.0f;

    private String text = "";
    private String fontKey = "inter";
    private Vector4f textColor = new Vector4f(1f, 1f, 1f, 1f);
    
    private I18nKey i18nKey = null;

    private boolean textDirty = true;
    private MeshData textMesh = null;
    private float textWidth = 0f;
    private float textHeight = 0f;

    public UIButton(String id, eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        super(id);
        this.context = context;
        setBlocksInput(true);
    }

    public void setOnClick(Runnable onClick)
    {
        this.onClick = onClick;
    }

    public void setColors(float br, float bg, float bb, float hr, float hg, float hb, float pr, float pg, float pb)
    {
        this.baseR = br; this.baseG = bg; this.baseB = bb;
        this.hoverR = hr; this.hoverG = hg; this.hoverB = hb;
        this.pressR = pr; this.pressG = pg; this.pressB = pb;
    }

    public void setText(String text)
    {
        if (!this.text.equals(text))
        {
            this.text = text;
            this.i18nKey = null;
            this.textDirty = true;
        }
    }

    public void setKey(I18nKey key)
    {
        this.i18nKey = key;
        if (key != null)
        {
            this.text = key.getValue();
        }
        this.textDirty = true;
    }

    public void setFont(String fontKey)
    {
        if (!this.fontKey.equals(fontKey))
        {
            this.fontKey = fontKey;
            this.textDirty = true;
        }
    }

    public void setTextColor(float r, float g, float b, float a)
    {
        this.textColor.set(r, g, b, a);
    }

    private void rebuildTextMesh()
    {
        if (this.textMesh != null)
        {
            
            this.textMesh = null;
        }

        if (this.text == null || this.text.isEmpty())
        {
            return;
        }

        Font font = FontManager.getFont(this.fontKey);
        if (font == null) return;

        TextShaper shaper = new TextShaper();
        TextLayout layout = shaper.shape(this.text, font, font.getNativeSize());
        
        MeshData data = TextMeshGenerator.generate(layout, font);
        
        if (data.indices.length == 0) return;

        this.textWidth = layout.getWidth();
        this.textHeight = layout.getHeight();

        BufferLayout layoutSpec = BufferLayout.builder()
                .add(0, 3, AttributeType.FLOAT) // pos
                .add(1, 2, AttributeType.FLOAT) // tex
                .build();

        this.textMesh = data;
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        if (this.i18nKey != null)
        {
            String live = this.i18nKey.getValue();
            if (!live.equals(this.text))
            {
                this.text = live;
                this.textDirty = true;
            }
        }

        if (this.textDirty)
        {
            rebuildTextMesh();
            this.textDirty = false;
        }

        if (this.context.isClicked(this) && this.onClick != null)
        {
            this.onClick.run();
        }
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        float x = getComputedX();
        float y = getComputedY();
        float w = getComputedWidth();
        float h = getComputedHeight();

        float r = this.baseR, g = this.baseG, b = this.baseB, a = this.baseA;

        if (this.context.isHeld(this))
        {
            r = this.pressR; g = this.pressG; b = this.pressB; a = this.pressA;
        }
        else if (this.context.isHovered(this))
        {
            r = this.hoverR; g = this.hoverG; b = this.hoverB; a = this.hoverA;
        }

        renderer.drawRect(x, y, w, h, 0.08f, 0.08f, 0.08f, 1.0f);

        renderer.drawRoundedRect(x + 1, y + 1, w - 2, h - 2, r, g, b, a, 3.0f);

        if (!this.context.isHeld(this))
        {
            renderer.drawRect(x + 1, y + 1, w - 2, 1.0f, 1.0f, 1.0f, 1.0f, 0.06f);
        }

        if (this.textMesh != null)
        {
            TextureGL atlas = FontManager.getAtlas(this.fontKey);
            if (atlas != null)
            {
                float padX = w * 0.08f;
                float padY = h * 0.08f;
                float availW = w - padX * 2f;
                float availH = h - padY * 2f;

                float scale = 1.0f;
                if (this.textWidth > 0 && this.textHeight > 0)
                {
                    scale = Math.min(availW / this.textWidth, availH / this.textHeight);
                    scale = Math.min(scale, 1.0f);
                }

                float scaledW = this.textWidth  * scale;
                float scaledH = this.textHeight * scale;
                float cx = x + (w - scaledW) * 0.5f;
                float cy = y + (h - scaledH) * 0.5f;

                Font font = FontManager.getFont(this.fontKey);
                if (font != null) cy += font.getBaseline() * font.getNativeSize() * scale;

                renderer.pushClip(x, y, w, h);
                renderer.drawText(this.textMesh, atlas, cx, cy, scale,
                        this.textColor.x, this.textColor.y, this.textColor.z, this.textColor.w);
                renderer.popClip();
            }
        }
    }

    public void cleanup()
    {
        if (this.textMesh != null)
        {
            
            this.textMesh = null;
        }
    }

    
}
