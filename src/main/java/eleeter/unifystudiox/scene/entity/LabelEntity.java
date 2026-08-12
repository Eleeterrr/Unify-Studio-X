package eleeter.unifystudiox.scene.entity;

import eleeter.unifystudiox.gizmo.Ray;
import eleeter.unifystudiox.graphics.text.render.MeshData;
import eleeter.unifystudiox.graphics.text.render.TextMeshGenerator;
import eleeter.unifystudiox.graphics.text.render.font.Font;
import eleeter.unifystudiox.graphics.text.render.shaping.TextLayout;
import eleeter.unifystudiox.graphics.text.render.shaping.TextShaper;
import eleeter.unifystudiox.scene.SelectionResult;
import eleeter.unifystudiox.scene.io.SerializeProperty;
import org.joml.Vector3f;
import org.joml.Vector4f;


public class LabelEntity extends BaseSceneEntity implements Positionable, Pickable
{
    private final String id;

    @SerializeProperty
    private String text = "";
    private Font font = null;
    @SerializeProperty
    private float fontSize = 0.5F;
    @SerializeProperty
    private final Vector4f color = new Vector4f(1f, 1f, 1f, 1f);


    @SerializeProperty
    private boolean billboard = true;

    private static final TextShaper SHAPER = new TextShaper();

    private MeshData meshData;
    private boolean meshDirty = true;
    private float layoutWidth = 0f;
    private float layoutHeight = 0f;


    private static final float PICK_RADIUS_SQ = 0.5f * 0.5f;


    public LabelEntity(String id)
    {
        this.id = id;
    }


    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public void update(double deltaTime)
    {  }

    @Override
    public void cleanup()
    {
    }

    @Override
    public SelectionResult pick(Ray ray)
    {
        Vector3f pos = getPosition();
        Vector3f w = new Vector3f(pos).sub(ray.origin());
        float t = w.dot(ray.direction());

        if (t < 0) return SelectionResult.empty();

        Vector3f closest = new Vector3f(ray.direction()).mul(t).add(ray.origin());
        float distSq = closest.distanceSquared(pos);

        if (distSq <= PICK_RADIUS_SQ)
        {
            return new SelectionResult(this, t, -1, null);
        }
        return SelectionResult.empty();
    }


    public LabelEntity setText(String text)
    {
        this.text = (text == null) ? "" : text;
        this.meshDirty = true;
        return this;
    }


    public LabelEntity setFont(Font font)
    {
        this.font = font;
        this.meshDirty = true;
        return this;
    }

    public LabelEntity setFontSize(float fontSize)
    {
        this.fontSize = fontSize;
        this.meshDirty = true;
        return this;
    }


    public LabelEntity setColor(float r, float g, float b, float a)
    {
        this.color.set(r, g, b, a);
        return this;
    }

    public LabelEntity setColor(float r, float g, float b)
    {
        return setColor(r, g, b, 1f);
    }


    public LabelEntity setBillboard(boolean billboard)
    {
        this.billboard = billboard;
        return this;
    }

    @Override
    public void setVisible(boolean visible)
    {
        super.setVisible(visible);
    }


    public MeshData getMeshData()
    {
        if (!this.meshDirty) return this.meshData;

        if (this.font == null || this.text.isEmpty())
        {
            this.meshData = null;
            this.meshDirty = false;
            return null;
        }

        TextLayout layout = SHAPER.shape(this.text, this.font, this.fontSize);
        this.layoutWidth = layout.getWidth();
        this.layoutHeight = layout.getHeight();
        this.meshData = TextMeshGenerator.generate(layout, this.font);
        this.meshDirty = false;
        return this.meshData;
    }


    public boolean isMeshDirty()
    {
        return this.meshDirty;
    }


    public Font getFont()
    {
        return this.font;
    }

    public String getText()
    {
        return this.text;
    }

    public float getFontSize()
    {
        return this.fontSize;
    }

    public Vector4f getColor()
    {
        return this.color;
    }

    public boolean isBillboard()
    {
        return this.billboard;
    }

    public float getLayoutWidth()
    {
        return this.layoutWidth;
    }

    public float getLayoutHeight()
    {
        return this.layoutHeight;
    }
}
