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

public class UILabel extends UIPanel
{
    public enum Align
    {
        LEFT, CENTER, RIGHT
    }

    private String text = "";
    private String fontKey = "inter";
    private Vector4f textColor = new Vector4f(1f, 1f, 1f, 1f);
    private Align alignment = Align.LEFT;

    private I18nKey i18nKey = null;

    private boolean textDirty = true;
    private MeshData textMesh = null;
    private float textWidth = 0f;
    private float textHeight = 0f;

    public UILabel(String id)
    {
        super(id);
        setBlocksInput(false);
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

    public void setAlignment(Align alignment)
    {
        this.alignment = alignment;
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
        if (font == null)
            return;

        TextShaper shaper = new TextShaper();
        TextLayout layout = shaper.shape(this.text, font, font.getNativeSize());

        MeshData data = TextMeshGenerator.generate(layout, font);

        if (data.indices.length == 0)
            return;

        this.textWidth = layout.getWidth();
        this.textHeight = layout.getHeight();

        BufferLayout layoutSpec = BufferLayout
                .builder()
                .add(0, 3, AttributeType.FLOAT)
                .add(1, 2, AttributeType.FLOAT)
                .build();

        this.textMesh = data;
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        /* If bound to an i18n key, check every frame whether the language changed */
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
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        if (this.textMesh != null)
        {
            TextureGL atlas = FontManager.getAtlas(this.fontKey);
            if (atlas != null)
            {
                float w = getComputedWidth();
                float h = getComputedHeight();

                float scale = 1.0f;
                if (this.textWidth > w)           scale = w / this.textWidth;
                if (this.textHeight * scale > h)  scale = h / this.textHeight;

                float scaledW = this.textWidth  * scale;
                float scaledH = this.textHeight * scale;

                float x  = getComputedX();
                float y  = getComputedY();

                float cx = x;
                if      (this.alignment == Align.CENTER) cx = x + (w - scaledW) * 0.5f;
                else if (this.alignment == Align.RIGHT)  cx = x + w - scaledW;

                float cy = y + (h - scaledH) * 0.5f;

                Font font = FontManager.getFont(this.fontKey);
                if (font != null)
                {
                    cy += font.getBaseline() * font.getNativeSize() * scale;
                }

                renderer.pushClip(x, y, w, h);
                float textAlpha = this.isEnabled() ? this.textColor.w : this.textColor.w * 0.4f;
                renderer.drawText(this.textMesh, atlas, cx, cy, scale, this.textColor.x, this.textColor.y, this.textColor.z, textAlpha);
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
