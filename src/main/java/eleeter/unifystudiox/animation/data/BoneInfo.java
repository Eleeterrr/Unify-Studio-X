package eleeter.unifystudiox.animation.data;


public final class BoneInfo
{
    private final String id;
    private final String displayName;
    private final String parentId;
    private final Transform restPose;

    public BoneInfo(String id, String displayName, String parentId, Transform restPose)
    {
        if (id == null || id.isEmpty())
        {
            throw new IllegalArgumentException(
                    "BoneInfo: id must not be null or empty.");
        }
        if (displayName == null || displayName.isEmpty())
        {
            throw new IllegalArgumentException(
                    "BoneInfo: displayName must not be null or empty. Bone id: '" + id + "'.");
        }
        if (restPose == null)
        {
            throw new IllegalArgumentException(
                    "BoneInfo: restPose must not be null. Bone id: '" + id + "'.");
        }

        this.id = id;
        this.displayName = displayName;
        this.parentId = parentId;
        this.restPose = restPose;
    }

    public String getId()
    {
        return this.id;
    }


    public String getDisplayName()
    {
        return this.displayName;
    }


    public String getParentId()
    {
        return this.parentId;
    }


    public Transform getRestPose()
    {
        return this.restPose;
    }

    @Override
    public String toString()
    {
        return "BoneInfo{"
                + "id='" + this.id + '\''
                + ", displayName='" + this.displayName + '\''
                + ", parentId='" + this.parentId + '\''
                + '}';
    }
}
