package eleeter.unifystudiox.amb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import eleeter.unifystudiox.animation.api.AnimatableObject;
import eleeter.unifystudiox.animation.api.SkeletonProvider;
import eleeter.unifystudiox.animation.data.BoneInfo;
import eleeter.unifystudiox.animation.data.Skeleton;
import eleeter.unifystudiox.animation.data.Transform;

public class AmbModelAnimatableAdapter implements AnimatableObject, SkeletonProvider
{
    private static final String PROPERTY_POSITION_X = "position.x";
    private static final String PROPERTY_POSITION_Y = "position.y";
    private static final String PROPERTY_POSITION_Z = "position.z";
    private static final String PROPERTY_ROTATION_X = "rotation.x";
    private static final String PROPERTY_ROTATION_Y = "rotation.y";
    private static final String PROPERTY_ROTATION_Z = "rotation.z";
    private static final String PROPERTY_SCALE_X = "scale.x";
    private static final String PROPERTY_SCALE_Y = "scale.y";
    private static final String PROPERTY_SCALE_Z = "scale.z";
    private static final String PROPERTY_VISIBLE = "visible";

    private static final String BONE_PREFIX = "bone:";
    private static final int BONE_PREFIX_PARTS = 3;

    private final AmbModelInstance instance;
    private final Skeleton skeleton;
    private final List<String> supportedProperties;

    public AmbModelAnimatableAdapter(AmbModelInstance instance)
    {
        if (instance == null)
        {
            throw new IllegalArgumentException(
                    "AmbModelAnimatableAdapter: instance must not be null.");
        }

        this.instance = instance;
        this.skeleton = buildSkeletonSnapshot(instance);
        this.supportedProperties = buildSupportedPropertyList(instance);
    }

    @Override
    public String getObjectId()
    {
        return this.instance.getId();
    }

    @Override
    public Object getProperty(String propertyName)
    {
        if (propertyName == null || propertyName.isEmpty())
        {
            throw new IllegalArgumentException(
                    "AmbModelAnimatableAdapter.getProperty: propertyName must not be null or empty.");
        }

        if (propertyName.startsWith(BONE_PREFIX))
        {
            return readBoneProperty(propertyName);
        }

        throw new IllegalArgumentException(
                "AmbModelAnimatableAdapter.getProperty: unknown property '" + propertyName + "'."
                        + " Supported properties are listed by getSupportedProperties().");
    }

    @Override
    public void setProperty(String propertyName, Object value)
    {
        if (propertyName == null || propertyName.isEmpty())
        {
            throw new IllegalArgumentException(
                    "AmbModelAnimatableAdapter.setProperty: propertyName must not be null or empty.");
        }
        if (value == null)
        {
            throw new IllegalArgumentException(
                    "AmbModelAnimatableAdapter.setProperty: value must not be null. property: '" + propertyName + "'.");
        }

        if (propertyName.startsWith(BONE_PREFIX))
        {
            writeBoneProperty(propertyName, value);
            return;
        }

        throw new IllegalArgumentException(
                "AmbModelAnimatableAdapter.setProperty: unknown property '" + propertyName + "'."
                        + " Supported properties are listed by getSupportedProperties().");
    }

    @Override
    public List<String> getSupportedProperties()
    {
        return this.supportedProperties;
    }

    @Override
    public Skeleton getSkeleton()
    {
        return this.skeleton;
    }

    private Object readBoneProperty(String propertyName)
    {
        String[] parts = parseBonePropertyParts(propertyName);
        String boneId = parts[1];
        String component = parts[2];

        AmbBone bone = findBoneById(boneId, propertyName);

        switch (component)
        {
            case PROPERTY_POSITION_X:
                return bone.localTranslation.x;
            case PROPERTY_POSITION_Y:
                return bone.localTranslation.y;
            case PROPERTY_POSITION_Z:
                return bone.localTranslation.z;
            case PROPERTY_ROTATION_X:
                return bone.localRotation.x;
            case PROPERTY_ROTATION_Y:
                return bone.localRotation.y;
            case PROPERTY_ROTATION_Z:
                return bone.localRotation.z;
            case PROPERTY_SCALE_X:
                return bone.localScale.x;
            case PROPERTY_SCALE_Y:
                return bone.localScale.y;
            case PROPERTY_SCALE_Z:
                return bone.localScale.z;
            case PROPERTY_VISIBLE:
                return true;
            default:
                throw new IllegalArgumentException(
                        "AmbModelAnimatableAdapter: unknown bone component '" + component
                                + "' in property '" + propertyName + "'."
                                + " Valid components: position.x/y/z, rotation.x/y/z, scale.x/y/z, visible.");
        }
    }

    private void writeBoneProperty(String propertyName, Object value)
    {
        String[] parts = parseBonePropertyParts(propertyName);
        String boneId = parts[1];
        String component = parts[2];

        AmbBone bone = findBoneById(boneId, propertyName);

        float floatValue;
        switch (component)
        {
            case PROPERTY_POSITION_X:
                floatValue = requireFloat(value, propertyName);
                bone.localTranslation.x = floatValue;
                break;
            case PROPERTY_POSITION_Y:
                floatValue = requireFloat(value, propertyName);
                bone.localTranslation.y = floatValue;
                break;
            case PROPERTY_POSITION_Z:
                floatValue = requireFloat(value, propertyName);
                bone.localTranslation.z = floatValue;
                break;
            case PROPERTY_ROTATION_X:
                floatValue = requireFloat(value, propertyName);
                bone.localRotation.x = floatValue;
                break;
            case PROPERTY_ROTATION_Y:
                floatValue = requireFloat(value, propertyName);
                bone.localRotation.y = floatValue;
                break;
            case PROPERTY_ROTATION_Z:
                floatValue = requireFloat(value, propertyName);
                bone.localRotation.z = floatValue;
                break;
            case PROPERTY_SCALE_X:
                floatValue = requireFloat(value, propertyName);
                bone.localScale.x = floatValue;
                break;
            case PROPERTY_SCALE_Y:
                floatValue = requireFloat(value, propertyName);
                bone.localScale.y = floatValue;
                break;
            case PROPERTY_SCALE_Z:
                floatValue = requireFloat(value, propertyName);
                bone.localScale.z = floatValue;
                break;
            case PROPERTY_VISIBLE:
                break;
            default:
                throw new IllegalArgumentException(
                        "AmbModelAnimatableAdapter: unknown bone component '" + component
                                + "' in property '" + propertyName + "'."
                                + " Valid components: position.x/y/z, rotation.x/y/z, scale.x/y/z, visible.");
        }
    }

    private String[] parseBonePropertyParts(String propertyName)
    {

        if (!propertyName.startsWith(BONE_PREFIX))
        {
            throw new IllegalArgumentException("Property does not start with " + BONE_PREFIX);
        }

        int firstColon = propertyName.indexOf(':');
        int lastColon = propertyName.lastIndexOf(':');

        if (firstColon == lastColon || firstColon == -1)
        {
            throw new IllegalArgumentException(
                    "AmbModelAnimatableAdapter: bone property '" + propertyName
                            + "' is malformed. Expected format: 'bone:BONE_ID:COMPONENT'.");
        }

        String boneId = propertyName.substring(firstColon + 1, lastColon);
        String component = propertyName.substring(lastColon + 1);

        return new String[]{"bone", boneId, component};
    }

    private AmbBone findBoneById(String boneId, String originalPropertyName)
    {
        for (AmbBone bone : this.instance.sourceModel.skeleton.bones)
        {
            if (boneId.equals(bone.name))
            {
                return bone;
            }
        }

        throw new IllegalArgumentException(
                "AmbModelAnimatableAdapter: no bone with id '" + boneId
                        + "' found in model '" + this.instance.getId()
                        + "'. Property attempted: '" + originalPropertyName + "'.");
    }

    private float requireFloat(Object value, String propertyName)
    {
        if (!(value instanceof Float))
        {
            throw new IllegalArgumentException(
                    "AmbModelAnimatableAdapter.setProperty: expected Float for property '"
                            + propertyName + "' but got " + value.getClass().getSimpleName() + ".");
        }
        return (Float) value;
    }

    private static Skeleton buildSkeletonSnapshot(AmbModelInstance instance)
    {
        List<AmbBone> ambBones = instance.sourceModel.skeleton.bones;
        List<BoneInfo> boneInfoList = new ArrayList<>(ambBones.size());

        for (AmbBone ambBone : ambBones)
        {
            String parentId = null;
            if (ambBone.parentIndex >= 0 && ambBone.parentIndex < ambBones.size())
            {
                parentId = ambBones.get(ambBone.parentIndex).name;
            }

            Vector3f restTranslation = new Vector3f();
            Quaternionf restRotation = new Quaternionf();
            Vector3f restScale = new Vector3f();

            ambBone.bindLocalMatrix.getTranslation(restTranslation);
            ambBone.bindLocalMatrix.getNormalizedRotation(restRotation);
            ambBone.bindLocalMatrix.getScale(restScale);

            Transform restPose = new Transform(restTranslation, restRotation, restScale);

            boneInfoList.add(new BoneInfo(ambBone.name, ambBone.name, parentId, restPose));
        }

        return new Skeleton(boneInfoList);
    }

    private static List<String> buildSupportedPropertyList(AmbModelInstance instance)
    {
        List<AmbBone> ambBones = instance.sourceModel.skeleton.bones;
        List<String> properties = new ArrayList<>(ambBones.size() * 10);

        for (AmbBone ambBone : ambBones)
        {
            String prefix = BONE_PREFIX + ambBone.name + ":";
            properties.add(prefix + PROPERTY_POSITION_X);
            properties.add(prefix + PROPERTY_POSITION_Y);
            properties.add(prefix + PROPERTY_POSITION_Z);
            properties.add(prefix + PROPERTY_ROTATION_X);
            properties.add(prefix + PROPERTY_ROTATION_Y);
            properties.add(prefix + PROPERTY_ROTATION_Z);
            properties.add(prefix + PROPERTY_SCALE_X);
            properties.add(prefix + PROPERTY_SCALE_Y);
            properties.add(prefix + PROPERTY_SCALE_Z);
            properties.add(prefix + PROPERTY_VISIBLE);
        }

        return Collections.unmodifiableList(properties);
    }
}
