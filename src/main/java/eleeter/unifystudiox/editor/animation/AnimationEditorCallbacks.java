package eleeter.unifystudiox.editor.animation;



public interface AnimationEditorCallbacks
{
    void onPlayRequested();

    void onPauseRequested();

    void onStopRequested();

    void onAddKeyframeRequested(float time);

    void onAddPoseKeyframeRequested(float time);


    void onBoneSelected(String boneId);

    void onKeyframeSelected(String boneId, String property, float time);

    void onKeyframeMoved(String boneId, String property, float oldTime, float newTime);

    void onTimeChanged(float newTime);

    void onPropertyChanged(String boneId, String property, float time, Object newValue);

    void onObjectSelected(String objectId);

    void onClipSelected(String clipName);

    void onKeyframeTypeSelected(KeyframeType type);
}
