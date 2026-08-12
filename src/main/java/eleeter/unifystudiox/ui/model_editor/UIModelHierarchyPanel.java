package eleeter.unifystudiox.ui.model_editor;

import eleeter.unifystudiox.editor.animation.ModelHierarchyNode;
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
import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.theme.UITheme;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class UIModelHierarchyPanel extends UIPanel
{
    private static final String FONT_KEY = "inter";
    private static final float TEXT_SCALE = 0.50F;
    private static final float HEADER_H = 36.0F;
    private static final float FOOTER_H = 16.0F;
    private static final float BORDER = 1.0F;
    private static final float SCROLLBAR_W = 6.0F;
    private static final float ROW_H = 26.0F;
    private static final float ROW_PADDING_X = 12.0F;
    private static final float INDENT_W = 14.0F;

    private static final BufferLayout TEXT_BUFFER_LAYOUT = BufferLayout
            .builder()
            .add(0, 3, AttributeType.FLOAT)
            .add(1, 2, AttributeType.FLOAT)
            .build();

    private final eleeter.unifystudiox.ui.framework.render.ScrollState scrollV;
    private final eleeter.unifystudiox.ui.framework.render.ScrollState scrollH;

    private final List<ModelHierarchyRow> flatRows;
    private final Map<String, CachedTextMesh> textCache;

    private CachedTextMesh titleMesh;
    private ModelHierarchyNode rootNode;
    private int selectedBoneIndex;
    private float totalContentHeight;
    private float maxContentWidth;
    private Consumer<Integer> onBoneSelected;


    public UIModelHierarchyPanel(eleeter.unifystudiox.ui.framework.render.ScrollState scrollVertical, eleeter.unifystudiox.ui.framework.render.ScrollState scrollHorizontal)
    {
        super("model_hierarchy_panel");

        this.scrollV = scrollVertical;
        this.scrollH = scrollHorizontal;

        this.flatRows = new ArrayList<>();
        this.textCache = new HashMap<>();
        this.selectedBoneIndex = -1;
        this.totalContentHeight = 0.0F;
        this.maxContentWidth = 0.0F;

        this.setBlocksInput(true);
        this.setVisible(true);
        this.setZIndex(21);
        this.getTransform().set(0.0F, 0.0F, 1.0F, 1.0F);
    }

    public void setHierarchyRoot(ModelHierarchyNode root)
    {
        this.rootNode = root;
        this.rebuildRows();
    }

    public void selectAndReveal(int boneIndex)
    {
        this.selectedBoneIndex = boneIndex;
        if (this.rootNode != null && boneIndex != -1)
        {
            boolean revealed = this.revealNode(this.rootNode, boneIndex);
            if (revealed)
            {
                this.rebuildRows();
            }

            for (int i = 0; i < this.flatRows.size(); i++)
            {
                if (this.flatRows.get(i).getNode().getBoneIndex() == boneIndex)
                {
                    float targetY = i * ROW_H;
                    float viewH = this.ch - HEADER_H - FOOTER_H - BORDER - SCROLLBAR_W;
                    float current = this.scrollV.getScroll();
                    if (targetY < current)
                    {
                        this.scrollV.setScroll(targetY);
                    } else if (targetY + ROW_H > current + viewH)
                    {
                        this.scrollV.setScroll(targetY - viewH + ROW_H);
                    }
                    break;
                }
            }
        }
    }

    public void setOnBoneSelected(Consumer<Integer> callback)
    {
        this.onBoneSelected = callback;
    }

    public void setDocked(float x, float y, float w, float h)
    {
        this.cx = x;
        this.cy = y;
        this.cw = w;
        this.ch = h;
    }

    @Override
    public void updateLayout(float parentX, float parentY, float parentW, float parentH)
    {
        super.updateLayout(parentX, parentY, parentW, parentH);
    }

    @Override
    public boolean containsPoint(float x, float y)
    {
        if (!this.isVisible() || !this.isEnabled()) return false;
        return x >= this.cx && x < this.cx + this.cw
                && y >= this.cy && y < this.cy + this.ch;
    }

    @Override
    public void render(UIRenderer renderer)
    {
        if (!this.isVisible()) return;

        this.renderSelf(renderer);

        float contentX = this.cx + BORDER;
        float contentY = this.cy + HEADER_H;
        float contentW = this.cw - BORDER * 2.0F;
        float contentH = this.ch - HEADER_H - BORDER;
        if (this.scrollH.hasScrollbar()) contentH -= SCROLLBAR_W;

        renderer.pushClip(contentX, contentY, contentW, contentH);
        this.renderRowText(renderer, contentX, contentY, contentW, contentH);
        renderer.popClip();

        this.renderScrollbars(renderer, contentX, contentY, contentW, contentH);
    }

    @Override
    public void updateLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        this.setVisible(true);
        super.updateLogic(context, deltaTime);
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        float contentX = this.cx + BORDER;
        float contentY = this.cy + HEADER_H;
        float contentW = this.cw - BORDER * 2.0F;
        float contentH = this.ch - HEADER_H - BORDER;
        if (this.scrollH.hasScrollbar()) contentH -= SCROLLBAR_W;

        this.scrollV.region.set(contentX, contentY, contentW - SCROLLBAR_W, contentH);
        this.scrollV.clamp();

        this.scrollH.region.set(contentX, contentY + contentH, contentW, SCROLLBAR_W);
        this.scrollH.clamp();

        boolean isShiftHeld = context.isKeyHeld(UIKey.LEFT_SHIFT) || context.isKeyHeld(UIKey.RIGHT_SHIFT);

        if (context.isMousePressed())
        {
            this.scrollV.mouseClicked(context);
            this.scrollH.mouseClicked(context);
        }

        if (!context.isMouseDown())
        {
            this.scrollV.mouseReleased(context);
            this.scrollH.mouseReleased(context);
        }

        if (isShiftHeld)
        {
            this.scrollH.mouseScroll(context);
        } else
        {
            this.scrollV.mouseScroll(context);
        }

        this.scrollV.drag(context);
        this.scrollH.drag(context);
        this.scrollV.clamp();
        this.scrollH.clamp();

        this.totalContentHeight = this.flatRows.size() * ROW_H + 8.0F;
        this.scrollV.scrollSize = this.totalContentHeight;
        this.scrollH.scrollSize = this.maxContentWidth;

        if (context.isMousePressed())
        {
            float rowX = this.cx + BORDER;
            float rowY = this.cy + HEADER_H - this.scrollV.getScroll();
            float hOffset = this.scrollH.getScroll();

            for (int i = 0; i < this.flatRows.size(); i++)
            {
                float ry = rowY + i * ROW_H;
                if (context.getMouseX() >= rowX
                        && context.getMouseX() < rowX + this.cw - BORDER * 2.0F
                        && context.getMouseY() >= ry
                        && context.getMouseY() < ry + ROW_H
                        && context.getMouseY() >= this.cy + HEADER_H
                        && context.getMouseY() < this.cy + this.ch - (this.scrollH.hasScrollbar() ? SCROLLBAR_W : 0.0F))
                {
                    ModelHierarchyRow row = this.flatRows.get(i);
                    ModelHierarchyNode node = row.getNode();
                    int depth = row.getDepth();

                    float clickX = context.getMouseX() - rowX + hOffset;
                    float arrowMinX = ROW_PADDING_X + depth * INDENT_W;
                    float arrowMaxX = arrowMinX + 16.0F;

                    if (clickX >= arrowMinX && clickX <= arrowMaxX && !node.getChildren().isEmpty())
                    {
                        node.setExpanded(!node.isExpanded());
                        this.rebuildRows();
                        this.markDirty();
                    } else
                    {
                        this.selectedBoneIndex = node.getBoneIndex();
                        if (this.onBoneSelected != null)
                        {
                            this.onBoneSelected.accept(node.getBoneIndex());
                        }
                    }
                    break;
                }
            }
        }
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        float x = this.cx;
        float y = this.cy;
        float w = this.cw;
        float h = this.ch;

        UITheme.Theme theme = UITheme.get();
        float[] bg = theme.surface();
        float[] accent = theme.accent();
        float[] border = theme.border();

        renderer.drawRect(x, y, w, h, bg[0], bg[1], bg[2], 0.95F);

        /* Header background */
        renderer.drawRect(x, y, w, HEADER_H, bg[0] * 1.3F, bg[1] * 1.3F, bg[2] * 1.3F, 0.98F);

        /* Header bottom divider */
        renderer.drawRect(x, y + HEADER_H - 1.0F, w, 1.0F, border[0], border[1], border[2], 0.5F);

        /* Title text drawn directly */
        this.drawTitleText(renderer, x + 16.0F, y, w, HEADER_H);

        float contentX = x + BORDER;
        float contentY = y + HEADER_H;
        float contentW = w - BORDER * 2.0F;
        float contentH = h - HEADER_H - BORDER;
        if (this.scrollH.hasScrollbar()) contentH -= SCROLLBAR_W;

        float scrollVOffset = this.scrollV.getScroll();
        float scrollHOffset = this.scrollH.getScroll();

        for (int i = 0; i < this.flatRows.size(); i++)
        {
            float rowY = contentY + i * ROW_H - scrollVOffset;
            float rowBY = rowY + 2.0F;
            float rowBH = ROW_H - 4.0F;

            if (rowY + ROW_H < contentY || rowY > contentY + contentH) continue;

            ModelHierarchyRow row = this.flatRows.get(i);
            ModelHierarchyNode node = row.getNode();
            boolean selected = (node.getBoneIndex() != -1) && (node.getBoneIndex() == this.selectedBoneIndex);

            if (selected)
            {
                renderer.drawRoundedRect(contentX + 4.0F, rowBY, contentW - 8.0F, rowBH,
                        accent[0], accent[1], accent[2], 0.22F, 4.0F);
                renderer.drawRoundedRect(contentX + 4.0F, rowBY, 3.0F, rowBH,
                        accent[0], accent[1], accent[2], 1.0F, 2.0F);
            } else if (i % 2 == 0)
            {
                renderer.drawRect(contentX + 4.0F, rowBY, contentW - 8.0F, rowBH, 1.0F, 1.0F, 1.0F, 0.02F);
            }

            /* Depth guide lines — horizontally offset by scroll */
            int depth = row.getDepth();
            for (int d = 1; d <= depth; d++)
            {
                float lineX = contentX + ROW_PADDING_X + (d - 1) * INDENT_W + 5.0F - scrollHOffset;
                if (lineX >= contentX && lineX < contentX + contentW)
                {
                    renderer.drawRect(lineX, rowY, 1.0F, ROW_H, 0.25F, 0.27F, 0.32F, 0.30F);
                }
            }
        }
    }

    @Override
    public void collectInteractable(List<UIElement> out)
    {
        if (!this.isVisible() || !this.isEnabled()) return;
        if (this.getBlocksInput()) out.add(this);
    }

    private void renderScrollbars(UIRenderer renderer, float contentX, float contentY, float contentW, float contentH)
    {
        if (this.scrollV.hasScrollbar())
        {
            eleeter.unifystudiox.ui.framework.render.Region track = this.scrollV.getScrollregion();
            eleeter.unifystudiox.ui.framework.render.Region thumb = this.scrollV.getScrollbarregion();
            renderer.drawRect(track.x, track.y, track.w, track.h, 0.08F, 0.09F, 0.11F, 0.55F);
            float alpha = this.scrollV.dragging ? 0.85F : 0.48F;
            renderer.drawRoundedRect(track.x, thumb.y, track.w, thumb.h, 0.28F, 0.50F, 0.98F, alpha, 3.0F);
        }

        if (this.scrollH.hasScrollbar())
        {
            eleeter.unifystudiox.ui.framework.render.Region track = this.scrollH.getScrollregion();
            eleeter.unifystudiox.ui.framework.render.Region thumb = this.scrollH.getScrollbarregion();
            renderer.drawRect(track.x, track.y, track.w, track.h, 0.08F, 0.09F, 0.11F, 0.55F);
            float alpha = this.scrollH.dragging ? 0.85F : 0.48F;
            renderer.drawRoundedRect(thumb.x, track.y, thumb.w, track.h, 0.28F, 0.50F, 0.98F, alpha, 3.0F);
        }
    }

    private void renderRowText(UIRenderer renderer,
                               float contentX, float contentY,
                               float contentW, float contentH)
    {
        TextureGL atlas = FontManager.getAtlas(FONT_KEY);
        Font font = FontManager.getFont(FONT_KEY);
        if (atlas == null || font == null) return;

        float scrollVOffset = this.scrollV.getScroll();
        float scrollHOffset = this.scrollH.getScroll();
        float baselineOffset = font.getBaseline() * font.getNativeSize() * TEXT_SCALE;

        for (int i = 0; i < this.flatRows.size(); i++)
        {
            float rowY = contentY + i * ROW_H - scrollVOffset;
            if (rowY + ROW_H < contentY || rowY > contentY + contentH) continue;

            ModelHierarchyRow row = this.flatRows.get(i);
            ModelHierarchyNode node = row.getNode();
            int depth = row.getDepth();

            String prefix = node.getChildren().isEmpty()
                    ? "    "
                    : (node.isExpanded() ? "▾  " : "▸  ");

            String label = prefix + node.getName();

            CachedTextMesh mesh = this.getOrCreateMesh(label);
            if (mesh == null) continue;

            float indent = depth * INDENT_W;
            float textX = contentX + ROW_PADDING_X + indent - scrollHOffset;
            float textY = rowY + (ROW_H - mesh.height * TEXT_SCALE) * 0.5F + baselineOffset;

            boolean selected = (node.getBoneIndex() != -1) && (node.getBoneIndex() == this.selectedBoneIndex);
            float r = selected ? 1.0F : 0.82F;
            float g = selected ? 1.0F : 0.85F;
            float b = selected ? 1.0F : 0.90F;

            renderer.drawText(mesh.data, atlas, textX, textY, TEXT_SCALE, r, g, b, 1.0F);
        }
    }

    private void drawTitleText(UIRenderer renderer, float textX, float panelY, float panelW, float headerH)
    {
        if (this.titleMesh == null)
        {
            this.titleMesh = this.buildMesh("Outliner");
        }

        TextureGL atlas = FontManager.getAtlas(FONT_KEY);
        Font font = FontManager.getFont(FONT_KEY);
        if (atlas == null || font == null || this.titleMesh == null) return;

        float scale = 0.52F;
        float baselineOffset = font.getBaseline() * font.getNativeSize() * scale;
        float ty = panelY + (headerH - this.titleMesh.height * scale) * 0.5F + baselineOffset;
        renderer.drawText(this.titleMesh.data, atlas, textX, ty, scale, 0.88F, 0.91F, 0.98F, 1.0F);
    }

    private CachedTextMesh getOrCreateMesh(String text)
    {
        CachedTextMesh existing = this.textCache.get(text);
        if (existing != null) return existing;

        CachedTextMesh built = this.buildMesh(text);
        if (built != null)
        {
            this.textCache.put(text, built);
        }
        return built;
    }

    private CachedTextMesh buildMesh(String text)
    {
        Font font = FontManager.getFont(FONT_KEY);
        if (font == null) return null;

        TextShaper shaper = new TextShaper();
        TextLayout layout = shaper.shape(text, font, font.getNativeSize());
        MeshData data = TextMeshGenerator.generate(layout, font);
        if (data.indices.length == 0) return null;

        return CachedTextMesh.upload(data, TEXT_BUFFER_LAYOUT, layout.getWidth(), layout.getHeight());
    }

    private void rebuildRows()
    {
        for (CachedTextMesh mesh : this.textCache.values())
        {
            mesh.destroy();
        }
        this.textCache.clear();

        this.flatRows.clear();
        this.maxContentWidth = 0.0F;

        if (this.rootNode != null)
        {
            this.flattenTree(this.rootNode, 0);
        }

        Font font = FontManager.getFont(FONT_KEY);
        if (font != null)
        {
            for (ModelHierarchyRow row : this.flatRows)
            {
                ModelHierarchyNode node = row.getNode();
                int depth = row.getDepth();
                String prefix = node.getChildren().isEmpty() ? "    " : "▸  ";
                String label = prefix + node.getName();
                CachedTextMesh mesh = this.getOrCreateMesh(label);
                if (mesh != null)
                {
                    float rowWidth = ROW_PADDING_X + depth * INDENT_W + mesh.width * TEXT_SCALE + 16.0F;
                    if (rowWidth > this.maxContentWidth)
                    {
                        this.maxContentWidth = rowWidth;
                    }
                }
            }
        }
    }

    private void flattenTree(ModelHierarchyNode node, int depth)
    {
        this.flatRows.add(new ModelHierarchyRow(node, depth));
        if (node.isExpanded())
        {
            for (ModelHierarchyNode child : node.getChildren())
            {
                this.flattenTree(child, depth + 1);
            }
        }
    }

    private boolean revealNode(ModelHierarchyNode current, int targetIndex)
    {
        if (current.getBoneIndex() == targetIndex)
        {
            return true;
        }

        for (ModelHierarchyNode child : current.getChildren())
        {
            if (this.revealNode(child, targetIndex))
            {
                current.setExpanded(true);
                return true;
            }
        }
        return false;
    }

    public void destroyTextMeshes()
    {
        for (CachedTextMesh mesh : this.textCache.values())
        {
            mesh.destroy();
        }
        this.textCache.clear();

        if (this.titleMesh != null)
        {
            this.titleMesh.destroy();
            this.titleMesh = null;
        }
    }

    private static final class CachedTextMesh
    {
        final MeshData data;
        final float width;
        final float height;

        private CachedTextMesh(MeshData data, float width, float height)
        {
            this.data = data;
            this.width = width;
            this.height = height;
        }

        static CachedTextMesh upload(MeshData data, BufferLayout layout, float width, float height)
        {
            return new CachedTextMesh(data, width, height);
        }

        void destroy()
        {
        }
    }


    public static class ModelHierarchyRow
    {
        private final ModelHierarchyNode node;
        private final int depth;

        public ModelHierarchyRow(ModelHierarchyNode node, int depth)
        {
            this.node = node;
            this.depth = depth;
        }

        public ModelHierarchyNode getNode()
        {
            return this.node;
        }

        public int getDepth()
        {
            return this.depth;
        }
    }

    public void syncRenderPosition(float x, float y, float w, float h)
    {
        this.cx = x;
        this.cy = y;
        this.cw = w;
        this.ch = h;

        float contentH = h - HEADER_H - BORDER;
        if (this.scrollH.hasScrollbar()) contentH -= SCROLLBAR_W;


        this.scrollV.region.set(x + BORDER, y + HEADER_H, w - BORDER * 2.0F - SCROLLBAR_W, contentH);
        this.scrollH.region.set(x + BORDER, y + HEADER_H + contentH, w - BORDER * 2.0F, SCROLLBAR_W);
        this.scrollV.clamp();
        this.scrollH.clamp();
    }

}
