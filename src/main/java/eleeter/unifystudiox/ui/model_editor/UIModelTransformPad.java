package eleeter.unifystudiox.ui.model_editor;

import java.util.HashMap;
import java.util.Map;

import org.joml.Quaternionf;
import org.joml.Vector3f;

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
import eleeter.unifystudiox.scene.entity.Positionable;
import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.widgets.UINumberField;

public class UIModelTransformPad extends UIPanel
{
    private static final String FONT_KEY = "inter";
    private static final BufferLayout TEXT_BUFFER_LAYOUT = BufferLayout.builder()
            .add(0, 3, AttributeType.FLOAT)
            .add(1, 2, AttributeType.FLOAT)
            .build();

    private static final float PANEL_W = 414.0F;
    private static final float PANEL_H = 126.0F;
    private static final float LABEL_W = 76.0F;
    private static final float FIELD_GAP = 8.0F;
    private static final float ROW_GAP = 9.0F;
    private static final float ROW_H = 36.0F;

    private static final String[] ROW_LABELS = {"Trans", "Rotation", "Scale"};

    private final Map<String, CachedTextMesh> labelCache = new HashMap<>();
    private final UINumberField[] fields;

    private TextureGL fontAtlas;
    private Positionable target;
    private boolean syncingFromTarget = false;

    public UIModelTransformPad()
    {
        super("model_transform_pad");
        this.setBlocksInput(false);
        this.setVisible(false);
        this.setZIndex(24);

        this.fields = new UINumberField[]
        {
                this.createField("field_tx", "X", 0.02F, 0.95F, 0.45F, 0.45F),
                this.createField("field_ty", "Y", 0.02F, 0.55F, 0.92F, 0.62F),
                this.createField("field_tz", "Z", 0.02F, 0.48F, 0.72F, 1.00F),
                this.createField("field_rx", "X", 0.50F, 0.95F, 0.45F, 0.45F),
                this.createField("field_ry", "Y", 0.50F, 0.55F, 0.92F, 0.62F),
                this.createField("field_rz", "Z", 0.50F, 0.48F, 0.72F, 1.00F),
                this.createField("field_sx", "X", 0.01F, 0.95F, 0.45F, 0.45F),
                this.createField("field_sy", "Y", 0.01F, 0.55F, 0.92F, 0.62F),
                this.createField("field_sz", "Z", 0.01F, 0.48F, 0.72F, 1.00F)
        };

        for (UINumberField field : this.fields)
        {
            this.addChild(field);
        }

        this.fields[0].setOnValueChanged(v -> this.onFieldChanged());
        this.fields[1].setOnValueChanged(v -> this.onFieldChanged());
        this.fields[2].setOnValueChanged(v -> this.onFieldChanged());
        this.fields[3].setOnValueChanged(v -> this.onFieldChanged());
        this.fields[4].setOnValueChanged(v -> this.onFieldChanged());
        this.fields[5].setOnValueChanged(v -> this.onFieldChanged());
        this.fields[6].setOnValueChanged(v -> this.onFieldChanged());
        this.fields[7].setOnValueChanged(v -> this.onFieldChanged());
        this.fields[8].setOnValueChanged(v -> this.onFieldChanged());
    }

    public float getPreferredWidth()
    {
        return PANEL_W;
    }

    public float getPreferredHeight()
    {
        return PANEL_H;
    }

    public void setDocked(float x, float y, float w, float h)
    {
        this.cx = x;
        this.cy = y;
        this.cw = w;
        this.ch = h;
        this.layoutFields();
    }

    public void setTarget(Positionable target)
    {
        this.target = target;
        this.setVisible(target != null);
        if (target != null)
        {
            this.syncFromTarget();
        }
    }

    public boolean isInteracting()
    {
        for (UINumberField field : this.fields)
        {
            if (field.isInteracting())
            {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        this.layoutFields();
        if (!this.isVisible() || this.target == null)
        {
            return;
        }

        if (!this.isInteracting())
        {
            this.syncFromTarget();
        }
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        if (!this.isVisible() || this.target == null)
        {
            return;
        }

        this.ensureFontAtlas();
        for (int row = 0; row < ROW_LABELS.length; row++)
        {
            float rowY = this.cy + row * (ROW_H + ROW_GAP);
            this.drawLeftText(renderer, ROW_LABELS[row], this.cx, rowY + 12.0F, 13.0F, 0.95F, 0.97F, 1.00F, 0.98F);
        }
    }

    public void cleanup()
    {
        for (UINumberField field : this.fields)
        {
            field.cleanup();
        }
        for (CachedTextMesh mesh : this.labelCache.values())
        {
            mesh.destroy();
        }
        this.labelCache.clear();
    }

    private UINumberField createField(String id, String prefix, float step, float r, float g, float b)
    {
        UINumberField field = new UINumberField(id);
        field.setPrefix(prefix);
        field.setStep(step);
        field.setTextColor(r, g, b, 1.0F);
        return field;
    }

    private void onFieldChanged()
    {
        if (this.syncingFromTarget || this.target == null)
        {
            return;
        }

        this.target.setLocalPosition(new Vector3f(this.fields[0].getValue(),
                this.fields[1].getValue(),
                this.fields[2].getValue()
        ));
        this.target.setLocalRotation(new Quaternionf().rotationXYZ((float) Math.toRadians(this.fields[3].getValue()), (float) Math.toRadians(this.fields[4].getValue()), (float) Math.toRadians(this.fields[5].getValue())
        ));
        this.target.setLocalScale(new Vector3f(this.fields[6].getValue(), this.fields[7].getValue(), this.fields[8].getValue()
        ));
    }

    private void syncFromTarget()
    {
        if (this.target == null)
        {
            return;
        }

        this.syncingFromTarget = true;
        Vector3f position = this.target.getLocalPosition();
        Vector3f euler = new Vector3f();
        this.target.getLocalRotation().getEulerAnglesXYZ(euler);
        Vector3f scale = this.target.getLocalScale();

        this.fields[0].setValue(position.x);
        this.fields[1].setValue(position.y);
        this.fields[2].setValue(position.z);
        this.fields[3].setValue((float) Math.toDegrees(euler.x));
        this.fields[4].setValue((float) Math.toDegrees(euler.y));
        this.fields[5].setValue((float) Math.toDegrees(euler.z));
        this.fields[6].setValue(scale.x);
        this.fields[7].setValue(scale.y);
        this.fields[8].setValue(scale.z);
        this.syncingFromTarget = false;
    }

    private void layoutFields()
    {
        float fieldW = (this.cw - LABEL_W - FIELD_GAP * 2.0F) / 3.0F;
        for (int row = 0; row < 3; row++)
        {
            float rowY = this.cy + row * (ROW_H + ROW_GAP);
            for (int col = 0; col < 3; col++)
            {
                UINumberField field = this.fields[row * 3 + col];
                int fieldX = Math.round(this.cx + LABEL_W + col * (fieldW + FIELD_GAP));
                int fieldY = Math.round(rowY);
                field.getTransform().set(0.0F, 0.0F, 0.0F, 0.0F).setPixelOffset(fieldX, fieldY).setPixelSize(fieldW, ROW_H);
                field.markDirty();
                field.updateLayout(this.cx, this.cy, this.cw, this.ch);
            }
        }
    }

    private void drawLeftText(UIRenderer renderer, String text, float x, float y, float targetHeight,
                              float r, float g, float b, float a)
    {
        CachedTextMesh mesh = this.getOrCreateLabelMesh(text);
        if (mesh == null || this.fontAtlas == null)
        {
            return;
        }

        float scale = mesh.height > 0.0F ? targetHeight / mesh.height : 1.0F;
        Font font = FontManager.getFont(FONT_KEY);
        float ty = y;
        if (font != null)
        {
            ty += font.getBaseline() * scale;
        }

        renderer.drawText(mesh.data, this.fontAtlas, x, ty, scale, r, g, b, a);
    }

    private CachedTextMesh getOrCreateLabelMesh(String text)
    {
        CachedTextMesh existing = this.labelCache.get(text);
        if (existing != null)
        {
            return existing;
        }

        CachedTextMesh built = this.buildMesh(text);
        if (built != null)
        {
            this.labelCache.put(text, built);
        }
        return built;
    }

    private CachedTextMesh buildMesh(String text)
    {
        Font font = FontManager.getFont(FONT_KEY);
        if (font == null)
        {
            return null;
        }

        TextShaper shaper = new TextShaper();
        TextLayout layout = shaper.shape(text, font, font.getNativeSize());
        MeshData data = TextMeshGenerator.generate(layout, font);
        if (data.indices.length == 0)
        {
            return null;
        }

        VertexBuffer vbo = new VertexBuffer(data.vertices, GpuBufferUsage.STATIC);
        VertexBuffer ebo = new VertexBuffer(data.indices, GpuBufferUsage.STATIC);
        Vao vao = Vao.builder()
                .bindVertexBuffer(vbo, TEXT_BUFFER_LAYOUT)
                .elementBuffer(ebo)
                .build();
        return new CachedTextMesh(data, layout.getWidth(), layout.getHeight());
    }

    private void ensureFontAtlas()
    {
        if (this.fontAtlas == null)
        {
            this.fontAtlas = FontManager.getAtlas(FONT_KEY);
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

}
