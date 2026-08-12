package eleeter.unifystudiox.editor.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eleeter.unifystudiox.animation.data.BoneInfo;
import eleeter.unifystudiox.animation.data.Skeleton;
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
import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;

public class BonesPanel extends UIPanel
{
    private static final String FONT_KEY = "inter";


    private AnimationEditorCallbacks callbacks;
    private Skeleton skeleton;
    private List<BoneRow> visibleRows = new ArrayList<>();


    private final Map<String, Boolean> expandedState = new HashMap<>();

    private String selectedBoneId = null;
    private String hoveredBoneId = null;
    private float scrollY = 0.0f;


    private final Map<String, CachedTextMesh> textCache = new HashMap<>();

    private TextureGL fontAtlas = null;


    public BonesPanel(String id)
    {
        super(id);
        this.setBlocksInput(true);
    }

    public void setCallbacks(AnimationEditorCallbacks callbacks)
    {
        this.callbacks = callbacks;
    }

    public void setSkeleton(Skeleton skeleton)
    {
        this.skeleton = skeleton;
        this.scrollY = 0.0f;
        this.expandedState.clear();
        this.clearTextCache();
        this.rebuildVisibleRows();
    }

    public void setSelectedBone(String boneId)
    {
        this.selectedBoneId = boneId;
    }


    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        this.hoveredBoneId = null;
        this.handleScroll(context);
        this.handleClick(context);
    }

    private void handleScroll(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        if (!this.containsPoint(context.getMouseX(), context.getMouseY())) return;

        float delta = context.getScrollDelta();
        if (Math.abs(delta) < 0.001f) return;

        float maxScroll = Math.max(0f,
                this.visibleRows.size() * AnimationEditorTheme.BONE_ROW_HEIGHT - this.getComputedHeight());
        this.scrollY -= delta * AnimationEditorTheme.BONE_ROW_HEIGHT;
        this.scrollY = Math.max(0f, Math.min(this.scrollY, maxScroll));
    }

    private void handleClick(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        if (!context.isMousePressed()) return;
        if (!this.containsPoint(context.getMouseX(), context.getMouseY())) return;

        float mouseX = context.getMouseX();
        float mouseY = context.getMouseY();

        BoneRow hitRow = this.rowAtScreenY(mouseY);
        if (hitRow == null) return;

        float rowStartX = this.getComputedX();
        float arrowEndX = rowStartX + AnimationEditorTheme.BONE_ARROW_AREA_WIDTH + hitRow.depth * AnimationEditorTheme.BONE_INDENT_PX;

        if (mouseX < arrowEndX && this.hasChildren(hitRow.boneInfo.getId()))
        {
            this.toggleExpanded(hitRow.boneInfo.getId());
            return;
        }

        String boneId = hitRow.boneInfo.getId();
        this.selectedBoneId = boneId;
        if (this.callbacks != null) this.callbacks.onBoneSelected(boneId);
    }

    private BoneRow rowAtScreenY(float screenY)
    {
        for (int i = 0; i < this.visibleRows.size(); i++)
        {
            float rowTopY = this.getComputedY() + i * AnimationEditorTheme.BONE_ROW_HEIGHT - this.scrollY;
            float rowBotY = rowTopY + AnimationEditorTheme.BONE_ROW_HEIGHT;
            if (screenY >= rowTopY && screenY < rowBotY) return this.visibleRows.get(i);
        }
        return null;
    }

    private boolean hasChildren(String boneId)
    {
        if (this.skeleton == null) return false;
        return !this.skeleton.getChildren(boneId).isEmpty();
    }

    private void toggleExpanded(String boneId)
    {
        boolean current = this.expandedState.getOrDefault(boneId, true);
        this.expandedState.put(boneId, !current);
        this.rebuildVisibleRows();
    }

    private void rebuildVisibleRows()
    {
        this.visibleRows = new ArrayList<>();
        if (this.skeleton == null) return;
        List<BoneInfo> roots = this.skeleton.getRootBones();
        for (BoneInfo root : roots)
        {
            this.appendRowsRecursive(root, 0);
        }
        this.visibleRows = Collections.unmodifiableList(this.visibleRows);
    }

    private void appendRowsRecursive(BoneInfo bone, int depth)
    {
        this.visibleRows = new ArrayList<>(this.visibleRows);
        this.visibleRows.add(new BoneRow(bone, depth));

        boolean expanded = this.expandedState.getOrDefault(bone.getId(), true);
        if (expanded && this.skeleton != null)
        {
            for (BoneInfo child : this.skeleton.getChildren(bone.getId()))
            {
                this.appendRowsRecursive(child, depth + 1);
            }
        }

        this.visibleRows = Collections.unmodifiableList(this.visibleRows);
    }


    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        float x = this.getComputedX();
        float y = this.getComputedY();
        float w = this.getComputedWidth();
        float h = this.getComputedHeight();

        float[] bg = AnimationEditorTheme.PANEL_BG;
        renderer.drawRect(x, y, w, h, bg[0], bg[1], bg[2], 1.0f);

        float[] hdr = AnimationEditorTheme.TOOLBAR_BG;
        renderer.drawRect(x, y, w, AnimationEditorTheme.HEADER_HEIGHT, hdr[0], hdr[1], hdr[2], 1.0f);

        this.ensureFontAtlas();

        renderer.pushClip(x, y, w, h);

        if (this.visibleRows.isEmpty())
        {
            this.renderCenteredText(renderer, "No object selected", x, y, w, h);
        } else
        {
            this.renderBoneRows(renderer, x, y, w, h);
        }

        renderer.popClip();

        float[] border = AnimationEditorTheme.BORDER_COLOR;
        renderer.drawRect(x + w - 1f, y, 1f, h, border[0], border[1], border[2], 1.0f);
    }

    private void renderBoneRows(UIRenderer renderer, float panelX, float panelY,
                                float panelW, float panelH)
    {
        float rowH = AnimationEditorTheme.BONE_ROW_HEIGHT;
        float startY = panelY + AnimationEditorTheme.HEADER_HEIGHT;
        float clipH = panelH - AnimationEditorTheme.HEADER_HEIGHT;

        for (int i = 0; i < this.visibleRows.size(); i++)
        {
            BoneRow row = this.visibleRows.get(i);
            float rowTop = startY + i * rowH - this.scrollY;
            float rowBot = rowTop + rowH;

            if (rowBot < startY || rowTop > startY + clipH) continue;

            this.renderBoneRow(renderer, row, panelX, rowTop, panelW, rowH);
        }
    }

    private void renderBoneRow(UIRenderer renderer, BoneRow row,
                               float panelX, float rowY, float panelW, float rowH)
    {
        String boneId = row.boneInfo.getId();
        boolean isSelected = boneId.equals(this.selectedBoneId);

        if (isSelected)
        {
            float[] sel = AnimationEditorTheme.BONE_SELECTED_BG;
            renderer.drawRect(panelX, rowY, panelW, rowH, sel[0], sel[1], sel[2], sel[3]);
        }

        float indentX = panelX + row.depth * AnimationEditorTheme.BONE_INDENT_PX + AnimationEditorTheme.BONE_ARROW_AREA_WIDTH;

        if (this.hasChildren(boneId))
        {
            boolean expanded = this.expandedState.getOrDefault(boneId, true);
            String arrow = expanded ? "v" : ">";
            this.renderText(renderer, arrow, panelX + row.depth * AnimationEditorTheme.BONE_INDENT_PX + 4f,
                    rowY + 4f, 12.0f, AnimationEditorTheme.TEXT_SECONDARY);
        }

        String displayName = row.boneInfo.getDisplayName();
        float[] textColor = isSelected ? AnimationEditorTheme.TEXT_PRIMARY : AnimationEditorTheme.TEXT_SECONDARY;
        this.renderText(renderer, displayName, indentX, rowY + 4f, 14.0f, textColor);

        float[] div = AnimationEditorTheme.DIVIDER_COLOR;
        renderer.drawRect(panelX, rowY + rowH - 1f, panelW, 1f, div[0], div[1], div[2], 1.0f);
    }

    private void renderText(UIRenderer renderer, String text, float x, float y,
                            float targetHeight, float[] color)
    {
        if (this.fontAtlas == null) return;
        CachedTextMesh mesh = this.getOrBuildText(text);
        if (mesh == null) return;

        float scale = 1.0f;
        if (mesh.height > 0) scale = targetHeight / mesh.height;

        Font font = FontManager.getFont(FONT_KEY);
        float ty = y;
        if (font != null) ty += font.getBaseline() * scale;

        renderer.drawText(mesh.data, this.fontAtlas,
                x, ty, scale, color[0], color[1], color[2], color[3]);
    }

    private void renderCenteredText(UIRenderer renderer, String text,
                                    float x, float y, float w, float h)
    {
        if (this.fontAtlas == null) return;
        CachedTextMesh mesh = this.getOrBuildText(text);
        if (mesh == null) return;

        float targetHeight = 16.0f;
        float scale = 1.0f;
        if (mesh.height > 0) scale = targetHeight / mesh.height;

        float tx = x + (w - mesh.width * scale) * 0.5f;
        float ty = y + (h - mesh.height * scale) * 0.5f;

        Font font = FontManager.getFont(FONT_KEY);
        if (font != null) ty += font.getBaseline() * scale;

        float[] col = AnimationEditorTheme.TEXT_SECONDARY;
        renderer.drawText(mesh.data, this.fontAtlas,
                tx, ty, scale, col[0], col[1], col[2], col[3]);
    }


    private void ensureFontAtlas()
    {
        if (this.fontAtlas == null)
            this.fontAtlas = FontManager.getAtlas(FONT_KEY);
    }

    private CachedTextMesh getOrBuildText(String text)
    {
        if (this.textCache.containsKey(text)) return this.textCache.get(text);

        Font font = FontManager.getFont(FONT_KEY);
        if (font == null) return null;

        TextShaper shaper = new TextShaper();
        TextLayout layout = shaper.shape(text, font, font.getNativeSize());
        MeshData data = TextMeshGenerator.generate(layout, font);
        if (data.indices.length == 0) return null;

        BufferLayout spec =
                BufferLayout.builder()
                        .add(0, 3, AttributeType.FLOAT)
                        .add(1, 2, AttributeType.FLOAT)
                        .build();

        VertexBuffer vbo = new VertexBuffer(
                data.vertices, GpuBufferUsage.STATIC);
        VertexBuffer ebo = new VertexBuffer(
                data.indices, GpuBufferUsage.STATIC);
        Vao vao = Vao.builder()
                .bindVertexBuffer(vbo, spec).elementBuffer(ebo).build();

        CachedTextMesh mesh = new CachedTextMesh(data, layout.getWidth(), layout.getHeight());
        this.textCache.put(text, mesh);
        return mesh;
    }

    private void clearTextCache()
    {
        for (CachedTextMesh mesh : this.textCache.values()) mesh.destroy();
        this.textCache.clear();
    }

    public void cleanup()
    {
        this.clearTextCache();
    }


    private record BoneRow(BoneInfo boneInfo, int depth)
        {
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

}
