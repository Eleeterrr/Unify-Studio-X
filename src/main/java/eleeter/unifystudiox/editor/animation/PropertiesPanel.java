package eleeter.unifystudiox.editor.animation;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3f;

import eleeter.unifystudiox.animation.data.EasingType;
import eleeter.unifystudiox.animation.data.Transform;
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

public class PropertiesPanel extends UIPanel
{
    private enum Mode
    {
        EMPTY, KEYFRAME, POSE, LIVE_POSE
    }

    private static final float ROW_HEIGHT = 28.0f;
    private static final float SECTION_PAD = 20.0f;
    private static final float LABEL_COLUMN_WIDTH = 70.0f;
    private static final float FIELD_PAD = 4.0f;
    private static final String FONT_KEY = "inter";

    private AnimationEditorCallbacks callbacks;
    private Mode mode = Mode.EMPTY;

    private String selectedBoneId = null;
    private String selectedProperty = null;
    private float selectedTime = 0.0f;

    private Map<String, Transform> poseTransforms = new HashMap<>();

    private Object keyframeValue = null;

    private final UIEditableFloat fieldPositionX;
    private final UIEditableFloat fieldPositionY;
    private final UIEditableFloat fieldPositionZ;
    private final UIEditableFloat fieldRotationX;
    private final UIEditableFloat fieldRotationY;
    private final UIEditableFloat fieldRotationZ;
    private final UIEditableFloat fieldScaleX;
    private final UIEditableFloat fieldScaleY;
    private final UIEditableFloat fieldScaleZ;

    private final UIEditableFloat fieldSingleValue;

    private final UIToggleButton toggleVisible;

    private final Map<String, CachedTextMesh> textCache = new HashMap<>();
    private TextureGL fontAtlas = null;

    public PropertiesPanel(String id, eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        super(id);
        this.setBlocksInput(true);

        this.fieldPositionX = new UIEditableFloat(id + "_px", context);
        this.fieldPositionY = new UIEditableFloat(id + "_py", context);
        this.fieldPositionZ = new UIEditableFloat(id + "_pz", context);
        this.fieldRotationX = new UIEditableFloat(id + "_rx", context);
        this.fieldRotationY = new UIEditableFloat(id + "_ry", context);
        this.fieldRotationZ = new UIEditableFloat(id + "_rz", context);
        this.fieldScaleX = new UIEditableFloat(id + "_sx", context);
        this.fieldScaleY = new UIEditableFloat(id + "_sy", context);
        this.fieldScaleZ = new UIEditableFloat(id + "_sz", context);
        this.fieldSingleValue = new UIEditableFloat(id + "_sv", context);
        this.toggleVisible = new UIToggleButton(id + "_vis", context);

        this.addChild(this.fieldPositionX);
        this.addChild(this.fieldPositionY);
        this.addChild(this.fieldPositionZ);
        this.addChild(this.fieldRotationX);
        this.addChild(this.fieldRotationY);
        this.addChild(this.fieldRotationZ);
        this.addChild(this.fieldScaleX);
        this.addChild(this.fieldScaleY);
        this.addChild(this.fieldScaleZ);
        this.addChild(this.fieldSingleValue);
        this.addChild(this.toggleVisible);

        this.wireListeners();
        this.clearSelection();
    }

    private void wireListeners()
    {
        this.fieldPositionX.setListener(v -> this.notifyPoseChanged("position.x", v));
        this.fieldPositionY.setListener(v -> this.notifyPoseChanged("position.y", v));
        this.fieldPositionZ.setListener(v -> this.notifyPoseChanged("position.z", v));
        this.fieldRotationX.setListener(v -> this.notifyPoseChanged("rotation.x", v));
        this.fieldRotationY.setListener(v -> this.notifyPoseChanged("rotation.y", v));
        this.fieldRotationZ.setListener(v -> this.notifyPoseChanged("rotation.z", v));
        this.fieldScaleX.setListener(v -> this.notifyPoseChanged("scale.x", v));
        this.fieldScaleY.setListener(v -> this.notifyPoseChanged("scale.y", v));
        this.fieldScaleZ.setListener(v -> this.notifyPoseChanged("scale.z", v));
        this.fieldSingleValue.setListener(v -> this.notifyChanged(this.selectedProperty, v));
        this.toggleVisible.setListener(v -> this.notifyPoseChanged("visible", v));
    }

    private void notifyPoseChanged(String propertySuffix, Object value)
    {
        if (this.selectedBoneId == null)
            return;
        this.notifyChanged("bone:" + this.selectedBoneId + ":" + propertySuffix, value);
    }

    private void notifyChanged(String property, Object value)
    {
        if (this.callbacks == null || this.selectedBoneId == null || property == null)
            return;
        this.callbacks.onPropertyChanged(this.selectedBoneId, property, this.selectedTime, value);
    }

    public void setCallbacks(AnimationEditorCallbacks callbacks)
    {
        this.callbacks = callbacks;
    }

    public void clearSelection()
    {
        this.mode = Mode.EMPTY;
        this.selectedBoneId = null;
        this.selectedProperty = null;
        this.setAllFieldsVisible(false);
    }

    public void setKeyframeData(String boneId, String property, float time,
                                Object value, EasingType easing)
    {
        this.mode = Mode.KEYFRAME;
        this.selectedBoneId = boneId;
        this.selectedProperty = property;
        this.selectedTime = time;
        this.keyframeValue = value;

        this.setAllFieldsVisible(false);

        if (value instanceof Float)
        {
            float displayValue = (Float) value;
            if (property.contains("rotation."))
            {
                displayValue = (float) Math.toDegrees(displayValue);
            }
            this.fieldSingleValue.setValue(displayValue);
            this.positionField(this.fieldSingleValue, 0);
            this.fieldSingleValue.setVisible(true);
            this.fieldSingleValue.setEnabled(true);
        }
    }

    public void showLivePose(String boneId, float px, float py, float pz, float rx, float ry, float rz, float sx,
                             float sy, float sz)
    {
        this.selectedBoneId = boneId;
        this.mode = Mode.LIVE_POSE;

        this.fieldPositionX.setValue(px);
        this.fieldPositionY.setValue(py);
        this.fieldPositionZ.setValue(pz);

        this.fieldRotationX.setValue(rx);
        this.fieldRotationY.setValue(ry);
        this.fieldRotationZ.setValue(rz);

        this.fieldScaleX.setValue(sx);
        this.fieldScaleY.setValue(sy);
        this.fieldScaleZ.setValue(sz);

        this.fieldSingleValue.setVisible(false);
        this.toggleVisible.setVisible(false);

        this.fieldPositionX.setVisible(true);
        this.fieldPositionY.setVisible(true);
        this.fieldPositionZ.setVisible(true);
        this.fieldRotationX.setVisible(true);
        this.fieldRotationY.setVisible(true);
        this.fieldRotationZ.setVisible(true);
        this.fieldScaleX.setVisible(true);
        this.fieldScaleY.setVisible(true);
        this.fieldScaleZ.setVisible(true);

        this.markDirty();
    }

    public void setData(String boneId, Map<String, Object> liveValues)
    {
        this.mode = Mode.LIVE_POSE;
        this.selectedBoneId = boneId;

        this.fieldPositionX.setValue((Float) liveValues.get("position.x"));
        this.fieldPositionY.setValue((Float) liveValues.get("position.y"));
        this.fieldPositionZ.setValue((Float) liveValues.get("position.z"));

        this.fieldRotationX.setValue((Float) liveValues.get("rotation.x"));
        this.fieldRotationY.setValue((Float) liveValues.get("rotation.y"));
        this.fieldRotationZ.setValue((Float) liveValues.get("rotation.z"));

        this.fieldScaleX.setValue((Float) liveValues.get("scale.x"));
        this.fieldScaleY.setValue((Float) liveValues.get("scale.y"));
        this.fieldScaleZ.setValue((Float) liveValues.get("scale.z"));

        this.setAllFieldsVisible(false);

        this.fieldPositionX.setVisible(true);
        this.fieldPositionY.setVisible(true);
        this.fieldPositionZ.setVisible(true);
        this.fieldRotationX.setVisible(true);
        this.fieldRotationY.setVisible(true);
        this.fieldRotationZ.setVisible(true);
        this.fieldScaleX.setVisible(true);
        this.fieldScaleY.setVisible(true);
        this.fieldScaleZ.setVisible(true);

        this.fieldPositionX.setEnabled(true);
        this.fieldPositionY.setEnabled(true);
        this.fieldPositionZ.setEnabled(true);
        this.fieldRotationX.setEnabled(true);
        this.fieldRotationY.setEnabled(true);
        this.fieldRotationZ.setEnabled(true);
        this.fieldScaleX.setEnabled(true);
        this.fieldScaleY.setEnabled(true);
        this.fieldScaleZ.setEnabled(true);

        this.fieldSingleValue.setVisible(false);
        this.toggleVisible.setVisible(false);

        this.markDirty();
    }


    public void setPoseKeyframeData(String boneId, float time,
                                    Map<String, Transform> boneTransforms)
    {
        this.mode = Mode.POSE;
        this.selectedBoneId = boneId;
        this.selectedTime = time;
        this.poseTransforms = new HashMap<>(boneTransforms);

        this.setAllFieldsVisible(false);

        Transform transform = boneTransforms.get(boneId);
        if (transform == null && !boneTransforms.isEmpty())
            transform = boneTransforms.values().iterator().next();
        if (transform == null)
            return;

        this.fieldPositionX.setValue(transform.getTranslation().x);
        this.fieldPositionY.setValue(transform.getTranslation().y);
        this.fieldPositionZ.setValue(transform.getTranslation().z);

        Vector3f euler = new Vector3f();
        transform.getRotation().getEulerAnglesXYZ(euler);
        this.fieldRotationX.setValue((float) Math.toDegrees(euler.x));
        this.fieldRotationY.setValue((float) Math.toDegrees(euler.y));
        this.fieldRotationZ.setValue((float) Math.toDegrees(euler.z));

        this.fieldScaleX.setValue(transform.getScale().x);
        this.fieldScaleY.setValue(transform.getScale().y);
        this.fieldScaleZ.setValue(transform.getScale().z);

        int row = 0;
        this.positionField(this.fieldPositionX, row++);
        this.positionField(this.fieldPositionY, row++);
        this.positionField(this.fieldPositionZ, row++);
        row++;
        this.positionField(this.fieldRotationX, row++);
        this.positionField(this.fieldRotationY, row++);
        this.positionField(this.fieldRotationZ, row++);
        row++;
        this.positionField(this.fieldScaleX, row++);
        this.positionField(this.fieldScaleY, row++);
        this.positionField(this.fieldScaleZ, row++);
        row++;
        this.positionToggle(this.toggleVisible, row);

        this.fieldPositionX.setVisible(true);
        this.fieldPositionX.setEnabled(true);
        this.fieldPositionY.setVisible(true);
        this.fieldPositionY.setEnabled(true);
        this.fieldPositionZ.setVisible(true);
        this.fieldPositionZ.setEnabled(true);
        this.fieldRotationX.setVisible(true);
        this.fieldRotationX.setEnabled(true);
        this.fieldRotationY.setVisible(true);
        this.fieldRotationY.setEnabled(true);
        this.fieldRotationZ.setVisible(true);
        this.fieldRotationZ.setEnabled(true);
        this.fieldScaleX.setVisible(true);
        this.fieldScaleX.setEnabled(true);
        this.fieldScaleY.setVisible(true);
        this.fieldScaleY.setEnabled(true);
        this.fieldScaleZ.setVisible(true);
        this.fieldScaleZ.setEnabled(true);
        this.toggleVisible.setVisible(true);
        this.toggleVisible.setEnabled(true);
    }

    private void setAllFieldsVisible(boolean visible)
    {
        this.fieldPositionX.setVisible(visible);
        this.fieldPositionX.setEnabled(visible);
        this.fieldPositionY.setVisible(visible);
        this.fieldPositionY.setEnabled(visible);
        this.fieldPositionZ.setVisible(visible);
        this.fieldPositionZ.setEnabled(visible);
        this.fieldRotationX.setVisible(visible);
        this.fieldRotationX.setEnabled(visible);
        this.fieldRotationY.setVisible(visible);
        this.fieldRotationY.setEnabled(visible);
        this.fieldRotationZ.setVisible(visible);
        this.fieldRotationZ.setEnabled(visible);
        this.fieldScaleX.setVisible(visible);
        this.fieldScaleX.setEnabled(visible);
        this.fieldScaleY.setVisible(visible);
        this.fieldScaleY.setEnabled(visible);
        this.fieldScaleZ.setVisible(visible);
        this.fieldScaleZ.setEnabled(visible);
        this.fieldSingleValue.setVisible(visible);
        this.fieldSingleValue.setEnabled(visible);
        this.toggleVisible.setVisible(visible);
        this.toggleVisible.setEnabled(visible);
    }

    private void positionField(UIEditableFloat field, int row)
    {
        float contentStartY = AnimationEditorTheme.HEADER_HEIGHT;
        float fieldY = contentStartY + row * ROW_HEIGHT;
        float fieldX = LABEL_COLUMN_WIDTH;
        float fieldW = this.getComputedWidth() - LABEL_COLUMN_WIDTH - FIELD_PAD * 2f;

        field.getTransform()
                .set(0f, 0f, 0f, 0f)
                .setPixelOffset((int) fieldX, (int) fieldY)
                .setPixelSize(fieldW, ROW_HEIGHT - 2f);
        field.markDirty();
    }

    private void positionToggle(UIToggleButton toggle, int row)
    {
        float contentStartY = AnimationEditorTheme.HEADER_HEIGHT;
        float fieldY = contentStartY + row * ROW_HEIGHT;
        float fieldX = LABEL_COLUMN_WIDTH;
        float fieldW = this.getComputedWidth() - LABEL_COLUMN_WIDTH - FIELD_PAD * 2f;

        toggle.getTransform()
                .set(0f, 0f, 0f, 0f)
                .setPixelOffset((int) fieldX, (int) fieldY)
                .setPixelSize(fieldW, ROW_HEIGHT - 2f);
        toggle.markDirty();
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        if (this.mode == Mode.POSE || this.mode == Mode.LIVE_POSE)
        {
            int row = 0;
            this.positionField(this.fieldPositionX, row++);
            this.positionField(this.fieldPositionY, row++);
            this.positionField(this.fieldPositionZ, row++);
            row++;
            this.positionField(this.fieldRotationX, row++);
            this.positionField(this.fieldRotationY, row++);
            this.positionField(this.fieldRotationZ, row++);
            row++;
            this.positionField(this.fieldScaleX, row++);
            this.positionField(this.fieldScaleY, row++);
            this.positionField(this.fieldScaleZ, row++);
            row++;
            this.positionToggle(this.toggleVisible, row);
        } else if (this.mode == Mode.KEYFRAME)
        {
            this.positionField(this.fieldSingleValue, 0);
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

        renderer.pushClip(x, y, w, h);

        if (this.mode == Mode.EMPTY)
        {
            this.renderEmptyStateCard(renderer, x, y, w, h);
        } else if (this.mode == Mode.POSE || this.mode == Mode.LIVE_POSE)
        {
            this.renderPoseLabels(renderer, x, y, w);
        } else
        {
            this.renderKeyframeLabel(renderer, x, y, w);
        }

        renderer.popClip();

        float[] acc = AnimationEditorTheme.ACCENT_COLOR;
        renderer.drawRect(x, y, 1f, h, acc[0], acc[1], acc[2], 0.247f);
    }

    private void renderEmptyStateCard(UIRenderer renderer, float px, float py, float pw, float ph)
    {
        float cardW = pw - 32f;
        float cardH = 90f;
        float cardX = px + (pw - cardW) * 0.5f;
        float cardY = py + (ph - cardH) * 0.5f;

        float[] bg = AnimationEditorTheme.EMPTY_CARD_BG;
        renderer.drawRect(cardX, cardY, cardW, cardH, bg[0], bg[1], bg[2], 1.0f);

        float[] br = AnimationEditorTheme.EMPTY_CARD_BORDER;
        renderer.drawRect(cardX, cardY, cardW, 1f, br[0], br[1], br[2], 1.0f);
        renderer.drawRect(cardX, cardY + cardH - 1f, cardW, 1f, br[0], br[1], br[2], 1.0f);
        renderer.drawRect(cardX, cardY, 1f, cardH, br[0], br[1], br[2], 1.0f);
        renderer.drawRect(cardX + cardW - 1f, cardY, 1f, cardH, br[0], br[1], br[2], 1.0f);

        float[] acc = AnimationEditorTheme.ACCENT_COLOR;
        float iconCX = cardX + cardW * 0.5f;
        float iconTop = cardY + 10f;
        renderer.drawRect(iconCX - 7f, iconTop, 14f, 16f, acc[0], acc[1], acc[2], 0.85f);
        renderer.drawRect(iconCX - 7f, iconTop + 10f, 5f, 8f, bg[0], bg[1], bg[2], 1.0f);
        renderer.drawRect(iconCX + 2f, iconTop + 10f, 5f, 8f, bg[0], bg[1], bg[2], 1.0f);

        float labelY = iconTop + 22f;
        this.renderCenteredText(renderer, "Select a keyframe",
                cardX, labelY, cardW, 20f, AnimationEditorTheme.TEXT_PRIMARY, 13.0f);

        float hintY = labelY + 22f;
        this.renderCenteredText(renderer, "Click any keyframe to inspect",
                cardX, hintY, cardW, 18f, AnimationEditorTheme.TEXT_SECONDARY, 11.0f);
    }

    private void renderPoseLabels(UIRenderer renderer, float panelX, float panelY, float panelW)
    {
        float labelW = LABEL_COLUMN_WIDTH - FIELD_PAD;
        float contentY = panelY + AnimationEditorTheme.HEADER_HEIGHT;
        int row = 0;

        this.renderSectionHeader(renderer, "Position", panelX, contentY + row * ROW_HEIGHT, panelW);
        this.renderPropertyLabel(renderer, "X", panelX, contentY + (++row) * ROW_HEIGHT, labelW);
        this.renderPropertyLabel(renderer, "Y", panelX, contentY + (++row) * ROW_HEIGHT, labelW);
        this.renderPropertyLabel(renderer, "Z", panelX, contentY + (++row) * ROW_HEIGHT, labelW);

        this.renderSectionHeader(renderer, "Rotation (Deg)", panelX, contentY + (++row) * ROW_HEIGHT, panelW);
        this.renderPropertyLabel(renderer, "RX", panelX, contentY + (++row) * ROW_HEIGHT, labelW);
        this.renderPropertyLabel(renderer, "RY", panelX, contentY + (++row) * ROW_HEIGHT, labelW);
        this.renderPropertyLabel(renderer, "RZ", panelX, contentY + (++row) * ROW_HEIGHT, labelW);

        this.renderSectionHeader(renderer, "Scale", panelX, contentY + (++row) * ROW_HEIGHT, panelW);
        this.renderPropertyLabel(renderer, "SX", panelX, contentY + (++row) * ROW_HEIGHT, labelW);
        this.renderPropertyLabel(renderer, "SY", panelX, contentY + (++row) * ROW_HEIGHT, labelW);
        this.renderPropertyLabel(renderer, "SZ", panelX, contentY + (++row) * ROW_HEIGHT, labelW);

        this.renderSectionHeader(renderer, "Visible", panelX, contentY + (++row) * ROW_HEIGHT, panelW);
    }

    private void renderKeyframeLabel(UIRenderer renderer, float panelX, float panelY, float panelW)
    {
        float labelW = LABEL_COLUMN_WIDTH - FIELD_PAD;
        float contentY = panelY + AnimationEditorTheme.HEADER_HEIGHT;
        String name = this.selectedProperty != null ? this.selectedProperty : "Value";
        if (name.contains("rotation."))
        {
            name += " (Deg)";
        }
        this.renderPropertyLabel(renderer, name, panelX, contentY, labelW);
    }

    private void renderSectionHeader(UIRenderer renderer, String title,
                                     float x, float y, float w)
    {
        float[] sep = AnimationEditorTheme.DIVIDER_COLOR;
        renderer.drawRect(x + 4f, y, w - 8f, 1f, sep[0], sep[1], sep[2], 1.0f);
        this.renderText(renderer, title, x + 6f, y + 3f, 12.0f, AnimationEditorTheme.TEXT_SECONDARY);
    }

    private void renderPropertyLabel(UIRenderer renderer, String name,
                                     float x, float y, float maxW)
    {
        CachedTextMesh mesh = this.getOrBuildText(name);
        if (mesh == null || this.fontAtlas == null)
            return;

        float targetHeight = 12.0f;
        float scale = 1.0f;
        if (mesh.height > 0)
            scale = targetHeight / mesh.height;

        float tx = x + maxW - mesh.width * scale;
        float ty = y + (ROW_HEIGHT - mesh.height * scale) * 0.5f;

        Font font = FontManager.getFont(FONT_KEY);
        if (font != null)
            ty += font.getBaseline() * scale;

        float[] col = AnimationEditorTheme.TEXT_SECONDARY;
        renderer.drawText(mesh.data, this.fontAtlas,
                tx, ty, scale, col[0], col[1], col[2], col[3]);
    }

    private void renderText(UIRenderer renderer, String text, float x, float y,
                            float targetHeight, float[] color)
    {
        if (this.fontAtlas == null)
            return;
        CachedTextMesh mesh = this.getOrBuildText(text);
        if (mesh == null)
            return;

        float scale = 1.0f;
        if (mesh.height > 0)
            scale = targetHeight / mesh.height;

        Font font = FontManager.getFont(FONT_KEY);
        float ty = y;
        if (font != null)
            ty += font.getBaseline() * scale;

        renderer.drawText(mesh.data, this.fontAtlas,
                x, ty, scale, color[0], color[1], color[2], color[3]);
    }

    private void renderCenteredText(UIRenderer renderer, String text,
                                    float x, float y, float w, float h)
    {
        this.renderCenteredText(renderer, text, x, y, w, h,
                AnimationEditorTheme.TEXT_SECONDARY, 16.0f);
    }

    private void renderCenteredText(UIRenderer renderer, String text,
                                    float x, float y, float w, float h,
                                    float[] color, float targetHeight)
    {
        if (this.fontAtlas == null)
            return;
        CachedTextMesh mesh = this.getOrBuildText(text);
        if (mesh == null)
            return;

        float scale = 1.0f;
        if (mesh.height > 0)
            scale = targetHeight / mesh.height;

        float tx = x + (w - mesh.width * scale) * 0.5f;
        float ty = y + (h - mesh.height * scale) * 0.5f;

        Font font = FontManager.getFont(FONT_KEY);
        if (font != null)
            ty += font.getBaseline() * scale;

        renderer.drawText(mesh.data, this.fontAtlas,
                tx, ty, scale, color[0], color[1], color[2], color[3]);
    }

    private void ensureFontAtlas()
    {
        if (this.fontAtlas == null)
            this.fontAtlas = FontManager.getAtlas(FONT_KEY);
    }

    private CachedTextMesh getOrBuildText(String text)
    {
        if (this.textCache.containsKey(text))
            return this.textCache.get(text);

        Font font = FontManager.getFont(FONT_KEY);
        if (font == null)
            return null;

        TextShaper shaper = new TextShaper();
        TextLayout layout = shaper.shape(text, font, font.getNativeSize());
        MeshData data = TextMeshGenerator.generate(layout, font);
        if (data.indices.length == 0)
            return null;

        BufferLayout spec = BufferLayout.builder()
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

    public void cleanup()
    {
        for (CachedTextMesh mesh : this.textCache.values())
            mesh.destroy();
        this.textCache.clear();
        this.fieldPositionX.cleanup();
        this.fieldPositionY.cleanup();
        this.fieldPositionZ.cleanup();
        this.fieldRotationX.cleanup();
        this.fieldRotationY.cleanup();
        this.fieldRotationZ.cleanup();
        this.fieldScaleX.cleanup();
        this.fieldScaleY.cleanup();
        this.fieldScaleZ.cleanup();
        this.fieldSingleValue.cleanup();
        this.toggleVisible.cleanup();
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
