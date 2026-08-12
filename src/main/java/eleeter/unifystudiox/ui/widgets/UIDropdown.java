package eleeter.unifystudiox.ui.widgets;

import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.graphics.text.render.MeshData;
import eleeter.unifystudiox.graphics.text.render.TextMeshGenerator;
import eleeter.unifystudiox.graphics.text.render.font.Font;
import eleeter.unifystudiox.graphics.text.render.shaping.TextLayout;
import eleeter.unifystudiox.graphics.text.render.shaping.TextShaper;
import eleeter.unifystudiox.renderer.FontManager;
import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class UIDropdown extends UIPanel
{
    private static final float OPTION_HEIGHT = 22.0F;
    private static final float ARROW_AREA_W = 18.0F;

    private final eleeter.unifystudiox.ui.framework.render.context.UIInputContext context;
    private final List<String> optionKeys = new ArrayList<>();
    private final List<String> optionLabels = new ArrayList<>();
    private Consumer<String> onSelect;

    private String selectedKey = "";
    private String selectedLabel = "";

    private boolean isOpen = false;

    private MeshData headerMesh = null;
    private float headerMeshWidth = 0.0F;
    private float headerMeshHeight = 0.0F;
    private boolean headerDirty = true;

    private final List<MeshData> optionMeshes = new ArrayList<>();
    private final List<Float> optionMeshWidths = new ArrayList<>();
    private final List<Float> optionMeshHeights = new ArrayList<>();
    private boolean optionsDirty = true;

    private final String fontKey = "inter";

    public UIDropdown(String id, eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        super(id);
        this.context = context;
        this.setBlocksInput(true);
    }


    public void addOption(String key, String label)
    {
        this.optionKeys.add(key);
        this.optionLabels.add(label);
        this.optionsDirty = true;
    }


    public void setSelected(String key)
    {
        int idx = this.optionKeys.indexOf(key);
        if (idx >= 0)
        {
            this.selectedKey = this.optionKeys.get(idx);
            this.selectedLabel = this.optionLabels.get(idx);
            this.headerDirty = true;
        }
    }


    public void setOnSelect(Consumer<String> callback)
    {
        this.onSelect = callback;
    }

    public String getSelectedKey()
    {
        return this.selectedKey;
    }

    public boolean isOpen()
    {
        return this.isOpen;
    }


    private void rebuildHeaderMesh()
    {
        this.headerMesh = null;
        if (this.selectedLabel.isEmpty())
        {
            return;
        }

        Font font = FontManager.getFont(this.fontKey);
        if (font == null)
        {
            return;
        }

        TextShaper shaper = new TextShaper();
        TextLayout layout = shaper.shape(this.selectedLabel, font, font.getNativeSize());
        MeshData data = TextMeshGenerator.generate(layout, font);
        if (data.indices.length == 0)
        {
            return;
        }

        this.headerMesh = data;
        this.headerMeshWidth = layout.getWidth();
        this.headerMeshHeight = layout.getHeight();
    }

    private void rebuildOptionMeshes()
    {
        this.optionMeshes.clear();
        this.optionMeshWidths.clear();
        this.optionMeshHeights.clear();

        Font font = FontManager.getFont(this.fontKey);
        if (font == null)
        {
            return;
        }

        TextShaper shaper = new TextShaper();

        for (String label : this.optionLabels)
        {
            TextLayout layout = shaper.shape(label, font, font.getNativeSize());
            MeshData data = TextMeshGenerator.generate(layout, font);
            this.optionMeshes.add(data.indices.length == 0 ? null : data);
            this.optionMeshWidths.add(layout.getWidth());
            this.optionMeshHeights.add(layout.getHeight());
        }
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext ctx, double deltaTime)
    {
        if (this.headerDirty)
        {
            rebuildHeaderMesh();
            this.headerDirty = false;
        }

        if (this.optionsDirty)
        {
            rebuildOptionMeshes();
            this.optionsDirty = false;
        }

        if (ctx.isClicked(this))
        {
            this.isOpen = !this.isOpen;
        }

        if (this.isOpen && ctx.isMousePressed() && !ctx.isHoveredWithin(this))
        {
            float baseX = getComputedX();
            float baseY = getComputedY() + getComputedHeight();
            float dropW = getComputedWidth();
            float dropH = (float) this.optionKeys.size() * OPTION_HEIGHT;
            float mx = ctx.getMouseX();
            float my = ctx.getMouseY();
            boolean insidePopup = mx >= baseX && mx < baseX + dropW && my >= baseY && my < baseY + dropH;
            if (!insidePopup)
            {
                this.isOpen = false;
            }
        }

        if (this.isOpen)
        {
            float baseX = getComputedX();
            float baseY = getComputedY() + getComputedHeight();
            float dropW = getComputedWidth();

            for (int i = 0; i < this.optionKeys.size(); i++)
            {
                float optY = baseY + (float) i * OPTION_HEIGHT;
                float mx = ctx.getMouseX();
                float my = ctx.getMouseY();
                boolean inside = mx >= baseX && mx < baseX + dropW && my >= optY && my < optY + OPTION_HEIGHT;

                if (inside && ctx.isMousePressed())
                {
                    this.selectedKey = this.optionKeys.get(i);
                    this.selectedLabel = this.optionLabels.get(i);
                    this.headerDirty = true;
                    this.isOpen = false;

                    if (this.onSelect != null)
                    {
                        this.onSelect.accept(this.selectedKey);
                    }
                    break;
                }
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

        float br = 0.220F, bg = 0.220F, bb = 0.220F;
        if (this.context.isHeld(this))
        {
            br = 0.180F;
            bg = 0.380F;
            bb = 0.600F;
        } else if (this.context.isHovered(this) || this.isOpen)
        {
            br = 0.270F;
            bg = 0.270F;
            bb = 0.270F;
        }

        renderer.drawRect(x, y, w, h, 0.08F, 0.08F, 0.08F, 1.0F);
        renderer.drawRoundedRect(x + 1.0F, y + 1.0F, w - 2.0F, h - 2.0F, br, bg, bb, 1.0F, 3.0F);

        if (!this.context.isHeld(this))
        {
            renderer.drawRect(x + 1.0F, y + 1.0F, w - 2.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.06F);
        }

        float arrowX = x + w - ARROW_AREA_W;
        renderer.drawRect(arrowX, y + 1.0F, 1.0F, h - 2.0F, 1.0F, 1.0F, 1.0F, 0.08F);

        float midY = y + h * 0.5F;
        if (this.isOpen)
        {
            renderer.drawRect(arrowX + 5.0F, midY + 2.0F, 5.0F, 1.5F, 0.85F, 0.85F, 0.90F, 0.80F);
            renderer.drawRect(arrowX + 7.0F, midY - 0.5F, 1.5F, 3.0F, 0.85F, 0.85F, 0.90F, 0.80F);
        } else
        {
            renderer.drawRect(arrowX + 5.0F, midY - 2.5F, 5.0F, 1.5F, 0.85F, 0.85F, 0.90F, 0.80F);
            renderer.drawRect(arrowX + 7.0F, midY - 0.5F, 1.5F, 3.0F, 0.85F, 0.85F, 0.90F, 0.80F);
        }

        renderHeaderText(renderer, x, y, w, h);
    }

    private void renderHeaderText(UIRenderer renderer, float x, float y, float w, float h)
    {
        if (this.headerMesh == null)
        {
            return;
        }

        TextureGL atlas = FontManager.getAtlas(this.fontKey);
        Font font = FontManager.getFont(this.fontKey);
        if (atlas == null || font == null)
        {
            return;
        }

        float availW = w - ARROW_AREA_W - 10.0F;
        float availH = h - 4.0F;
        float scale = 1.0F;

        if (this.headerMeshWidth > 0.0F && this.headerMeshHeight > 0.0F)
        {
            scale = Math.min(availW / this.headerMeshWidth, availH / this.headerMeshHeight);
            scale = Math.min(scale, 1.0F);
        }

        float scaledH = this.headerMeshHeight * scale;
        float tx = x + 6.0F;
        float ty = y + (h - scaledH) * 0.5F + font.getBaseline() * font.getNativeSize() * scale;

        renderer.pushClip(x + 1.0F, y + 1.0F, availW + 6.0F, h - 2.0F);
        renderer.drawText(this.headerMesh, atlas, tx, ty, scale, 0.93F, 0.93F, 0.93F, 1.0F);
        renderer.popClip();
    }


    public void renderPopup(UIRenderer renderer)
    {
        if (!this.isOpen || this.optionKeys.isEmpty())
        {
            return;
        }

        float x = getComputedX();
        float y = getComputedY() + getComputedHeight();
        float w = getComputedWidth();
        float dropH = (float) this.optionKeys.size() * OPTION_HEIGHT;

        renderer.drawRoundedRect(x, y, w, dropH + 2.0F, 0.13F, 0.14F, 0.17F, 1.0F, 4.0F);
        renderer.drawRoundedRect(x + 1.0F, y + 1.0F, w - 2.0F, dropH, 0.10F, 0.11F, 0.13F, 0.97F, 3.0F);

        TextureGL atlas = FontManager.getAtlas(this.fontKey);
        Font font = FontManager.getFont(this.fontKey);

        for (int i = 0; i < this.optionKeys.size(); i++)
        {
            float optY = y + (float) i * OPTION_HEIGHT;
            float mx = this.context.getMouseX();
            float my = this.context.getMouseY();
            boolean hovered = mx >= x && mx < x + w && my >= optY && my < optY + OPTION_HEIGHT;
            boolean selected = this.optionKeys.get(i).equals(this.selectedKey);

            if (selected)
            {
                renderer.drawRect(x + 1.0F, optY, w - 2.0F, OPTION_HEIGHT, 0.18F, 0.38F, 0.60F, 0.50F);
            } else if (hovered)
            {
                renderer.drawRect(x + 1.0F, optY, w - 2.0F, OPTION_HEIGHT, 0.26F, 0.26F, 0.28F, 1.0F);
            }

            if (i > 0)
            {
                renderer.drawRect(x + 4.0F, optY, w - 8.0F, 1.0F, 0.22F, 0.25F, 0.30F, 0.40F);
            }

            MeshData mesh = i < this.optionMeshes.size() ? this.optionMeshes.get(i) : null;
            if (mesh != null && atlas != null && font != null)
            {
                float mw = this.optionMeshWidths.get(i);
                float mh = this.optionMeshHeights.get(i);
                float availW = w - ARROW_AREA_W - 10.0F;
                float scale = 1.0F;

                if (mw > 0.0F && mh > 0.0F)
                {
                    scale = Math.min(availW / mw, (OPTION_HEIGHT - 4.0F) / mh);
                    scale = Math.min(scale, 1.0F);
                }

                float scaledH = mh * scale;
                float tx = x + 8.0F;
                float ty = optY + (OPTION_HEIGHT - scaledH) * 0.5F + font.getBaseline() * font.getNativeSize() * scale;

                renderer.pushClip(x + 1.0F, optY, w - 2.0F, OPTION_HEIGHT);
                renderer.drawText(mesh, atlas, tx, ty, scale, 0.93F, 0.93F, 0.93F, selected ? 1.0F : 0.80F);
                renderer.popClip();
            }
        }
    }
}
