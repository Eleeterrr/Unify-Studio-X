package eleeter.unifystudiox.ui.widgets;

import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.graphics.text.render.font.Font;
import eleeter.unifystudiox.i18n.I18nKey;
import eleeter.unifystudiox.renderer.FontManager;
import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.framework.render.UITextRenderer;
import org.joml.Vector4f;

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
    private UITextRenderer.TextHandle textMesh = UITextRenderer.TextHandle.EMPTY;

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
        this.textMesh.release();
        this.textMesh = UITextRenderer.acquire(this.text, this.fontKey);
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
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        if (!this.textMesh.isValid()) return;

        TextureGL atlas = FontManager.getAtlas(this.fontKey);
        if (atlas == null) return;

        float w = getComputedWidth();
        float h = getComputedHeight();

        float scale = 1.0f;
        if (this.textMesh.getWidth() > w)
        {
            scale = w / this.textMesh.getWidth();
        }

        if (this.textMesh.getHeight() * scale > h)
        {
            scale = h / this.textMesh.getHeight();
        }

        float scaledW = this.textMesh.getWidth() * scale;
        float scaledH = this.textMesh.getHeight() * scale;

        float x = getComputedX();
        float y = getComputedY();

        if (this.alignment == Align.CENTER) this.cx = x + (w - scaledW) * 0.5f;
        else if (this.alignment == Align.RIGHT) this.cx = x + w - scaledW;

        float cy = y + (h - scaledH) * 0.5f;

        Font font = FontManager.getFont(this.fontKey);
        if (font != null)
        {
            cy += font.getBaseline() * font.getNativeSize() * scale;
        }

        renderer.pushClip(x, y, w, h);
        float textAlpha = this.isEnabled() ? this.textColor.w : this.textColor.w * 0.4f;
        renderer.drawText(this.textMesh.getMesh(), atlas, this.cx, cy, scale, this.textColor.x, this.textColor.y, this.textColor.z, textAlpha);
        renderer.popClip();
    }

    public void cleanup()
    {
        this.textMesh.release();
        this.textMesh = UITextRenderer.TextHandle.EMPTY;
    }
}