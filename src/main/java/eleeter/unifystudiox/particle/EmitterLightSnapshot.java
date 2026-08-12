package eleeter.unifystudiox.particle;

import org.joml.Matrix4f;

public class EmitterLightSnapshot
{
    public boolean active;
    public boolean castsShadow;
    public float shadowDirX;
    public float shadowDirY = -1.0F;
    public float shadowDirZ;
    public float x;
    public float y;
    public float z;
    public float r = 1.0F;
    public float g = 1.0F;
    public float b = 1.0F;
    public float intensity;
    public float range;

    public final Matrix4f lightView = new Matrix4f();
    public final Matrix4f lightProjection = new Matrix4f();
    public final Matrix4f lightSpaceMatrix = new Matrix4f();
}
