package eleeter.unifystudiox.animation.runtime;

import eleeter.unifystudiox.animation.api.AnimatableObject;
import eleeter.unifystudiox.animation.api.Interpolatable;
import eleeter.unifystudiox.animation.data.AnimationClip;
import eleeter.unifystudiox.animation.data.AnimationTrack;
import eleeter.unifystudiox.animation.data.EasingType;
import eleeter.unifystudiox.animation.data.Keyframe;
import eleeter.unifystudiox.animation.interpolation.BooleanInterpolator;
import eleeter.unifystudiox.animation.interpolation.LinearFloatInterpolator;
import eleeter.unifystudiox.animation.interpolation.LinearVector3Interpolator;
import eleeter.unifystudiox.animation.interpolation.SlerpQuaternionInterpolator;
import eleeter.unifystudiox.animation.interpolation.StepInterpolator;
import java.util.List;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class AnimationPlayer
{
    private static final LinearFloatInterpolator FLOAT_LINEAR = new LinearFloatInterpolator();
    private static final LinearVector3Interpolator VECTOR3_LINEAR = new LinearVector3Interpolator();
    private static final SlerpQuaternionInterpolator QUAT_SLERP = new SlerpQuaternionInterpolator();
    private static final StepInterpolator<Object> STEP_GENERIC = new StepInterpolator<>();
    private static final BooleanInterpolator BOOL_STEP = new BooleanInterpolator();

    private AnimationClip currentClip;
    private float currentTime;
    private boolean playing;

    public AnimationPlayer()
    {
        this.currentClip = null;
        this.currentTime = 0f;
        this.playing = false;
    }

    public void play(AnimationClip clip)
    {
        if (clip == null)
        {
            throw new IllegalArgumentException(
                    "AnimationPlayer.play: clip must not be null.");
        }
        this.currentClip = clip;
        this.currentTime = 0f;
        this.playing = true;
    }

    public void updateClip(AnimationClip clip)
    {
        if (clip == null) return;
        this.currentClip = clip;
    }

    public AnimationClip getCurrentClip()
    {
        return this.currentClip;
    }

    public void pause()
    {
        this.playing = false;
    }

    public void seekToTime(float timeInSeconds)
    {
        if (timeInSeconds < 0f)
        {
            throw new IllegalArgumentException(
                    "AnimationPlayer.seekToTime: time must be >= 0. Got: " + timeInSeconds);
        }
        this.currentTime = timeInSeconds;
    }

    public float getCurrentTime()
    {
        return this.currentTime;
    }

    public boolean isPlaying()
    {
        return this.playing;
    }

    public void update(float deltaTime, AnimatableObject target)
    {
        if (this.currentClip == null)
        {
            return;
        }

        if (this.playing)
        {
            this.currentTime += deltaTime;

            // Loop back to the beginning when the clip duration is exceeded.
            if (this.currentTime > this.currentClip.getDurationSeconds())
            {
                this.currentTime = this.currentTime % this.currentClip.getDurationSeconds();
            }
        }

        List<AnimationTrack<?>> tracks = this.currentClip.getTracks();

        for (AnimationTrack<?> track : tracks)
        {
            evaluateAndApplyTrack(track, this.currentTime, target);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void evaluateAndApplyTrack(AnimationTrack<T> track, float time, AnimatableObject target)
    {
        List<Keyframe<T>> keyframes = track.getKeyframes();

        if (keyframes.isEmpty())
        {
            return;
        }

        Interpolatable<T> strategy = resolveStrategy(keyframes, track);
        T evaluatedValue = Interpolator.evaluate(keyframes, time, strategy);
        target.setProperty(track.getPropertyName(), evaluatedValue);
    }

    @SuppressWarnings("unchecked")
    private <T> Interpolatable<T> resolveStrategy(List<Keyframe<T>> keyframes, AnimationTrack<T> track)
    {
        EasingType easing = keyframes.get(0).getEasingType();

        if (easing == EasingType.STEP)
        {
            return (Interpolatable<T>) STEP_GENERIC;
        }

        T firstValue = keyframes.get(0).getValue();

        if (firstValue instanceof Float)
        {
            return (Interpolatable<T>) FLOAT_LINEAR;
        }
        if (firstValue instanceof Vector3f)
        {
            return (Interpolatable<T>) VECTOR3_LINEAR;
        }
        if (firstValue instanceof Quaternionf)
        {
            return (Interpolatable<T>) QUAT_SLERP;
        }
        if (firstValue instanceof Boolean)
        {
            return (Interpolatable<T>) BOOL_STEP;
        }

        throw new IllegalStateException(
                "AnimationPlayer: no interpolation strategy registered for value type '"
                        + firstValue.getClass().getSimpleName()
                        + "' on track: " + track);
    }
}
