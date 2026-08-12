package eleeter.unifystudiox.animation.api;

import java.util.List;

import eleeter.unifystudiox.animation.data.AnimationClip;
import eleeter.unifystudiox.animation.data.BoneInfo;
import eleeter.unifystudiox.animation.data.Keyframe;
import eleeter.unifystudiox.animation.data.PoseKeyframe;
import eleeter.unifystudiox.animation.data.Skeleton;

public interface AnimationSystem
{
    void register(String objectId, AnimatableObject animatableObject);
    void registerSkeleton(String objectId, Skeleton skeleton);

    AnimatableObject getAnimatableObject(String objectId);

    List<String> getRegisteredObjects();

    List<BoneInfo> getBonesFor(String objectId);

    void addClip(String objectId, AnimationClip clip);

    List<AnimationClip> getClips(String objectId);

    void addKeyframe(String objectId, String clipName, String trackTargetId, String propertyName, Keyframe<?> keyframe);

    void removeKeyframe(String objectId, String clipName, String trackTargetId, String propertyName, float time);

    List<Keyframe<?>> getKeyframesFor(String objectId, String trackTargetId, String propertyName);

    void addPoseKeyframe(String objectId, String clipName, PoseKeyframe poseKeyframe);

    void play(String objectId, String clipName);

    void pause(String objectId);

    void seekToTime(String objectId, float timeInSeconds);

    float getCurrentTime(String objectId);

    void update(float deltaTime);
}
