package eleeter.unifystudiox.animation.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AnimationTrack<T>
{
    private final String targetId;
    private final String propertyName;
    private final List<Keyframe<T>> keyframes;

    public AnimationTrack(String targetId, String propertyName)
    {
        if (targetId == null || targetId.isEmpty())
        {
            throw new IllegalArgumentException(
                "AnimationTrack: targetId must not be null or empty.");
        }
        if (propertyName == null || propertyName.isEmpty())
        {
            throw new IllegalArgumentException(
                "AnimationTrack: propertyName must not be null or empty.");
        }

        this.targetId     = targetId;
        this.propertyName = propertyName;
        this.keyframes    = new ArrayList<>();
    }

    private AnimationTrack(String targetId, String propertyName, List<Keyframe<T>> existingKeyframes)
    {
        this.targetId     = targetId;
        this.propertyName = propertyName;
        this.keyframes    = new ArrayList<>(existingKeyframes);
    }

    public AnimationTrack<T> addKeyframe(Keyframe<T> keyframe)
    {
        if (keyframe == null)
        {
            throw new IllegalArgumentException(
                "AnimationTrack.addKeyframe: keyframe must not be null.");
        }

        List<Keyframe<T>> updated = new ArrayList<>();
        boolean replaced = false;
        for (Keyframe<T> kf : this.keyframes)
        {
            if (Float.compare(kf.getTime(), keyframe.getTime()) == 0)
            {
                updated.add(keyframe);
                replaced = true;
            }
            else
            {
                updated.add(kf);
            }
        }
        
        if (!replaced)
        {
            updated.add(keyframe);
        }

        // Keep keyframes sorted by ascending time so the interpolator can binary-scan them.
        updated.sort((first, second) -> Float.compare(first.getTime(), second.getTime()));

        return new AnimationTrack<>(this.targetId, this.propertyName, updated);
    }

    public Optional<Keyframe<T>> getKeyframeAtTime(float time)
    {
        for (Keyframe<T> keyframe : this.keyframes)
        {
            if (Float.compare(keyframe.getTime(), time) == 0)
            {
                return Optional.of(keyframe);
            }
        }
        return Optional.empty();
    }

    public String getTargetId()
    {
        return this.targetId;
    }

    public String getPropertyName()
    {
        return this.propertyName;
    }

    public List<Keyframe<T>> getKeyframes()
    {
        return Collections.unmodifiableList(this.keyframes);
    }

    @Override
    public String toString()
    {
        return "AnimationTrack{"
            + "targetId='" + this.targetId + '\''
            + ", property='" + this.propertyName + '\''
            + ", keyframeCount=" + this.keyframes.size()
            + '}';
    }
}
