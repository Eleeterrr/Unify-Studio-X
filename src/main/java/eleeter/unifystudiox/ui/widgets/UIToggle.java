package eleeter.unifystudiox.ui.widgets;

import java.util.function.Consumer;

import org.joml.Vector4f;

import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.graphics.layout.AttributeType;
import eleeter.unifystudiox.graphics.layout.BufferLayout;
import eleeter.unifystudiox.graphics.text.render.MeshData;
import eleeter.unifystudiox.graphics.text.render.TextMeshGenerator;
import eleeter.unifystudiox.graphics.text.render.font.Font;
import eleeter.unifystudiox.graphics.text.render.shaping.TextLayout;
import eleeter.unifystudiox.graphics.text.render.shaping.TextShaper;
import eleeter.unifystudiox.renderer.FontManager;
import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;

public class UIToggle extends UIPanel
{
    private final eleeter.unifystudiox.ui.framework.render.context.UIInputContext context;
    private boolean checked = false;
    private String text = "";
    private String fontKey = "inter";
    private Vector4f textColor = new Vector4f(1f, 1f, 1f, 1f);
    private Consumer<Boolean> onToggle;
    private boolean textDirty = true;
    private float textWidth = 0f;
    private float textHeight = 0f;
    private MeshData textMesh = null;

    private float animProgress = 0.0f;
    private final float animSpeed = 5.0f;

    public UIToggle(String id, eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        super(id);
        this.context = context;
        setBlocksInput(true);
    }
    public void setText(String text)
    {
        if (text == null) text = "";
        if (!this.text.equals(text))
        {
            this.text = text;
            this.textDirty = true;
        }
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

    public void setTextColor(float r, float g, float b, float a)
    {
        this.textColor.set(r, g, b, a);
    }

    public void setDefaultState(boolean checked)
    {
        this.checked = checked;
        this.animProgress = checked ? 1.0f : 0.0f;
    }

    public boolean isChecked()
    {
        return this.checked;
    }

    public void setOnToggle(Consumer<Boolean> onToggle)
    {
        this.onToggle = onToggle;
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        if (this.textDirty)
        {
            rebuildTextMesh();
            this.textDirty = false;
        }

        if (context.isClicked(this))
        {
            this.checked = !this.checked;
            if (this.onToggle != null)
            {
                this.onToggle.accept(this.checked);
            }
        }

        float target = this.checked ? 1.0f : 0.0f;
        if (this.animProgress != target)
        {
            float dt = (float) deltaTime;
            if (this.animProgress < target)
            {
                this.animProgress += this.animSpeed * dt;
                if (this.animProgress > target) this.animProgress = target;
            }
            else
            {
                this.animProgress -= this.animSpeed * dt;
                if (this.animProgress < target) this.animProgress = target;
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

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        float x = getComputedX();
        float y = getComputedY();
        float w = getComputedWidth();
        float h = getComputedHeight();

        float offR = 0.2f, offG = 0.2f, offB = 0.2f;
        float onR = 0.2f,  onG = 0.8f,  onB = 0.3f;
        
        float bgR = offR + (onR - offR) * this.animProgress;
        float bgG = offG + (onG - offG) * this.animProgress;
        float bgB = offB + (onB - offB) * this.animProgress;

        if (this.context.isHovered(this))
        {
            bgR = Math.min(1.0f, bgR + 0.1f);
            bgG = Math.min(1.0f, bgG + 0.1f);
            bgB = Math.min(1.0f, bgB + 0.1f);
        }

        float pillWidth = h * 2.0f;
        float radius = h * 0.5f;
        renderer.drawRoundedRect(x, y, pillWidth, h, bgR, bgG, bgB, 1.0f, radius);

        float padding = 2.0f;
        float knobSize = h - (padding * 2.0f);
        
        float minX = x + padding;
        float maxX = x + pillWidth - knobSize - padding;
        float knobX = minX + (maxX - minX) * this.animProgress;
        float knobY = y + padding;

        renderer.drawRoundedRect(knobX, knobY, knobSize, knobSize, 1.0f, 1.0f, 1.0f, 1.0f, knobSize * 0.5f);

        if (this.textMesh != null)
        {
            TextureGL atlas = FontManager.getAtlas(this.fontKey);
            if (atlas != null)
            {
                float spacing = 8.0f;
                float tx = x + pillWidth + spacing;
                
                float availW = Math.max(0, w - pillWidth - spacing - 4.0f); // 4px padding
                float availH = h;

                float scale = 1.0f;
                if (this.textWidth > 0)
                {
                    scale = Math.min(availW / this.textWidth, availH / this.textHeight);
                    scale = Math.min(scale, 1.0f);
                }

                float ty = y + (h - this.textHeight * scale) * 0.5f;

                Font font = FontManager.getFont(this.fontKey);
                if (font != null)
                {
                    ty += font.getBaseline() * font.getNativeSize() * scale;
                }

                renderer.pushClip(x, y, w, h);
                renderer.drawText(this.textMesh, atlas, tx, ty, scale,
                        this.textColor.x, this.textColor.y, this.textColor.z, this.textColor.w);
                renderer.popClip();
            }
        }
    }
}
