package eleeter.unifystudiox.ui.widgets;

import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.graphics.layout.AttributeType;
import eleeter.unifystudiox.graphics.layout.BufferLayout;
import eleeter.unifystudiox.graphics.text.render.MeshData;
import eleeter.unifystudiox.graphics.text.render.TextMeshGenerator;
import eleeter.unifystudiox.graphics.text.render.font.Font;
import eleeter.unifystudiox.graphics.text.render.shaping.TextLayout;
import eleeter.unifystudiox.graphics.text.render.shaping.TextShaper;
import eleeter.unifystudiox.renderer.FontManager;
import eleeter.unifystudiox.ui.UIKey;
import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import java.util.Locale;
import java.util.function.Consumer;
import org.joml.Vector4f;

public class UINumberField extends UIPanel
{
    private eleeter.unifystudiox.ui.framework.render.context.UIInputContext context;
    private float value = 0.0f;
    private float minValue = -Float.MAX_VALUE;
    private float maxValue = Float.MAX_VALUE;
    private float step = 1.0f;

    private String prefix = "";
    private String fontKey = "inter";
    private Vector4f textColor = new Vector4f(1f, 1f, 1f, 1f);

    private boolean isEditing = false;
    private boolean wasDragging = false;
    private boolean pressedOnThis = false;
    private String textBuffer = "";
    private boolean isTextSelected = false;
    private long lastBackspaceTime = 0;

    private Consumer<Float> onValueChanged;

    private boolean textDirty = true;
    private MeshData textMesh = null;
    private float textWidth = 0f;
    private float textHeight = 0f;
    private boolean hoveredThisFrame = false;

    public UINumberField(String id)
    {
        this(id, null);
    }

    public UINumberField(String id, eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        super(id);
        this.context = context;
        setBlocksInput(true);
    }

    public void setValue(float value)
    {
        float clamped = Math.max(this.minValue, Math.min(this.maxValue, value));
        if (this.value != clamped)
        {
            this.value = clamped;
            this.textDirty = true;
        }
    }

    public float getValue()
    {
        return this.value;
    }

    public void setRange(float min, float max)
    {
        this.minValue = min;
        this.maxValue = max;
        setValue(this.value);
    }

    public void setStep(float step)
    {
        this.step = step;
    }

    public boolean isInteracting()
    {
        return this.isEditing || this.wasDragging;
    }

    public void setPrefix(String prefix)
    {
        if (!this.prefix.equals(prefix))
        {
            this.prefix = prefix;
            this.textDirty = true;
        }
    }

    public void setTextColor(float r, float g, float b, float a)
    {
        this.textColor.set(r, g, b, a);
    }

    public void setOnValueChanged(Consumer<Float> onValueChanged)
    {
        this.onValueChanged = onValueChanged;
    }

    private void startEditing(boolean selectAll)
    {
        this.isEditing = true;
        this.isTextSelected = selectAll;
        this.textBuffer = String.format(Locale.US, "%.3f", this.value);
        if (this.textBuffer.contains("."))
        {
            this.textBuffer = this.textBuffer.replaceAll("0*$", "").replaceAll("\\.$", "");
        }
        this.textDirty = true;
    }

    private void cancelEditing()
    {
        this.isEditing = false;
        this.isTextSelected = false;
        this.textDirty = true;
    }

    private void finishEditing()
    {
        this.isEditing = false;
        this.isTextSelected = false;
        try
        {
            float parsed = Float.parseFloat(this.textBuffer);
            setValue(parsed);
            if (this.onValueChanged != null)
            {
                this.onValueChanged.accept(this.value);
            }
        } catch (NumberFormatException e)
        {
            this.textDirty = true;
        }
    }

    private String getDisplayText()
    {
        if (this.isEditing)
        {
            boolean cursorVisible = !this.isTextSelected && ((System.currentTimeMillis() / 500) % 2 == 0);
            return this.textBuffer + (cursorVisible ? "|" : "");
        } else
        {
            String valStr = String.format(Locale.US, "%.2f", this.value);
            if (valStr.contains("."))
            {
                valStr = valStr.replaceAll("0*$", "").replaceAll("\\.$", "");
            }
            if (this.prefix != null && !this.prefix.isEmpty())
            {
                return this.prefix + ": " + valStr;
            }
            return valStr;
        }
    }

    private void rebuildTextMesh()
    {
        if (this.textMesh != null)
        {

            this.textMesh = null;
        }

        String display = getDisplayText();
        if (display == null || display.isEmpty())
        {
            return;
        }

        Font font = FontManager.getFont(this.fontKey);
        if (font == null) return;

        TextShaper shaper = new TextShaper();
        TextLayout layout = shaper.shape(display, font, font.getNativeSize());

        MeshData data = TextMeshGenerator.generate(layout, font);

        if (data.indices.length == 0) return;

        this.textWidth = layout.getWidth();
        this.textHeight = layout.getHeight();

        BufferLayout layoutSpec = BufferLayout.builder()
                .add(0, 3, AttributeType.FLOAT)
                .add(1, 2, AttributeType.FLOAT)
                .build();

        this.textMesh = data;
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        this.context = context;
        this.hoveredThisFrame = context.isHovered(this);

        if (this.isEditing)
        {
            if (context.isDoubleClicked(this))
            {
                this.isTextSelected = true;
            } else if (context.isClicked(this))
            {
                this.isTextSelected = false;
            }

            String input = context.consumeTextInput();
            if (input != null && !input.isEmpty())
            {
                if (this.isTextSelected)
                {
                    this.textBuffer = "";
                    this.isTextSelected = false;
                }
                for (char c : input.toCharArray())
                {
                    if (Character.isDigit(c) || c == '.' || c == '-')
                    {
                        this.textBuffer += c;
                        this.textDirty = true;
                    }
                }
            }

            boolean backspacePressed = context.isKeyPressed(UIKey.BACKSPACE);
            boolean backspaceHeld = context.isKeyHeld(UIKey.BACKSPACE);
            long now = System.currentTimeMillis();

            if (backspacePressed || backspaceHeld)
            {
                boolean triggerBackspace = false;
                if (backspacePressed)
                {
                    this.lastBackspaceTime = now + 400;
                    triggerBackspace = true;
                } else if (now >= this.lastBackspaceTime)
                {
                    this.lastBackspaceTime = now + 40;
                    triggerBackspace = true;
                }

                if (triggerBackspace)
                {
                    if (this.isTextSelected)
                    {
                        this.textBuffer = "";
                        this.isTextSelected = false;
                        this.textDirty = true;
                    } else if (!this.textBuffer.isEmpty())
                    {
                        this.textBuffer = this.textBuffer.substring(0, this.textBuffer.length() - 1);
                        this.textDirty = true;
                    }
                }
            }

            if (context.isKeyPressed(UIKey.ENTER) || context.isKeyPressed(UIKey.KP_ENTER))
            {
                finishEditing();
            } else if (context.isMousePressed() && !context.isHovered(this))
            {
                finishEditing();
            } else if (context.isKeyPressed(UIKey.ESCAPE))
            {
                cancelEditing();
            }

            this.textDirty = true;
        } else
        {
            if (context.isClicked(this))
            {
                this.pressedOnThis = true;
            }

            if (context.isDragging(this))
            {
                if (!this.wasDragging)
                {
                    context.captureCursor();
                    this.wasDragging = true;
                }

                float dx = context.getMouseDeltaX();
                if (dx != 0.0f)
                {
                    float sensitivity = 0.2f;
                    float shiftMultiplier = (context.isKeyHeld(UIKey.LEFT_SHIFT) || context.isKeyHeld(UIKey.RIGHT_SHIFT)) ? 10.0f : 1.0f;
                    float ctrlMultiplier = (context.isKeyHeld(UIKey.LEFT_CONTROL) || context.isKeyHeld(UIKey.RIGHT_CONTROL)) ? 0.1f : 1.0f;
                    float altMultiplier = (context.isKeyHeld(UIKey.LEFT_ALT) || context.isKeyHeld(UIKey.RIGHT_ALT)) ? 0.01f : 1.0f;

                    setValue(this.value + dx * this.step * sensitivity * shiftMultiplier * ctrlMultiplier * altMultiplier);
                    if (this.onValueChanged != null)
                    {
                        this.onValueChanged.accept(this.value);
                    }
                }
            } else
            {
                if (this.wasDragging)
                {
                    context.releaseCursor();
                    this.wasDragging = false;
                }

                if (context.isMouseReleased())
                {
                    if (this.pressedOnThis && context.isHovered(this) && !this.wasDragging)
                    {
                        if (!this.isEditing)
                        {
                            startEditing(false);
                        }
                    }
                    this.pressedOnThis = false;
                }
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
        float x = getComputedX();
        float y = getComputedY();
        float w = getComputedWidth();
        float h = getComputedHeight();

        float alpha = this.isEnabled() ? 1.0f : 0.4f;

        renderer.drawRect(x, y, w, h, 0.08f, 0.08f, 0.08f, alpha);

        float ix = x + 1, iy = y + 1, iw = w - 2, ih = h - 2;

        float bgR = 0.130f, bgG = 0.130f, bgB = 0.130f;
        if (this.isEditing)
        {
            bgR = 0.100f;
            bgG = 0.100f;
            bgB = 0.100f;
        } else if (this.wasDragging)
        {
            bgR = 0.190f;
            bgG = 0.190f;
            bgB = 0.190f;
        } else if (this.hoveredThisFrame)
        {
            bgR = 0.160f;
            bgG = 0.160f;
            bgB = 0.160f;
        }

        renderer.drawRoundedRect(ix, iy, iw, ih, bgR, bgG, bgB, alpha, 3.0f);

        if (!this.isEditing && this.minValue != -Float.MAX_VALUE && this.maxValue != Float.MAX_VALUE)
        {
            float range = this.maxValue - this.minValue;
            if (range > 0)
            {
                float t = Math.max(0f, Math.min(1f, (this.value - this.minValue) / range));
                if (t > 0)
                {
                    renderer.drawRect(ix, iy, iw * t, ih, 0.18f, 0.32f, 0.52f, 0.55f * alpha);
                }
            }
        }

        if (this.isEditing)
        {
            renderer.drawRect(ix, iy + ih - 1.0f, iw, 1.0f, 0.28f, 0.52f, 0.85f, 1.0f);
        }

        if (this.wasDragging)
        {
            renderer.drawRect(ix, iy, 2.0f, ih, 0.28f, 0.52f, 0.85f, 0.9f);
        }

        if (this.textMesh != null)
        {
            TextureGL atlas = FontManager.getAtlas(this.fontKey);
            if (atlas != null)
            {
                float padX = 6.0f;
                float padY = h * 0.08f;
                float availW = iw - padX * 2f;
                float availH = ih - padY * 2f;

                float scale = 1.0f;
                if (this.textWidth > 0 && this.textHeight > 0)
                {
                    scale = Math.min(availW / this.textWidth, availH / this.textHeight);
                    scale = Math.min(scale, 1.0f);
                }

                float scaledW = this.textWidth * scale;
                float scaledH = this.textHeight * scale;

                float tx = ix + padX;
                float ty = iy + (ih - scaledH) * 0.5f;

                if (this.isTextSelected && this.isEditing)
                {
                    renderer.drawRect(tx - 1f, ty - 1f, scaledW + 2f, scaledH + 2f, 0.18f, 0.35f, 0.65f, 0.55f);
                }

                Font font = FontManager.getFont(this.fontKey);
                if (font != null) ty += font.getBaseline() * font.getNativeSize() * scale;

                renderer.pushClip(ix, iy, iw, ih);
                float textAlpha = this.isEnabled() ? this.textColor.w : this.textColor.w * 0.4f;
                renderer.drawText(this.textMesh, atlas, tx, ty, scale,
                        this.textColor.x, this.textColor.y, this.textColor.z, textAlpha);
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
