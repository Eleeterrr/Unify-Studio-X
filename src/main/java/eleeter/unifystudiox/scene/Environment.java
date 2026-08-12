package eleeter.unifystudiox.scene;

import org.joml.Vector3f;

import eleeter.unifystudiox.renderer.core.RenderSettings;
import eleeter.unifystudiox.scene.io.SerializeProperty;

public class Environment
{

    @SerializeProperty
    private final Vector3f sunDirection = new Vector3f(0.4f, -1.0f, 0.3f).normalize();
    @SerializeProperty
    private final Vector3f sunColor = new Vector3f(1.0f, 0.95f, 0.85f);
    @SerializeProperty
    private float sunIntensity = 1.0f;

    @SerializeProperty
    private final Vector3f ambientColor = new Vector3f(0.15f, 0.15f, 0.2f);
    @SerializeProperty
    private float ambientIntensity = 0.4f;

    @SerializeProperty
    private boolean fogEnabled = true;
    @SerializeProperty
    private final Vector3f fogColor = new Vector3f(0.6f, 0.7f, 0.8f);
    @SerializeProperty
    private float fogDensity = 0.007f;
    @SerializeProperty
    private float fogStart = 20.0f;
    @SerializeProperty
    private float fogEnd = 300.0f;

    @SerializeProperty
    private final Vector3f skyColorZenith = new Vector3f(0.18f, 0.47f, 0.85f);
    @SerializeProperty
    private final Vector3f skyColorHorizon = new Vector3f(0.6f, 0.78f, 0.92f);

    public Vector3f getSunDirection()
    {
        return this.sunDirection;
    }

    public Vector3f getSunColor()
    {
        if (RenderSettings.NIGHT_MODE)
        {
            return new Vector3f(0.5f, 0.6f, 0.85f); /* Pale blue moonlight */
        }
        return this.sunColor;
    }

    public float getSunIntensity()
    {
        if (RenderSettings.NIGHT_MODE)
        {
            return this.sunIntensity * 0.15f;
        }
        return this.sunIntensity;
    }

    public Vector3f getAmbientColor()
    {
        if (RenderSettings.NIGHT_MODE)
        {
            return new Vector3f(0.04f, 0.05f, 0.1f);
        }
        return this.ambientColor;
    }

    public float getAmbientIntensity()
    {
        if (RenderSettings.NIGHT_MODE)
        {
            return this.ambientIntensity * 0.6f;
        }
        return this.ambientIntensity;
    }

    public boolean isFogEnabled()
    {
        return this.fogEnabled;
    }

    public Vector3f getFogColor()
    {
        if (RenderSettings.NIGHT_MODE)
        {
            return new Vector3f(0.03f, 0.04f, 0.08f);
        }
        return this.fogColor;
    }

    public float getFogDensity()
    {
        return this.fogDensity;
    }

    public float getFogStart()
    {
        return this.fogStart;
    }

    public float getFogEnd()
    {
        return this.fogEnd;
    }

    public Vector3f getSkyColorZenith()
    {
        return this.skyColorZenith;
    }

    public Vector3f getSkyColorHorizon()
    {
        return this.skyColorHorizon;
    }

    public Environment setSunDirection(float x, float y, float z)
    {
        this.sunDirection.set(x, y, z).normalize();
        return this;
    }

    public Environment setSunColor(float r, float g, float b)
    {
        this.sunColor.set(r, g, b);
        return this;
    }

    public Environment setSunIntensity(float intensity)
    {
        this.sunIntensity = intensity;
        return this;
    }

    public Environment setAmbientColor(float r, float g, float b)
    {
        ambientColor.set(r, g, b);
        return this;
    }

    public Environment setFogEnabled(boolean enabled)
    {
        this.fogEnabled = enabled;
        return this;
    }

    public Environment setFogDensity(float density)
    {
        this.fogDensity = density;
        return this;
    }

    public Environment setFogRange(float start, float end)
    {
        this.fogStart = start;
        this.fogEnd = end;
        return this;
    }
}
