package eleeter.unifystudiox.animation.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AnimationClip
{
    private final String name;
    private final float durationSeconds;
    private final List<AnimationTrack<?>> tracks;

    private AnimationClip(Builder builder)
    {
        this.name = builder.name;
        this.durationSeconds = builder.durationSeconds;
        this.tracks = Collections.unmodifiableList(new ArrayList<>(builder.tracks));
    }

    public String getName()
    {
        return this.name;
    }

    public float getDurationSeconds()
    {
        return this.durationSeconds;
    }

    public List<AnimationTrack<?>> getTracks()
    {
        return this.tracks;
    }

    public Optional<AnimationTrack<?>> getTrack(String targetId, String propertyName)
    {
        if (targetId == null || targetId.isEmpty())
        {
            throw new IllegalArgumentException(
                    "AnimationClip.getTrack: targetId must not be null or empty.");
        }
        if (propertyName == null || propertyName.isEmpty())
        {
            throw new IllegalArgumentException(
                    "AnimationClip.getTrack: propertyName must not be null or empty.");
        }

        for (AnimationTrack<?> track : this.tracks)
        {
            if (track.getTargetId().equals(targetId) && track.getPropertyName().equals(propertyName))
            {
                return Optional.of(track);
            }
        }

        return Optional.empty();
    }

    @Override
    public String toString()
    {
        return "AnimationClip{"
                + "name='" + this.name + '\''
                + ", duration=" + this.durationSeconds + "s"
                + ", trackCount=" + this.tracks.size()
                + '}';
    }

    public static final class Builder
    {
        private String name;
        private float durationSeconds;
        private final List<AnimationTrack<?>> tracks = new ArrayList<>();

        public Builder setName(String name)
        {
            if (name == null || name.isEmpty())
            {
                throw new IllegalArgumentException(
                        "AnimationClip.Builder.setName: name must not be null or empty.");
            }
            this.name = name;
            return this;
        }

        public Builder setDuration(float durationSeconds)
        {
            if (durationSeconds <= 0f)
            {
                throw new IllegalArgumentException(
                        "AnimationClip.Builder.setDuration: duration must be > 0. Got: " + durationSeconds);
            }
            this.durationSeconds = durationSeconds;
            return this;
        }

        public Builder addTrack(AnimationTrack<?> track)
        {
            if (track == null)
            {
                throw new IllegalArgumentException(
                        "AnimationClip.Builder.addTrack: track must not be null.");
            }
            this.tracks.add(track);
            return this;
        }

        public AnimationClip build()
        {
            if (this.name == null || this.name.isEmpty())
            {
                throw new IllegalStateException(
                        "AnimationClip.Builder.build: name has not been set. Call setName() first.");
            }
            if (this.durationSeconds <= 0f)
            {
                throw new IllegalStateException(
                        "AnimationClip.Builder.build: duration has not been set. Call setDuration() first.");
            }
            return new AnimationClip(this);
        }
    }
}
