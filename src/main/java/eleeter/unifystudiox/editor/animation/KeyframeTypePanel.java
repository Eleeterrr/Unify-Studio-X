package eleeter.unifystudiox.editor.animation;

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
import java.util.HashMap;
import java.util.Map;


public class KeyframeTypePanel extends UIPanel
{
    private static final float ITEM_HEIGHT = 60.0f;
    private static final float ITEM_PADDING = 10.0f;
    private static final String FONT_KEY = "inter";

    private AnimationEditorCallbacks callbacks;
    private KeyframeType selectedType = KeyframeType.POSE;

    private final Map<String, CachedTextMesh> textCache = new HashMap<>();
    private TextureGL fontAtlas = null;

    public KeyframeTypePanel(String id)
    {
        super(id);
        this.setBlocksInput(true);
    }

    public void setCallbacks(AnimationEditorCallbacks callbacks)
    {
        this.callbacks = callbacks;
    }

    public KeyframeType getSelectedType()
    {
        return this.selectedType;
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        if (!context.isMousePressed()) return;
        if (!this.containsPoint(context.getMouseX(), context.getMouseY())) return;

        KeyframeType[] types = KeyframeType.values();
        for (int i = 0; i < types.length; i++)
        {
            float itemY = this.getComputedY() + AnimationEditorTheme.HEADER_HEIGHT + i * ITEM_HEIGHT;
            if (context.getMouseY() >= itemY && context.getMouseY() < itemY + ITEM_HEIGHT)
            {
                this.selectedType = types[i];
                if (this.callbacks != null) this.callbacks.onKeyframeTypeSelected(types[i]);
                return;
            }
        }
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

        if (this.fontAtlas != null)
        {
            CachedTextMesh titleMesh = this.getOrBuildText("Track Types");
            if (titleMesh != null)
            {
                float targetH = 12.0f;
                float scale = titleMesh.height > 0 ? targetH / titleMesh.height : 1.0f;
                float tx = x + 8f;
                float ty = y + (AnimationEditorTheme.HEADER_HEIGHT - titleMesh.height * scale) * 0.5f;
                Font font = FontManager.getFont(FONT_KEY);
                if (font != null) ty += font.getBaseline() * scale;
                float[] col = AnimationEditorTheme.TEXT_SECONDARY;
                renderer.drawText(titleMesh.data, this.fontAtlas, tx, ty, scale, col[0], col[1], col[2], col[3]);
            }
        }

        renderer.pushClip(x, y, w, h);

        KeyframeType[] types = KeyframeType.values();
        for (int i = 0; i < types.length; i++)
        {
            float itemY = y + AnimationEditorTheme.HEADER_HEIGHT + i * ITEM_HEIGHT;
            this.renderTypeItem(renderer, types[i], x, itemY, w);
        }

        renderer.popClip();

        float[] border = AnimationEditorTheme.BORDER_COLOR;
        renderer.drawRect(x + w - 1f, y, 1f, h, border[0], border[1], border[2], 1.0f);
    }

    private void renderTypeItem(UIRenderer renderer, KeyframeType type,
                                float itemX, float itemY, float itemW)
    {
        boolean isSelected = type == this.selectedType;

        if (isSelected)
        {
            float[] acc = AnimationEditorTheme.ACCENT_COLOR;
            renderer.drawRect(itemX, itemY, itemW, ITEM_HEIGHT, acc[0], acc[1], acc[2], 0.20f);
            renderer.drawRect(itemX, itemY, 2f, ITEM_HEIGHT, acc[0], acc[1], acc[2], 1.0f);
        }

        float centerX = itemX + itemW * 0.25f;
        float centerY = itemY + ITEM_HEIGHT * 0.45f;
        this.renderDiamond(renderer, type, centerX, centerY);

        String labelText = type.getDisplayName();
        CachedTextMesh mesh = this.getOrBuildText(labelText);
        if (mesh != null && this.fontAtlas != null)
        {
            float scale = 1.0f;
            float targetHeight = 14.0f;
            if (mesh.height > 0) scale = targetHeight / mesh.height;

            float[] col = isSelected ? AnimationEditorTheme.TEXT_PRIMARY : AnimationEditorTheme.TEXT_SECONDARY;
            float labelX = itemX + itemW * 0.5f;
            float labelY = itemY + (ITEM_HEIGHT - mesh.height * scale) * 0.5f;

            Font font = FontManager.getFont(FONT_KEY);
            if (font != null) labelY += font.getBaseline() * scale;

            renderer.drawText(mesh.data, this.fontAtlas,
                    labelX, labelY, scale, col[0], col[1], col[2], col[3]);
        }

        float[] div = AnimationEditorTheme.DIVIDER_COLOR;
        renderer.drawRect(itemX, itemY + ITEM_HEIGHT - 1f, itemW, 1f, div[0], div[1], div[2], 1.0f);
    }

    private void renderDiamond(UIRenderer renderer, KeyframeType type, float cx, float cy)
    {
        float halfSize = type == KeyframeType.POSE ? AnimationEditorTheme.KEYFRAME_DIAMOND_SIZE * 1.4f : AnimationEditorTheme.KEYFRAME_DIAMOND_SIZE;

        float[] color = type == KeyframeType.POSE ? AnimationEditorTheme.POSE_KEYFRAME_COLOR : AnimationEditorTheme.KEYFRAME_COLOR;

        renderer.drawRect(cx - halfSize, cy - 1f, halfSize * 2f, 2f,
                color[0], color[1], color[2], color[3]);
        renderer.drawRect(cx - 1f, cy - halfSize, 2f, halfSize * 2f,
                color[0], color[1], color[2], color[3]);
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

        BufferLayout spec = BufferLayout.builder().add(0, 3, AttributeType.FLOAT).add(1, 2, AttributeType.FLOAT).build();

        VertexBuffer vbo = new VertexBuffer(data.vertices, GpuBufferUsage.STATIC);
        VertexBuffer ebo = new VertexBuffer(data.indices, GpuBufferUsage.STATIC);
        Vao vao = Vao.builder().bindVertexBuffer(vbo, spec).elementBuffer(ebo).build();

        CachedTextMesh mesh = new CachedTextMesh(data, layout.getWidth(), layout.getHeight());
        this.textCache.put(text, mesh);
        return mesh;
    }

    public void cleanup()
    {
        for (CachedTextMesh mesh : this.textCache.values()) mesh.destroy();
        this.textCache.clear();
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
