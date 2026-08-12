package eleeter.unifystudiox.ui.widgets;

import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.graphics.text.render.MeshData;
import eleeter.unifystudiox.graphics.text.render.TextMeshGenerator;
import eleeter.unifystudiox.graphics.text.render.font.Font;
import eleeter.unifystudiox.graphics.text.render.shaping.TextLayout;
import eleeter.unifystudiox.graphics.text.render.shaping.TextShaper;
import eleeter.unifystudiox.renderer.FontManager;
import eleeter.unifystudiox.ui.UIKey;
import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import java.util.function.Consumer;


public class UISearchField extends UIPanel
{


    private static final float BLINK_INTERVAL = 0.53F;
    private static final float CORNER_RADIUS = 5.0F;
    private static final float PADDING_LEFT = 28.0F;
    private static final float PADDING_RIGHT = 22.0F;
    private static final float PADDING_ICON_R = 0.42F;
    private static final float PADDING_ICON_G = 0.46F;
    private static final float PADDING_ICON_B = 0.56F;

    private static final float BG_R = 0.10F;
    private static final float BG_G = 0.11F;
    private static final float BG_B = 0.14F;

    private static final float REST_R = 0.20F;
    private static final float REST_G = 0.21F;
    private static final float REST_B = 0.25F;

    private static final float FOCUS_R = 0.30F;
    private static final float FOCUS_G = 0.48F;
    private static final float FOCUS_B = 0.88F;

    private static final float TEXT_R = 0.88F;
    private static final float TEXT_G = 0.91F;
    private static final float TEXT_B = 0.96F;
    private static final float PLACEHOLDER_R = 0.38F;
    private static final float PLACEHOLDER_G = 0.41F;
    private static final float PLACEHOLDER_B = 0.50F;

    private final String fontKey = "inter";


    private eleeter.unifystudiox.ui.framework.render.context.UIInputContext context;
    private String value = "";
    private String placeholder = "Search assets...";
    private boolean isFocused = false;
    private double blinkTimer = 0.0;
    private boolean isCursorVisible = true;
    private boolean isValueDirty = true;
    private boolean isPlaceholderDirty = true;

    private float focusProgress = 0.0F;
    private float clearHoverProgress = 0.0F;

    private Consumer<String> onValueChanged;
    private Runnable onEnter;

    private MeshData valueMesh = null;
    private MeshData placeholderMesh = null;
    private float valueTextWidth = 0.0F;
    private float valueTextHeight = 0.0F;


    public UISearchField(String id)
    {
        super(id);
        this.setBlocksInput(true);
    }

    public void setValue(String value)
    {
        if (!this.value.equals(value))
        {
            this.value = value;
            this.isValueDirty = true;
            if (this.onValueChanged != null)
            {
                this.onValueChanged.accept(this.value);
            }
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

    public void setOnValueChanged(Consumer<String> onValueChanged)
    {
        this.onValueChanged = onValueChanged;
    }

    public void setOnEnter(Runnable onEnter)
    {
        this.onEnter = onEnter;
    }


    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext ctx, double deltaTime)
    {
        this.context = ctx;

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

        float targetFocus = this.isFocused ? 1.0F : 0.0F;
        this.focusProgress += (targetFocus - this.focusProgress) * (float) deltaTime * 14.0F;

        float x = getComputedX();
        float y = getComputedY();
        float w = getComputedWidth();
        float h = getComputedHeight();
        boolean overClear = false;

        if (!this.value.isEmpty())
        {
            float clearZoneX = x + w - h;
            overClear = ctx.getMouseX() >= clearZoneX && ctx.getMouseX() <= x + w
                    && ctx.getMouseY() >= y && ctx.getMouseY() <= y + h;

            float targetClearHover = overClear ? 1.0F : 0.0F;
            this.clearHoverProgress += (targetClearHover - this.clearHoverProgress) * (float) deltaTime * 16.0F;
        } else
        {
            this.clearHoverProgress = 0.0F;
        }

        if (ctx.isClicked(this))
        {
            if (overClear)
            {
                setValue("");
            }
            this.isFocused = true;
            this.blinkTimer = 0.0;
            this.isCursorVisible = true;
        } else if (ctx.isMousePressed() && !ctx.isHovered(this))
        {
            this.isFocused = false;
        }

        if (this.isFocused)
        {
            String typed = ctx.consumeTextInput();
            if (typed != null && !typed.isEmpty())
            {
                setValue(this.value + typed);
            }

            if (ctx.isKeyPressed(UIKey.BACKSPACE) && !this.value.isEmpty())
            {
                setValue(this.value.substring(0, this.value.length() - 1));
            }

            if ((ctx.isKeyPressed(UIKey.ENTER) || ctx.isKeyPressed(UIKey.KP_ENTER))
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

        float br = REST_R + (FOCUS_R - REST_R) * this.focusProgress;
        float bg = REST_G + (FOCUS_G - REST_G) * this.focusProgress;
        float bb = REST_B + (FOCUS_B - REST_B) * this.focusProgress;

        renderer.drawRoundedRect(x, y, w, h, br, bg, bb, 1.0F, CORNER_RADIUS);

        renderer.drawRoundedRect(x + 1.0F, y + 1.0F, w - 2.0F, h - 2.0F,
                BG_R, BG_G, BG_B, 1.0F, CORNER_RADIUS - 1.0F);

        renderer.pushClip(x + 1.0F, y + 1.0F, w - 2.0F, h - 2.0F);

        drawSearchIcon(renderer, x, y, h);

        boolean hasClearBtn = !this.value.isEmpty();
        float textX = x + PADDING_LEFT;
        float textEndX = x + w - (hasClearBtn ? PADDING_RIGHT : 8.0F);
        float textAvailW = textEndX - textX;

        if (this.valueMesh != null && !this.value.isEmpty())
        {
            TextureGL atlas = FontManager.getAtlas(this.fontKey);
            Font font = FontManager.getFont(this.fontKey);
            if (atlas != null && font != null)
            {
                float scale = (this.valueTextWidth > textAvailW)
                        ? (textAvailW / this.valueTextWidth) : 1.0F;
                float ty = y + (h - this.valueTextHeight * scale) * 0.5F
                        + font.getBaseline() * font.getNativeSize() * scale;

                renderer.drawText(this.valueMesh, atlas,
                        textX, ty, scale, TEXT_R, TEXT_G, TEXT_B, 1.0F);

                if (this.isFocused && this.isCursorVisible)
                {
                    float cursorX = textX + this.valueTextWidth * scale + 2.0F;
                    renderer.drawRect(cursorX, y + h * 0.22F, 1.5F, h * 0.56F,
                            FOCUS_R, FOCUS_G, FOCUS_B, 0.90F);
                }
            }
        } else if (this.placeholderMesh != null && !this.placeholder.isEmpty() && !this.isFocused)
        {
            TextureGL atlas = FontManager.getAtlas(this.fontKey);
            Font font = FontManager.getFont(this.fontKey);
            if (atlas != null && font != null)
            {
                float ty = y + (h - this.valueTextHeight * 1.0F) * 0.5F
                        + font.getBaseline() * font.getNativeSize();
                renderer.drawText(this.placeholderMesh,
                        atlas, textX, ty, 1.0F,
                        PLACEHOLDER_R, PLACEHOLDER_G, PLACEHOLDER_B, 0.65F);
            }
        } else if (this.isFocused && this.isCursorVisible && this.value.isEmpty())
        {
            renderer.drawRect(textX, y + h * 0.22F, 1.5F, h * 0.56F,
                    FOCUS_R, FOCUS_G, FOCUS_B, 0.90F);
        }

        if (hasClearBtn)
        {
            drawClearIcon(renderer, x, y, w, h);
        }

        renderer.popClip();
    }


    private void drawSearchIcon(UIRenderer renderer, float panelX, float panelY, float h)
    {
        float cx = panelX + 11.0F;
        float cy = panelY + h * 0.5F - 1.0F;
        float outerR = 5.0F;
        float innerR = 3.2F;

        float ir = PADDING_ICON_R + (FOCUS_R - PADDING_ICON_R) * this.focusProgress;
        float ig = PADDING_ICON_G + (FOCUS_G - PADDING_ICON_G) * this.focusProgress;
        float ib = PADDING_ICON_B + (FOCUS_B - PADDING_ICON_B) * this.focusProgress;

        renderer.drawRoundedRect(cx - outerR, cy - outerR, outerR * 2.0F, outerR * 2.0F, ir, ig, ib, 0.90F, outerR);
        renderer.drawRoundedRect(cx - innerR, cy - innerR, innerR * 2.0F, innerR * 2.0F, BG_R, BG_G, BG_B, 1.0F, innerR);

        renderer.drawRect(cx + 3.0F, cy + 3.0F, 5.0F, 1.5F, ir, ig, ib, 0.85F);
        renderer.drawRect(cx + 4.5F, cy + 3.0F, 1.5F, 4.5F, ir, ig, ib, 0.85F);
    }


    private void drawClearIcon(UIRenderer renderer, float panelX, float panelY, float w, float h)
    {
        float cx = panelX + w - 11.0F;
        float cy = panelY + h * 0.5F;
        float arm = 3.5F;
        float thickness = 1.5F;

        float cr = PLACEHOLDER_R + (TEXT_R - PLACEHOLDER_R) * this.clearHoverProgress;
        float cg = PLACEHOLDER_G + (TEXT_G - PLACEHOLDER_G) * this.clearHoverProgress;
        float cb = PLACEHOLDER_B + (TEXT_B - PLACEHOLDER_B) * this.clearHoverProgress;
        float ca = 0.55F + 0.45F * this.clearHoverProgress;

        renderer.drawRect(cx - arm, cy - arm, arm * 2.0F, thickness, cr, cg, cb, ca);
        renderer.drawRect(cx - arm, cy + arm - thickness, arm * 2.0F, thickness, cr, cg, cb, ca);

        renderer.drawRect(cx - thickness * 0.5F, cy - arm, thickness, arm * 2.0F, cr, cg, cb, ca);

        renderer.drawRect(cx - arm, cy - thickness * 0.5F, arm * 2.0F, thickness, cr, cg, cb, ca);
    }


    private void rebuildMesh(boolean isValue)
    {
        String text = isValue ? this.value : this.placeholder;

        if (isValue && this.valueMesh != null)
        {
            this.valueMesh = null;
        } else if (!isValue && this.placeholderMesh != null)
        {
            this.placeholderMesh = null;
        }

        if (text == null || text.isEmpty())
        {
            return;
        }

        Font font = FontManager.getFont(this.fontKey);
        if (font == null)
        {
            return;
        }

        TextShaper shaper = new TextShaper();
        TextLayout layout = shaper.shape(text, font, font.getNativeSize());
        MeshData data = TextMeshGenerator.generate(layout, font);
        if (data.indices.length == 0)
        {
            return;
        }

        MeshData mesh = data;

        if (isValue)
        {
            this.valueMesh = mesh;
            this.valueTextWidth = layout.getWidth();
            this.valueTextHeight = layout.getHeight();
        } else
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
