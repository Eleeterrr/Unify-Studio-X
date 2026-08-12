package eleeter.unifystudiox.animation.runtime;

import eleeter.unifystudiox.animation.api.AnimatableObject;
import eleeter.unifystudiox.animation.api.AnimationSystem;
import eleeter.unifystudiox.animation.data.AnimationClip;
import eleeter.unifystudiox.animation.data.AnimationTrack;
import eleeter.unifystudiox.animation.data.BoneInfo;
import eleeter.unifystudiox.animation.data.Keyframe;
import eleeter.unifystudiox.animation.data.PoseKeyframe;
import eleeter.unifystudiox.animation.data.Skeleton;
import eleeter.unifystudiox.animation.data.Transform;
import eleeter.unifystudiox.util.log.AniLogger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector3f;

public class AnimationSystemImpl implements AnimationSystem
{
    private final Map<String, AnimatableObject> objectRegistry = new HashMap<>();
    private final Map<String, Skeleton> skeletonRegistry = new HashMap<>();
    private final Map<String, AnimationPlayer> playerRegistry = new HashMap<>();
    private final Map<String, List<AnimationClip>> clipRegistry = new HashMap<>();

    @Override
    public void register(String objectId, AnimatableObject animatableObject)
    {
        validateObjectId(objectId, "register");
        if (animatableObject == null)
        {
            throw new IllegalArgumentException(
                    "AnimationSystemImpl.register: animatableObject must not be null. objectId: '" + objectId + "'.");
        }

        if (this.objectRegistry.containsKey(objectId))
        {
            AniLogger.warn("AnimationSystem",
                    "register() called for objectId '" + objectId + "' which is already registered. Overwriting.");
        }

        this.objectRegistry.put(objectId, animatableObject);
        this.playerRegistry.put(objectId, new AnimationPlayer());
        this.clipRegistry.put(objectId, new ArrayList<>());
    }

    @Override
    public void registerSkeleton(String objectId, Skeleton skeleton)
    {
        validateObjectId(objectId, "registerSkeleton");
        if (skeleton == null)
        {
            throw new IllegalArgumentException(
                    "AnimationSystemImpl.registerSkeleton: skeleton must not be null. objectId: '" + objectId + "'.");
        }

        if (this.skeletonRegistry.containsKey(objectId))
        {
            AniLogger.warn("AnimationSystem",
                    "registerSkeleton() called for objectId '" + objectId + "' which already has a skeleton. Overwriting.");
        }

        this.skeletonRegistry.put(objectId, skeleton);
    }

    @Override
    public AnimatableObject getAnimatableObject(String objectId)
    {
        validateRegisteredObject(objectId, "getAnimatableObject");
        return this.objectRegistry.get(objectId);
    }

    @Override
    public List<String> getRegisteredObjects()
    {
        return new ArrayList<>(this.objectRegistry.keySet());
    }

    @Override
    public List<BoneInfo> getBonesFor(String objectId)
    {
        validateObjectId(objectId, "getBonesFor");

        Skeleton skeleton = this.skeletonRegistry.get(objectId);
        if (skeleton == null)
        {
            return Collections.emptyList();
        }

        return skeleton.getBones();
    }

    @Override
    public void addClip(String objectId, AnimationClip clip)
    {
        validateRegisteredObject(objectId, "addClip");
        if (clip == null)
        {
            throw new IllegalArgumentException(
                    "AnimationSystemImpl.addClip: clip must not be null. objectId: '" + objectId + "'.");
        }

        this.clipRegistry.get(objectId).add(clip);
    }

    @Override
    public List<AnimationClip> getClips(String objectId)
    {
        validateRegisteredObject(objectId, "getClips");
        return Collections.unmodifiableList(this.clipRegistry.get(objectId));
    }

    @Override
    public void addKeyframe(String objectId, String clipName, String trackTargetId, String propertyName, Keyframe<?> keyframe)
    {
        validateRegisteredObject(objectId, "addKeyframe");
        if (clipName == null || clipName.isEmpty())
        {
            throw new IllegalArgumentException(
                    "AnimationSystemImpl.addKeyframe: clipName must not be null or empty.");
        }
        if (trackTargetId == null || trackTargetId.isEmpty())
        {
            throw new IllegalArgumentException(
                    "AnimationSystemImpl.addKeyframe: trackTargetId must not be null or empty.");
        }
        if (propertyName == null || propertyName.isEmpty())
        {
            throw new IllegalArgumentException(
                    "AnimationSystemImpl.addKeyframe: propertyName must not be null or empty.");
        }
        if (keyframe == null)
        {
            throw new IllegalArgumentException(
                    "AnimationSystemImpl.addKeyframe: keyframe must not be null.");
        }

        List<AnimationClip> clips = this.clipRegistry.get(objectId);

        AnimationClip targetClip = null;
        int targetClipIndex = -1;

        for (int index = 0; index < clips.size(); index++)
        {
            if (clips.get(index).getName().equals(clipName))
            {
                targetClip = clips.get(index);
                targetClipIndex = index;
                break;
            }
        }

        if (targetClip == null)
        {
            throw new IllegalArgumentException(
                    "No clip named '" + clipName + "' exists for object '" + objectId + "'. Call addClip() first.");
        }

        @SuppressWarnings("unchecked")
        AnimationTrack<Object> existingTrack = (AnimationTrack<Object>) targetClip
                .getTrack(trackTargetId, propertyName)
                .orElse(null);

        AnimationTrack<Object> updatedTrack;
        if (existingTrack == null)
        {
            updatedTrack = new AnimationTrack<>(trackTargetId, propertyName);
        } else
        {
            updatedTrack = existingTrack;
        }

        @SuppressWarnings("unchecked")
        Keyframe<Object> typedKeyframe = (Keyframe<Object>) keyframe;
        updatedTrack = updatedTrack.addKeyframe(typedKeyframe);

        AnimationClip.Builder clipBuilder = new AnimationClip.Builder()
                .setName(targetClip.getName())
                .setDuration(targetClip.getDurationSeconds());

        boolean wasReplaced = false;
        for (AnimationTrack<?> existingClipTrack : targetClip.getTracks())
        {
            boolean isTheUpdatedTrack = existingClipTrack.getTargetId().equals(trackTargetId)
                    && existingClipTrack.getPropertyName().equals(propertyName);

            if (isTheUpdatedTrack)
            {
                clipBuilder.addTrack(updatedTrack);
                wasReplaced = true;
            } else
            {
                clipBuilder.addTrack(existingClipTrack);
            }
        }

        if (!wasReplaced)
        {
            clipBuilder.addTrack(updatedTrack);
        }

        AnimationClip newClip = clipBuilder.build();
        clips.set(targetClipIndex, newClip);

        AnimationPlayer player = this.playerRegistry.get(objectId);
        if (player != null && player.getCurrentClip() != null
                && player.getCurrentClip().getName().equals(newClip.getName()))
        {
            player.updateClip(newClip);
        }
    }

    @Override
    public void removeKeyframe(String objectId, String clipName, String trackTargetId, String propertyName, float time)
    {
        validateRegisteredObject(objectId, "removeKeyframe");
        if (clipName == null || clipName.isEmpty())
        {
            throw new IllegalArgumentException(
                    "AnimationSystemImpl.removeKeyframe: clipName must not be null or empty.");
        }

        List<AnimationClip> clips = this.clipRegistry.get(objectId);
        AnimationClip targetClip = null;
        int targetClipIndex = -1;

        for (int i = 0; i < clips.size(); i++)
        {
            if (clips.get(i).getName().equals(clipName))
            {
                targetClip = clips.get(i);
                targetClipIndex = i;
                break;
            }
        }

        if (targetClip == null)
        {
            throw new IllegalArgumentException(
                    "No clip named '" + clipName + "' exists for object '" + objectId + "'. Call addClip() first.");
        }

        AnimationTrack<?> existingTrack = targetClip.getTrack(trackTargetId, propertyName).orElse(null);
        if (existingTrack != null)
        {
            boolean found = false;
            List<Keyframe<?>> newKeyframes = new ArrayList<>();
            for (Keyframe<?> kf : existingTrack.getKeyframes())
            {
                if (Float.compare(kf.getTime(), time) == 0)
                {
                    found = true;
                } else
                {
                    newKeyframes.add(kf);
                }
            }

            if (found)
            {
                AnimationTrack<Object> updatedTrack = new AnimationTrack<>(trackTargetId, propertyName);
                for (Keyframe<?> kf : newKeyframes)
                {
                    @SuppressWarnings("unchecked")
                    Keyframe<Object> typed = (Keyframe<Object>) kf;
                    updatedTrack = updatedTrack.addKeyframe(typed);
                }

                AnimationClip.Builder clipBuilder = new AnimationClip.Builder()
                        .setName(targetClip.getName())
                        .setDuration(targetClip.getDurationSeconds());

                for (AnimationTrack<?> clipTrack : targetClip.getTracks())
                {
                    if (clipTrack.getTargetId().equals(trackTargetId) && clipTrack.getPropertyName().equals(propertyName))
                    {
                        clipBuilder.addTrack(updatedTrack);
                    } else
                    {
                        clipBuilder.addTrack(clipTrack);
                    }
                }
                AnimationClip newClip = clipBuilder.build();
                clips.set(targetClipIndex, newClip);

                AnimationPlayer player = this.playerRegistry.get(objectId);
                if (player != null && player.getCurrentClip() != null
                        && player.getCurrentClip().getName().equals(newClip.getName()))
                {
                    player.updateClip(newClip);
                }
            }
        }
    }

    @Override
    public List<Keyframe<?>> getKeyframesFor(String objectId, String trackTargetId, String propertyName)
    {
        validateRegisteredObject(objectId, "getKeyframesFor");
        if (trackTargetId == null || trackTargetId.isEmpty())
        {
            throw new IllegalArgumentException(
                    "AnimationSystemImpl.getKeyframesFor: trackTargetId must not be null or empty.");
        }
        if (propertyName == null || propertyName.isEmpty())
        {
            throw new IllegalArgumentException(
                    "AnimationSystemImpl.getKeyframesFor: propertyName must not be null or empty.");
        }

        for (AnimationClip clip : this.clipRegistry.get(objectId))
        {
            AnimationTrack<?> track = clip.getTrack(trackTargetId, propertyName).orElse(null);
            if (track != null)
            {
                return Collections.unmodifiableList(new ArrayList<>(track.getKeyframes()));
            }
        }

        return Collections.emptyList();
    }

    @Override
    public void addPoseKeyframe(String objectId, String clipName, PoseKeyframe poseKeyframe)
    {
        validateRegisteredObject(objectId, "addPoseKeyframe");
        if (clipName == null || clipName.isEmpty())
        {
            throw new IllegalArgumentException(
                    "AnimationSystemImpl.addPoseKeyframe: clipName must not be null or empty.");
        }
        if (poseKeyframe == null)
        {
            throw new IllegalArgumentException(
                    "AnimationSystemImpl.addPoseKeyframe: poseKeyframe must not be null.");
        }

        // Expand the pose keyframe into individual per-bone per-component keyframes.
        // Each bone transform is split into translation x/y/z, rotation quaternion x/y/z/w, scale x/y/z.
        for (Map.Entry<String, Transform> boneEntry : poseKeyframe.getBoneTransforms().entrySet())
        {
            String boneId = boneEntry.getKey();
            Transform transform = boneEntry.getValue();

            addKeyframe(objectId, clipName, boneId, "bone:" + boneId + ":position.x",
                    new Keyframe<>(poseKeyframe.getTime(), transform.getTranslation().x, poseKeyframe.getEasingType()));
            addKeyframe(objectId, clipName, boneId, "bone:" + boneId + ":position.y",
                    new Keyframe<>(poseKeyframe.getTime(), transform.getTranslation().y, poseKeyframe.getEasingType()));
            addKeyframe(objectId, clipName, boneId, "bone:" + boneId + ":position.z",
                    new Keyframe<>(poseKeyframe.getTime(), transform.getTranslation().z, poseKeyframe.getEasingType()));

            Vector3f euler = new Vector3f();
            transform.getRotation().getEulerAnglesXYZ(euler);

            addKeyframe(objectId, clipName, boneId, "bone:" + boneId + ":rotation.x",
                    new Keyframe<>(poseKeyframe.getTime(), euler.x, poseKeyframe.getEasingType()));
            addKeyframe(objectId, clipName, boneId, "bone:" + boneId + ":rotation.y",
                    new Keyframe<>(poseKeyframe.getTime(), euler.y, poseKeyframe.getEasingType()));
            addKeyframe(objectId, clipName, boneId, "bone:" + boneId + ":rotation.z",
                    new Keyframe<>(poseKeyframe.getTime(), euler.z, poseKeyframe.getEasingType()));

            addKeyframe(objectId, clipName, boneId, "bone:" + boneId + ":scale.x",
                    new Keyframe<>(poseKeyframe.getTime(), transform.getScale().x, poseKeyframe.getEasingType()));
            addKeyframe(objectId, clipName, boneId, "bone:" + boneId + ":scale.y",
                    new Keyframe<>(poseKeyframe.getTime(), transform.getScale().y, poseKeyframe.getEasingType()));
            addKeyframe(objectId, clipName, boneId, "bone:" + boneId + ":scale.z",
                    new Keyframe<>(poseKeyframe.getTime(), transform.getScale().z, poseKeyframe.getEasingType()));
        }
    }

    @Override
    public void play(String objectId, String clipName)
    {
        validateRegisteredObject(objectId, "play");
        if (clipName == null || clipName.isEmpty())
        {
            throw new IllegalArgumentException(
                    "AnimationSystemImpl.play: clipName must not be null or empty. objectId: '" + objectId + "'.");
        }

        AnimationClip clipToPlay = null;
        for (AnimationClip clip : this.clipRegistry.get(objectId))
        {
            if (clip.getName().equals(clipName))
            {
                clipToPlay = clip;
                break;
            }
        }

        if (clipToPlay == null)
        {
            throw new IllegalArgumentException(
                    "AnimationSystemImpl.play: no clip named '" + clipName + "' found for objectId '" + objectId + "'."
                            + " Call addClip() first.");
        }

        this.playerRegistry.get(objectId).play(clipToPlay);
    }

    @Override
    public void pause(String objectId)
    {
        validateRegisteredObject(objectId, "pause");
        this.playerRegistry.get(objectId).pause();
    }

    @Override
    public void seekToTime(String objectId, float timeInSeconds)
    {
        validateRegisteredObject(objectId, "seekToTime");
        if (timeInSeconds < 0f)
        {
            throw new IllegalArgumentException(
                    "AnimationSystemImpl.seekToTime: timeInSeconds must be >= 0. Got: " + timeInSeconds);
        }
        this.playerRegistry.get(objectId).seekToTime(timeInSeconds);
    }

    @Override
    public float getCurrentTime(String objectId)
    {
        validateRegisteredObject(objectId, "getCurrentTime");
        return this.playerRegistry.get(objectId).getCurrentTime();
    }

    @Override
    public void update(float deltaTime)
    {
        if (deltaTime < 0f)
        {
            throw new IllegalArgumentException(
                    "AnimationSystemImpl.update: deltaTime must be >= 0. Got: " + deltaTime);
        }

        for (Map.Entry<String, AnimationPlayer> entry : this.playerRegistry.entrySet())
        {
            String objectId = entry.getKey();
            AnimationPlayer player = entry.getValue();
            AnimatableObject target = this.objectRegistry.get(objectId);

            // target should always be present if the player exists, but guard defensively.
            if (target != null)
            {
                player.update(deltaTime, target);
            }
        }
    }

    private void validateObjectId(String objectId, String callerMethod)
    {
        if (objectId == null || objectId.isEmpty())
        {
            throw new IllegalArgumentException(
                    "AnimationSystemImpl." + callerMethod + ": objectId must not be null or empty.");
        }
    }

    private void validateRegisteredObject(String objectId, String callerMethod)
    {
        validateObjectId(objectId, callerMethod);
        if (!this.objectRegistry.containsKey(objectId))
        {
            throw new IllegalArgumentException(
                    "AnimationSystemImpl." + callerMethod + ": objectId '" + objectId
                            + "' is not registered. Call register() first.");
        }
    }
}
