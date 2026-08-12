package eleeter.unifystudiox.anchor;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class AnchorBuilder
{
    private final BoneTarget target;
    private String boneName = null;
    private int boneIndex = -1;

    private final Vector3f offsetT = new Vector3f(0);
    private final Quaternionf offsetR = new Quaternionf();
    private final Vector3f offsetS = new Vector3f(1);

    public AnchorBuilder(BoneTarget target)
    {
        this.target = target;
    }

    public AnchorBuilder toBone(String name)
    {
        this.boneName = name;
        this.boneIndex = -1;
        return this;
    }

    public AnchorBuilder toBone(int index)
    {
        this.boneIndex = index;
        this.boneName = null;
        return this;
    }

    public AnchorBuilder offsetPosition(float x, float y, float z)
    {
        this.offsetT.set(x, y, z);
        return this;
    }

    public AnchorBuilder offsetRotation(float pitchDeg, float yawDeg, float rollDeg)
    {
        this.offsetR.rotationXYZ(
                (float) Math.toRadians(pitchDeg),
                (float) Math.toRadians(yawDeg),
                (float) Math.toRadians(rollDeg));
        return this;
    }

    public AnchorBuilder offsetRotationQ(float x, float y, float z, float w)
    {
        this.offsetR.set(x, y, z, w);
        return this;
    }

    public AnchorBuilder offsetScale(float x, float y, float z)
    {
        this.offsetS.set(x, y, z);
        return this;
    }

    public AnchorDefinition build()
    {
        AnchorDefinition def = new AnchorDefinition(this.target, this.boneName, this.boneIndex);
        def.offsetTranslation.set(this.offsetT);
        def.offsetRotation.set(this.offsetR);
        def.offsetScale.set(this.offsetS);
        return def;
    }
}
