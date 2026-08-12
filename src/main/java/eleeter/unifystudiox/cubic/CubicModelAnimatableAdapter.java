package eleeter.unifystudiox.cubic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import eleeter.unifystudiox.animation.api.AnimatableObject;
import eleeter.unifystudiox.animation.api.SkeletonProvider;
import eleeter.unifystudiox.animation.data.BoneInfo;
import eleeter.unifystudiox.animation.data.Skeleton;
import eleeter.unifystudiox.animation.data.Transform;
import eleeter.unifystudiox.cubic.render.CubeRuntimeModel;
import eleeter.unifystudiox.cubic.render.CubeRuntimeNode;

public class CubicModelAnimatableAdapter implements AnimatableObject, SkeletonProvider
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
    private static final int BONE_PART_COUNT = 3;

    private final CubicModelInstance instance;
    private final CubeRuntimeModel runtimeModel;
    private final Skeleton skeleton;
    private final List<String> supportedProperties;

    private final Map<String, Vector3f> nodeEulerCache = new HashMap<>();

    public CubicModelAnimatableAdapter(CubicModelInstance instance)
    {
        if (instance == null)
        {
            throw new IllegalArgumentException("CubicModelAnimatableAdapter: instance must not be null.");
        }

        this.instance = instance;
        this.runtimeModel = instance.getModel();
        this.skeleton = buildSkeletonSnapshot(this.runtimeModel);
        this.supportedProperties = buildSupportedPropertyList(this.runtimeModel);

        initEulerCache(this.runtimeModel);
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
                    "CubicModelAnimatableAdapter.getProperty: propertyName must not be null or empty.");
        }

        if (propertyName.startsWith(BONE_PREFIX))
        {
            return readNodeProperty(propertyName);
        }

        throw new IllegalArgumentException("CubicModelAnimatableAdapter.getProperty: unknown property '" + propertyName + "'.");
    }

    @Override
    public void setProperty(String propertyName, Object value)
    {
        if (propertyName == null || propertyName.isEmpty())
        {
            throw new IllegalArgumentException("CubicModelAnimatableAdapter.setProperty: propertyName must not be null or empty.");
        }
        if (value == null)
        {
            throw new IllegalArgumentException("CubicModelAnimatableAdapter.setProperty: value must not be null. property: '" + propertyName + "'.");
        }

        if (propertyName.startsWith(BONE_PREFIX))
        {
            writeNodeProperty(propertyName, value);
            return;
        }

        throw new IllegalArgumentException("CubicModelAnimatableAdapter.setProperty: unknown property '" + propertyName + "'.");
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

    private Object readNodeProperty(String propertyName)
    {
        String[] parts = parseBonePropertyParts(propertyName);
        String nodeId = parts[1];
        String component = parts[2];

        CubeRuntimeNode node = findNodeById(nodeId, propertyName);

        switch (component)
        {
            case PROPERTY_POSITION_X:
                return node.translation.x;
            case PROPERTY_POSITION_Y:
                return node.translation.y;
            case PROPERTY_POSITION_Z:
                return node.translation.z;
            case PROPERTY_SCALE_X:
                return node.scale.x;
            case PROPERTY_SCALE_Y:
                return node.scale.y;
            case PROPERTY_SCALE_Z:
                return node.scale.z;
            case PROPERTY_VISIBLE:
                return true;
            case PROPERTY_ROTATION_X:
            {
                Vector3f euler = getOrCreateEulerCache(nodeId, node);
                return euler.x;
            }
            case PROPERTY_ROTATION_Y:
            {
                Vector3f euler = getOrCreateEulerCache(nodeId, node);
                return euler.y;
            }
            case PROPERTY_ROTATION_Z:
            {
                Vector3f euler = getOrCreateEulerCache(nodeId, node);
                return euler.z;
            }
            default:
                throw new IllegalArgumentException("CubicModelAnimatableAdapter: unknown component '" + component + "' in property '" + propertyName + "'." + " Valid: position.x/y/z, rotation.x/y/z, scale.x/y/z, visible.");
        }
    }

    private void writeNodeProperty(String propertyName, Object value)
    {
        String[] parts = parseBonePropertyParts(propertyName);
        String nodeId = parts[1];
        String component = parts[2];

        CubeRuntimeNode node = findNodeById(nodeId, propertyName);

        float floatValue;
        switch (component)
        {
            case PROPERTY_POSITION_X:
                floatValue = requireFloat(value, propertyName);
                node.translation.x = floatValue;
                node.isManuallyControlled = true;
                break;

            case PROPERTY_POSITION_Y:
                floatValue = requireFloat(value, propertyName);
                node.translation.y = floatValue;
                node.isManuallyControlled = true;
                break;

            case PROPERTY_POSITION_Z:
                floatValue = requireFloat(value, propertyName);
                node.translation.z = floatValue;
                node.isManuallyControlled = true;
                break;

            case PROPERTY_ROTATION_X:
            {
                floatValue = requireFloat(value, propertyName);
                Vector3f euler = getOrCreateEulerCache(nodeId, node);
                euler.x = floatValue;
                node.rotation.identity().rotateXYZ(euler.x, euler.y, euler.z);
                node.isManuallyControlled = true;
                break;
            }
            case PROPERTY_ROTATION_Y:
            {
                floatValue = requireFloat(value, propertyName);
                Vector3f euler = getOrCreateEulerCache(nodeId, node);
                euler.y = floatValue;
                node.rotation.identity().rotateXYZ(euler.x, euler.y, euler.z);
                node.isManuallyControlled = true;
                break;
            }
            case PROPERTY_ROTATION_Z:
            {
                floatValue = requireFloat(value, propertyName);
                Vector3f euler = getOrCreateEulerCache(nodeId, node);
                euler.z = floatValue;
                node.rotation.identity().rotateXYZ(euler.x, euler.y, euler.z);
                node.isManuallyControlled = true;
                break;
            }
            case PROPERTY_SCALE_X:
                floatValue = requireFloat(value, propertyName);
                node.scale.x = floatValue;
                node.isManuallyControlled = true;
                break;

            case PROPERTY_SCALE_Y:
                floatValue = requireFloat(value, propertyName);
                node.scale.y = floatValue;
                node.isManuallyControlled = true;
                break;

            case PROPERTY_SCALE_Z:
                floatValue = requireFloat(value, propertyName);
                node.scale.z = floatValue;
                node.isManuallyControlled = true;
                break;

            case PROPERTY_VISIBLE:
                break;

            default:
                throw new IllegalArgumentException(
                        "CubicModelAnimatableAdapter: unknown component '" + component
                                + "' in property '" + propertyName + "'."
                                + " Valid: position.x/y/z, rotation.x/y/z, scale.x/y/z, visible.");
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
                    "CubicModelAnimatableAdapter: node property '" + propertyName
                            + "' is malformed. Expected format: 'bone:NODE_ID:COMPONENT'.");
        }

        String nodeId = propertyName.substring(firstColon + 1, lastColon);
        String component = propertyName.substring(lastColon + 1);

        return new String[]{"bone", nodeId, component};
    }

    private CubeRuntimeNode findNodeById(String nodeId, String originalPropertyName)
    {
        CubeRuntimeNode node = this.runtimeModel.nodesById.get(nodeId);
        if (node != null)
        {
            return node;
        }

        throw new IllegalArgumentException(
                "CubicModelAnimatableAdapter: no node with name '" + nodeId
                        + "' found in model '" + this.instance.getId()
                        + "'. Property attempted: '" + originalPropertyName + "'.");
    }

    private float requireFloat(Object value, String propertyName)
    {
        if (!(value instanceof Float))
        {
            throw new IllegalArgumentException(
                    "CubicModelAnimatableAdapter.setProperty: expected Float for property '"
                            + propertyName + "' but got " + value.getClass().getSimpleName() + ".");
        }
        return (Float) value;
    }

    private Vector3f getOrCreateEulerCache(String nodeName, CubeRuntimeNode node)
    {
        return this.nodeEulerCache.computeIfAbsent(nodeName, key ->
        {
            Vector3f euler = new Vector3f();
            node.rotation.getEulerAnglesXYZ(euler);
            return euler;
        });
    }

    private void initEulerCache(CubeRuntimeModel model)
    {
        for (CubeRuntimeNode node : model.flattenedNodes)
        {
            if (node != null)
            {
                Vector3f euler = new Vector3f();
                node.rotation.getEulerAnglesXYZ(euler);
                this.nodeEulerCache.put(node.id, euler);
            }
        }
    }

    private static Skeleton buildSkeletonSnapshot(CubeRuntimeModel model)
    {
        List<BoneInfo> boneInfoList = new ArrayList<>();

        for (CubeRuntimeNode node : model.flattenedNodes)
        {
            if (node == null)
            {
                continue;
            }

            String parentId = (node.parent != null) ? node.parent.id : null;

            Vector3f restTranslation = new Vector3f(node.translation);
            Quaternionf restRotation = new Quaternionf(node.rotation);
            Vector3f restScale = new Vector3f(node.scale);

            Transform restPose = new Transform(restTranslation, restRotation, restScale);

            boneInfoList.add(new BoneInfo(node.id, node.name, parentId, restPose));
        }

        return new Skeleton(boneInfoList);
    }

    private static List<String> buildSupportedPropertyList(CubeRuntimeModel model)
    {
        List<String> properties = new ArrayList<>();

        for (CubeRuntimeNode node : model.flattenedNodes)
        {
            if (node == null)
            {
                continue;
            }

            String prefix = BONE_PREFIX + node.name + ":";
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
