package eleeter.unifystudiox.animation.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class Skeleton
{
    private final List<BoneInfo> bones;

    public Skeleton(List<BoneInfo> bones)
    {
        if (bones == null)
        {
            throw new IllegalArgumentException(
                    "Skeleton: bones list must not be null.");
        }

        List<BoneInfo> defensiveCopy = new ArrayList<>(bones.size());
        for (int index = 0; index < bones.size(); index++)
        {
            if (bones.get(index) == null)
            {
                throw new IllegalArgumentException(
                        "Skeleton: bones list contains a null element at index " + index + ".");
            }
            defensiveCopy.add(bones.get(index));
        }

        this.bones = Collections.unmodifiableList(defensiveCopy);
    }


    public List<BoneInfo> getBones()
    {
        return this.bones;
    }


    public Optional<BoneInfo> getBone(String boneId)
    {
        if (boneId == null)
        {
            throw new IllegalArgumentException(
                    "Skeleton.getBone: boneId must not be null.");
        }

        for (BoneInfo boneInfo : this.bones)
        {
            if (boneInfo.getId().equals(boneId))
            {
                return Optional.of(boneInfo);
            }
        }

        return Optional.empty();
    }

    public List<BoneInfo> getRootBones()
    {
        List<BoneInfo> rootBones = new ArrayList<>();

        for (BoneInfo boneInfo : this.bones)
        {
            if (boneInfo.getParentId() == null)
            {
                rootBones.add(boneInfo);
            }
        }

        return rootBones;
    }


    public List<BoneInfo> getChildren(String parentId)
    {
        if (parentId == null)
        {
            throw new IllegalArgumentException(
                    "Skeleton.getChildren: parentId must not be null.");
        }

        List<BoneInfo> children = new ArrayList<>();

        for (BoneInfo boneInfo : this.bones)
        {
            if (parentId.equals(boneInfo.getParentId()))
            {
                children.add(boneInfo);
            }
        }

        return children;
    }

    public int getBoneCount()
    {
        return this.bones.size();
    }

    @Override
    public String toString()
    {
        return "Skeleton{boneCount=" + this.bones.size() + '}';
    }
}
