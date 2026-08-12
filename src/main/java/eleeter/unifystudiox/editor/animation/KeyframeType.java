package eleeter.unifystudiox.editor.animation;


public enum KeyframeType
{
    POSE("Pose Keyframe", "diamond_large"),

    PROPERTY("Property Keyframe", "diamond_small");
    private final String displayName;
    private final String shapeHint;


    KeyframeType(String displayName, String shapeHint)
    {
        this.displayName = displayName;
        this.shapeHint = shapeHint;
    }

    public String getDisplayName()
    {
        return this.displayName;
    }

    public String getShapeHint()
    {
        return this.shapeHint;
    }
}
