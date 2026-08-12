package eleeter.unifystudiox.cubic.render;

import java.lang.Math;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import eleeter.unifystudiox.cubic.CubicAnimation;
import eleeter.unifystudiox.cubic.CubicBoneAnimation;
import eleeter.unifystudiox.cubic.CubicKeyframe;

public class CubeAnimationPlayer
{
    private CubicAnimation animation;
    private CubeRuntimeNode node;
    private CubicBoneAnimation boneAnim;
    private CubicKeyframe start;
    private CubicKeyframe end;
    private float factor;


    public void apply(CubeRuntimeModel model, String animationName, float time)
    {
        this.animation = null;
        for (CubicAnimation anim : model.animations)
        {
            if (anim.name.equals(animationName))
            {
                this.animation = anim;
                break;
            }
        }

        if (this.animation == null) return;

        for (Map.Entry<String, CubicBoneAnimation> entry : this.animation.bones.entrySet())
        {
            this.node = model.nodesByName.get(entry.getKey());
            if (this.node == null || this.node.isManuallyControlled) continue;

            this.boneAnim = entry.getValue();

            sampleVector(this.boneAnim.positionKeyframes, time, this.node.translation, new Vector3f(0, 0, 0));

            Vector3f eulerRot = new Vector3f();
            sampleVector(this.boneAnim.rotationKeyframes, time, eulerRot, new Vector3f(0, 0, 0));


            this.node.rotation.identity().rotateXYZ((float) Math.toRadians(eulerRot.x), (float) Math.toRadians(eulerRot.y), (float) Math.toRadians(eulerRot.z)
            );

            sampleVector(this.boneAnim.scaleKeyframes, time, this.node.scale, new Vector3f(1, 1, 1));
        }
    }

    private void sampleVector(List<CubicKeyframe> keyframes, float time, Vector3f out, Vector3f defaultValue)
    {
        if (keyframes.isEmpty())
        {
            out.set(defaultValue);
            return;
        }

        if (time <= keyframes.get(0).time)
        {
            out.set(keyframes.get(0).value);
            return;
        }

        if (time >= keyframes.get(keyframes.size() - 1).time)
        {
            out.set(keyframes.get(keyframes.size() - 1).value);
            return;
        }

        for (int i = 0; i < keyframes.size() - 1; i++)
        {
            this.start = keyframes.get(i);
            this.end = keyframes.get(i + 1);

            if (time >= this.start.time && time < this.end.time)
            {
                this.factor = (time - this.start.time) / (this.end.time - this.start.time);
                this.start.value.lerp(this.end.value, this.factor, out);
                return;
            }
        }
    }
}
