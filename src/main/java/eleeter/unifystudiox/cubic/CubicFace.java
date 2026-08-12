package eleeter.unifystudiox.cubic;

import org.joml.Vector4f;

public class CubicFace
{
    public final String direction;
    public final Vector4f uv;
    public final int textureIndex;
    public final int rotation;

    public CubicFace(String direction, Vector4f uv, int textureIndex, int rotation)
    {
        this.direction = direction;
        this.uv = uv;
        this.textureIndex = textureIndex;
        this.rotation = rotation;
    }
}
