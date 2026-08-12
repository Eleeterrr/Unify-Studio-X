package eleeter.unifystudiox.cubic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CubicBoneAnimation
{
    public final String boneName;
    public final List<CubicKeyframe> positionKeyframes = new ArrayList<>();
    public final List<CubicKeyframe> rotationKeyframes = new ArrayList<>();
    public final List<CubicKeyframe> scaleKeyframes = new ArrayList<>();

    public CubicBoneAnimation(String boneName)
    {
        this.boneName = boneName;
    }

    public void sortKeyframes()
    {
        Comparator<CubicKeyframe> comparator = Comparator.comparingDouble(keyframe -> keyframe.time);
        this.positionKeyframes.sort(comparator);
        this.rotationKeyframes.sort(comparator);
        this.scaleKeyframes.sort(comparator);
    }
}
