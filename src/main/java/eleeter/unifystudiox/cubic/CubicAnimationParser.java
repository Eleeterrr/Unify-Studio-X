package eleeter.unifystudiox.cubic;

import java.util.Map;

import org.joml.Vector3f;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CubicAnimationParser
{


    public void parse(JsonElement animationsValue, CubicModel model, Map<String, CubicGroup> groupsById, Map<String, CubicGroup> groupsByName, CubicValueReader valueReader, String path)
    {
        if (animationsValue == null || animationsValue.isJsonNull())
        {
            return;
        }

        JsonArray animationArray = valueReader.array(animationsValue, path);


        if (animationArray != null)
        {
            for (int index = 0; index < animationArray.size(); index++)
            {
                JsonObject animationObject = valueReader.object(animationArray.get(index), path + "[" + index + "]");
                if (animationObject != null)
                {
                    model.animations.add(parseAnimation(animationObject, "animation_" + index, groupsById, groupsByName, valueReader, path + "[" + index + "]"));
                }
            }
            return;
        }

        JsonObject animationObjectMap = valueReader.object(animationsValue, path);
        if (animationObjectMap != null)
        {
            for (Map.Entry<String, JsonElement> entry : animationObjectMap.entrySet())
            {
                String animationPath = path + "." + entry.getKey();
                JsonObject animationObject = valueReader.object(entry.getValue(), animationPath);
                if (animationObject != null)
                {
                    model.animations.add(parseAnimation(animationObject, entry.getKey(), groupsById, groupsByName, valueReader, animationPath));
                }
            }
        }
    }

    private CubicAnimation parseAnimation(JsonObject animationObject, String fallbackName, Map<String, CubicGroup> groupsById, Map<String, CubicGroup> groupsByName, CubicValueReader valueReader, String path)
    {
        CubicParserUtils utils = new CubicParserUtils();
        String name = utils.firstString(valueReader, animationObject, path, "name");
        if (name == null || name.isBlank())
        {
            name = fallbackName;
        }

        float length = valueReader.floating(utils.firstExisting(animationObject, "length", "duration"), path + ".length", 0f);
        String loopMode = utils.firstString(valueReader, animationObject, path, "loop", "loopMode");
        if (loopMode == null || loopMode.isBlank())
        {
            loopMode = "once";
        }

        CubicAnimation animation = new CubicAnimation(name, length, loopMode);

        JsonObject bonesObject = valueReader.object(animationObject.get("bones"), path + ".bones");



        if (bonesObject != null)
        {
            for (Map.Entry<String, JsonElement> entry : bonesObject.entrySet())
            {
                JsonObject boneObject = valueReader.object(entry.getValue(), path + ".bones." + entry.getKey());
                if (boneObject == null) continue;

                String boneName = resolveBoneName(entry.getKey(), groupsById, groupsByName, valueReader, path + ".bones." + entry.getKey());
                CubicBoneAnimation boneAnimation = animation.bones.computeIfAbsent(boneName, CubicBoneAnimation::new);
                parseBoneChannels(boneObject, boneAnimation, groupsById, groupsByName, valueReader, path + ".bones." + entry.getKey());
            }
        }

        JsonObject animatorsObject = valueReader.object(animationObject.get("animators"), path + ".animators");
        if (animatorsObject != null)
        {
            for (Map.Entry<String, JsonElement> entry : animatorsObject.entrySet())
            {
                String animatorPath = path + ".animators." + entry.getKey();
                JsonObject animatorObject = valueReader.object(entry.getValue(), animatorPath);
                if (animatorObject == null) continue;

                String animatorName = utils.firstString(valueReader, animatorObject, animatorPath, "name");
                String boneName = resolveBoneName(animatorName != null ? animatorName : entry.getKey(), groupsById, groupsByName, valueReader, animatorPath);
                CubicBoneAnimation boneAnimation = animation.bones.computeIfAbsent(boneName, CubicBoneAnimation::new);

                JsonArray keyframes = valueReader.array(animatorObject.get("keyframes"), animatorPath + ".keyframes");
                if (keyframes != null)
                {
                    for (int index = 0; index < keyframes.size(); index++)
                    {
                        parseExplicitKeyframe(keyframes.get(index), boneAnimation, valueReader, animatorPath + ".keyframes[" + index + "]", null);
                    }
                } else
                {
                    parseBoneChannels(animatorObject, boneAnimation, groupsById, groupsByName, valueReader, animatorPath);
                }
            }
        }

        for (CubicBoneAnimation boneAnimation : animation.bones.values())
        {
            boneAnimation.sortKeyframes();
        }

        return animation;
    }

    private void parseBoneChannels(JsonObject boneObject, CubicBoneAnimation boneAnimation, Map<String, CubicGroup> groupsById, Map<String, CubicGroup> groupsByName, CubicValueReader valueReader, String path)
    {
        parseChannelCollection(boneObject.get("position"), "position", boneAnimation, valueReader, path + ".position");
        parseChannelCollection(boneObject.get("rotation"), "rotation", boneAnimation, valueReader, path + ".rotation");
        parseChannelCollection(boneObject.get("scale"), "scale", boneAnimation, valueReader, path + ".scale");

        JsonArray keyframes = valueReader.array(boneObject.get("keyframes"), path + ".keyframes");
        if (keyframes != null)
        {
            for (int index = 0; index < keyframes.size(); index++)
            {
                parseExplicitKeyframe(keyframes.get(index), boneAnimation, valueReader, path + ".keyframes[" + index + "]", null);
            }
        }
    }

    private void parseChannelCollection(JsonElement channelValue, String channel, CubicBoneAnimation boneAnimation, CubicValueReader valueReader, String path)
    {
        if (channelValue == null || channelValue.isJsonNull()) return;

        JsonArray channelArray = valueReader.array(channelValue, path);
        if (channelArray != null)
        {
            for (int index = 0; index < channelArray.size(); index++)
            {
                parseExplicitKeyframe(channelArray.get(index), boneAnimation, valueReader, path + "[" + index + "]", channel);
            }
            return;
        }

        JsonObject channelObject = valueReader.object(channelValue, path);
        if (channelObject != null)
        {
            if (channelObject.has("time") || channelObject.has("data_points"))
            {
                parseExplicitKeyframe(channelObject, boneAnimation, valueReader, path, channel);
                return;
            }
            for (Map.Entry<String, JsonElement> entry : channelObject.entrySet())
            {
                try
                {
                    float time = Float.parseFloat(entry.getKey());
                    parseExplicitKeyframe(entry.getValue(), boneAnimation, valueReader, path + "." + entry.getKey(), channel, time);
                } catch (NumberFormatException ignored) {}
            }
            return;
        }

        parseExplicitKeyframe(channelValue, boneAnimation, valueReader, path, channel);
    }

    private void parseExplicitKeyframe(JsonElement keyframeValue, CubicBoneAnimation boneAnimation, CubicValueReader valueReader, String path, String defaultChannel)
    {
        parseExplicitKeyframe(keyframeValue, boneAnimation, valueReader, path, defaultChannel, 0f);
    }

    private void parseExplicitKeyframe(JsonElement keyframeValue, CubicBoneAnimation boneAnimation, CubicValueReader valueReader, String path, String defaultChannel, float fallbackTime)
    {
        CubicParserUtils utils = new CubicParserUtils();
        JsonObject keyframeObject = valueReader.object(keyframeValue, path);
        String channel = defaultChannel;
        float time = fallbackTime;
        Vector3f value = new Vector3f(0f, 0f, 0f);
        String interpolation = "linear";

        if (keyframeObject != null)
        {
            String explicitChannel = utils.firstString(valueReader, keyframeObject, path, "channel");
            if (explicitChannel != null && !explicitChannel.isBlank()) channel = explicitChannel;

            time = valueReader.floating(keyframeObject.get("time"), path + ".time", fallbackTime);
            interpolation = utils.firstString(valueReader, keyframeObject, path, "interpolation");
            if (interpolation == null || interpolation.isBlank()) interpolation = "linear";

            JsonElement dataPoints = keyframeObject.get("data_points");
            if (dataPoints != null && dataPoints.isJsonArray() && dataPoints.getAsJsonArray().size() > 0)
            {
                value = valueReader.vector3(dataPoints.getAsJsonArray().get(0), path + ".data_points[0]", value);
            } else if (dataPoints != null && dataPoints.isJsonObject())
            {
                value = valueReader.vector3(dataPoints, path + ".data_points", value);
            } else
            {
                value = valueReader.vector3(keyframeObject, path, value);
            }
        } else
        {
            value = valueReader.vector3(keyframeValue, path, value);
        }

        if (channel == null || channel.isBlank()) return;

        CubicKeyframe keyframe = new CubicKeyframe(channel, time, value, interpolation);
        switch (channel)
        {
            case "position" -> boneAnimation.positionKeyframes.add(keyframe);
            case "rotation" -> boneAnimation.rotationKeyframes.add(keyframe);
            case "scale" -> boneAnimation.scaleKeyframes.add(keyframe);
        }
    }

    private String resolveBoneName(String reference, Map<String, CubicGroup> groupsById, Map<String, CubicGroup> groupsByName, CubicValueReader valueReader, String path)
    {
        if (reference == null || reference.isBlank()) return "unnamed_bone";
        CubicGroup groupById = groupsById.get(reference);
        if (groupById != null) return groupById.name;
        CubicGroup groupByName = groupsByName.get(reference);
        if (groupByName != null) return groupByName.name;
        return reference;
    }
}
