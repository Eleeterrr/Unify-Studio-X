package eleeter.unifystudiox.ui.widgets;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER;

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


public class UITextField extends UIPanel
{
    private static final float PADDING_X = 10.0F;
    private static final float BLINK_INTERVAL = 0.53F;
    private static final float CORNER_RADIUS = 4.0F;
    private static final float BORDER_DIM = 1.5F;

    private static final float BG_R = 0.09F, BG_G = 0.10F, BG_B = 0.13F;
    private static final float BORDER_R = 0.22F, BORDER_G = 0.22F, BORDER_B = 0.26F;

    private static final float FOCUS_R = 0.35F, FOCUS_G = 0.50F, FOCUS_B = 0.85F;

    private static final float PH_R = 0.38F, PH_G = 0.40F, PH_B = 0.45F;

    private final eleeter.unifystudiox.ui.framework.render.context.UIInputContext context;
    private final String fontKey = "inter";

    private String value = "";
    private String placeholder = "";
    private boolean isFocused = false;
    private double blinkTimer = 0.0;
    private boolean isCursorVisible = true;
    private boolean isValueDirty = true;
    private boolean isPlaceholderDirty = true;
    private Runnable onEnter;

    private MeshData valueMesh = null;
    private MeshData placeholderMesh = null;
    private float valueTextWidth = 0.0F;
    private float valueTextHeight = 0.0F;

    public UITextField(String id, eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        super(id);
        this.context = context;
        this.setBlocksInput(true);
    }

    public void setValue(String value)
    {
        if (!this.value.equals(value))
        {
            this.value = value;
            this.isValueDirty = true;
        }
    }

    public String getValue()
    {
        return this.value;
    }

    public void setPlaceholder(String placeholder)
    {
        if (!this.placeholder.equals(placeholder))
        {
            this.placeholder = placeholder;
            this.isPlaceholderDirty = true;
        }
    }

    public void setOnEnter(Runnable onEnter)
    {
        this.onEnter = onEnter;
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext ctx, double deltaTime)
    {
        if (this.isValueDirty)
        {
            rebuildMesh(true);
            this.isValueDirty = false;
        }

        if (this.isPlaceholderDirty)
        {
            rebuildMesh(false);
            this.isPlaceholderDirty = false;
        }

        if (ctx.isClicked(this))
        {
            this.isFocused = true;
            this.blinkTimer = 0.0;
            this.isCursorVisible = true;
        }
        else if (ctx.isMousePressed() && !ctx.isHovered(this))
        {
            this.isFocused = false;
        }

        if (this.isFocused)
        {
            String typed = ctx.consumeTextInput();
            if (!typed.isEmpty())
            {
                this.value += typed;
                this.isValueDirty = true;
            }

            if (ctx.isKeyPressed(GLFW_KEY_BACKSPACE) && !this.value.isEmpty())
            {
                this.value = this.value.substring(0, this.value.length() - 1);
                this.isValueDirty = true;
            }

            if ((ctx.isKeyPressed(GLFW_KEY_ENTER) || ctx.isKeyPressed(GLFW_KEY_KP_ENTER))
                    && this.onEnter != null)
            {
                this.onEnter.run();
            }

            this.blinkTimer += deltaTime;
            if (this.blinkTimer >= BLINK_INTERVAL)
            {
                this.blinkTimer = 0.0;
                this.isCursorVisible = !this.isCursorVisible;
            }
        }
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        float x = getComputedX();
        float y = getComputedY();
        float w = getComputedWidth();
        float h = getComputedHeight();

        float br = this.isFocused ? FOCUS_R : BORDER_R;
        float bg = this.isFocused ? FOCUS_G : BORDER_G;
        float bb = this.isFocused ? FOCUS_B : BORDER_B;
        renderer.drawRoundedRect(x, y, w, h, br, bg, bb, 1.0F, CORNER_RADIUS);

        renderer.drawRoundedRect(x + BORDER_DIM, y + BORDER_DIM, w - BORDER_DIM * 2.0F, h - BORDER_DIM * 2.0F, BG_R, BG_G, BG_B, 1.0F, CORNER_RADIUS - BORDER_DIM);

        float availW = w - PADDING_X * 2.0F;

        if (this.valueMesh != null && !this.value.isEmpty())
        {
            TextureGL atlas = FontManager.getAtlas(this.fontKey);
            Font font = FontManager.getFont(this.fontKey);
            if (atlas != null && font != null)
            {
                float scale = (this.valueTextWidth > availW) ? (availW / this.valueTextWidth) : 1.0F;
                float ty = y + (h - this.valueTextHeight * scale) * 0.5F + font.getBaseline() * scale;

                renderer.pushClip(x + BORDER_DIM, y + BORDER_DIM,
                        w - BORDER_DIM * 2.0F, h - BORDER_DIM * 2.0F);
                renderer.drawText(this.valueMesh, atlas,
                        x + PADDING_X, ty, scale, 0.92F, 0.93F, 0.95F, 1.0F);

                if (this.isFocused && this.isCursorVisible)
                {
                    float cursorX = x + PADDING_X + this.valueTextWidth * scale + 2.0F;
                    float cursorY = y + h * 0.2F;
                    float cursorH = h * 0.6F;
                    renderer.drawRect(cursorX, cursorY, 1.5F, cursorH, FOCUS_R, FOCUS_G, FOCUS_B, 0.9F);
                }

                renderer.popClip();
            }
        }
        else if (this.placeholderMesh != null && !this.placeholder.isEmpty() && !this.isFocused)
        {
            TextureGL atlas = FontManager.getAtlas(this.fontKey);
            Font font = FontManager.getFont(this.fontKey);
            if (atlas != null && font != null)
            {
                float scale = 1.0F;
                float ty = y + (h - this.valueTextHeight * scale) * 0.5F + font.getBaseline() * scale;
                renderer.pushClip(x + BORDER_DIM, y + BORDER_DIM,
                        w - BORDER_DIM * 2.0F, h - BORDER_DIM * 2.0F);
                renderer.drawText(this.placeholderMesh, atlas, x + PADDING_X, ty, scale, PH_R, PH_G, PH_B, 0.7F);
                renderer.popClip();
            }
        }
        else if (this.isFocused && this.isCursorVisible && this.value.isEmpty())
        {
            float cursorX = x + PADDING_X;
            float cursorY = y + h * 0.2F;
            float cursorH = h * 0.6F;
            renderer.drawRect(cursorX, cursorY, 1.5F, cursorH, FOCUS_R, FOCUS_G, FOCUS_B, 0.9F);
        }
    }

    private void rebuildMesh(boolean isValue)
    {
        String text = isValue ? this.value : this.placeholder;

        if (isValue)
        {
            if (this.valueMesh != null)
            {
                
                this.valueMesh = null;
            }
        }
        else
        {
            if (this.placeholderMesh != null)
            {
                
                this.placeholderMesh = null;
            }
        }

        if (text == null || text.isEmpty()) return;

        Font font = FontManager.getFont(this.fontKey);
        if (font == null) return;

        TextShaper shaper = new TextShaper();
        TextLayout layout = shaper.shape(text, font, font.getNativeSize());
        MeshData data = TextMeshGenerator.generate(layout, font);
        if (data.indices.length == 0) return;

        BufferLayout spec = BufferLayout.builder()
                .add(0, 3, AttributeType.FLOAT)
                .add(1, 2, AttributeType.FLOAT)
                .build();

        MeshData mesh = data;

        if (isValue)
        {
            this.valueMesh = mesh;
            this.valueTextWidth = layout.getWidth();
            this.valueTextHeight = layout.getHeight();
        }
        else
        {
            this.placeholderMesh = mesh;
        }
    }

    public void cleanup()
    {
        if (this.valueMesh != null)
        {
            
            this.valueMesh = null;
        }

        if (this.placeholderMesh != null)
        {
            
            this.placeholderMesh = null;
        }
    }

    
}
