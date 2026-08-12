package eleeter.unifystudiox.scene;

import eleeter.unifystudiox.scene.io.SerializeProperty;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SpotlightData
{

    public final Vector3f position = new Vector3f(0f, 5f, 0f);


    public final Vector3f direction = new Vector3f(0f, -1f, 0f);

    @SerializeProperty
    public final Vector3f color = new Vector3f(1f, 1f, 1f);

    @SerializeProperty
    public float intensity = 3.0f;


    @SerializeProperty
    public float innerCutoffDeg = 15.0f;


    @SerializeProperty
    public float outerCutoffDeg = 25.0f;


    @SerializeProperty
    public float range = 10.0F;

    @SerializeProperty
    public boolean enabled = true;

    @SerializeProperty
    public boolean castShadow = false;

    public final Matrix4f lightSpaceMatrix = new Matrix4f();

    public final Matrix4f lightView = new Matrix4f();


    public final Matrix4f lightProjection = new Matrix4f();


    public SpotlightData setPosition(float x, float y, float z)
    {
        this.position.set(x, y, z);
        return this;
    }

    public SpotlightData setDirection(float x, float y, float z)
    {
        this.direction.set(x, y, z).normalize();
        return this;
    }

    public SpotlightData setColor(float r, float g, float b)
    {
        this.color.set(r, g, b);
        return this;
    }

    public SpotlightData setIntensity(float intensity)
    {
        this.intensity = intensity;
        return this;
    }

    public SpotlightData setCutoff(float innerDeg, float outerDeg)
    {
        this.innerCutoffDeg = innerDeg;
        this.outerCutoffDeg = outerDeg;
        return this;
    }

    public SpotlightData setRange(float range)
    {
        this.range = range;
        return this;
    }

    public SpotlightData setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }


    public void updateLightSpaceMatrix()
    {
        float fovY = (float) Math.toRadians(this.outerCutoffDeg * 2.0f);
        this.lightProjection.identity().perspective(fovY, 1.0f, 0.1f, this.range);

        Vector3f spotCenter = new Vector3f(this.position).add(this.direction);
        Vector3f up = new Vector3f(0, 1, 0);
        if (Math.abs(this.direction.y) > 0.999f)
        {
            up.set(1, 0, 0);
        }

        this.lightView.identity().lookAt(this.position, spotCenter, up);

        this.lightProjection.mul(this.lightView, this.lightSpaceMatrix);
    }
}
