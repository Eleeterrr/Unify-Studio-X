package eleeter.unifystudiox.cubic;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joml.Vector3f;
import org.joml.Vector4f;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CubicGeometryParser
{

    private final CubicParserUtils utils = new CubicParserUtils();

    public void parseElements(JsonElement elementValue, CubicModel model, Map<String, CubeElement> elementsById, List<CubeElement> elementsByIndex, CubicValueReader valueReader)
    {
        JsonArray elements = valueReader.array(elementValue, "root.elements");
        if (elements == null)
        {
            return;
        }

        for (int index = 0; index < elements.size(); index++)
        {
            String path = "root.elements[" + index + "]";
            JsonObject elementObject = valueReader.object(elements.get(index), path);
            if (elementObject == null) continue;

            String id = utils.firstString(valueReader, elementObject, path, "uuid", "id");
            if (id == null || id.isBlank()) id = "element:" + index;

            String name = utils.firstString(valueReader, elementObject, path, "name");
            if (name == null || name.isBlank()) name = id;

            String type = utils.firstString(valueReader, elementObject, path, "type");
            if (type == null || type.isBlank()) type = "cube";

            CubeElement parsedElement;
            if ("cube".equalsIgnoreCase(type))
            {
                parsedElement = parseCubeElement(elementObject, id, name, valueReader, path);
            } else
            {
                parsedElement = new CubicUnknownElement(id, name, type);
            }

            model.elements.add(parsedElement);
            elementsByIndex.add(parsedElement);
            elementsById.putIfAbsent(id, parsedElement);
        }
    }

    private CubicElement parseCubeElement(JsonObject elementObject, String id, String name, CubicValueReader valueReader, String path)
    {
        Vector3f from = valueReader.vector3(elementObject.get("from"), path + ".from", new Vector3f(0f, 0f, 0f));
        Vector3f to = valueReader.vector3(elementObject.get("to"), path + ".to", new Vector3f(from));
        Vector3f origin = valueReader.vector3(utils.firstExisting(elementObject, "origin", "pivot"), path + ".origin", new Vector3f(0f, 0f, 0f));

        Map<String, CubicFace> faces = new LinkedHashMap<>();
        JsonObject facesObject = valueReader.object(elementObject.get("faces"), path + ".faces");
        if (facesObject != null)
        {
            for (Map.Entry<String, JsonElement> entry : facesObject.entrySet())
            {
                String direction = entry.getKey();
                String facePath = path + ".faces." + direction;
                JsonObject faceObject = valueReader.object(entry.getValue(), facePath);
                if (faceObject == null) continue;

                Vector4f uv = valueReader.uv(faceObject.get("uv"), facePath + ".uv", new Vector4f(0f, 0f, 0f, 0f));
                int textureIndex = valueReader.integer(utils.firstExisting(faceObject, "texture", "texture_index"), facePath + ".texture", 0);
                int rotation = valueReader.integer(faceObject.get("rotation"), facePath + ".rotation", 0);
                faces.put(direction, new CubicFace(direction, uv, textureIndex, rotation));
            }
        }

        Vector3f rotation = valueReader.vector3(elementObject.get("rotation"), path + ".rotation", new Vector3f(0f, 0f, 0f));
        return new CubicElement(id, name, from, to, origin, rotation, faces);
    }

    public void parseOutliner(JsonElement outlinerValue, CubicGroup root, Map<String, CubeElement> elementsById, List<CubeElement> elementsByIndex, Set<CubeElement> assignedElements, Map<String, CubicGroup> groupsById, Map<String, CubicGroup> groupsByName, CubicValueReader valueReader, String path, int[] boneCounter)
    {
        JsonArray outliner = valueReader.array(outlinerValue, path);
        if (outliner == null) return;

        for (int index = 0; index < outliner.size(); index++)
        {
            parseOutlinerNode(outliner.get(index), root, elementsById, elementsByIndex, assignedElements, groupsById, groupsByName, valueReader, path + "[" + index + "]", boneCounter);
        }
    }

    private void parseOutlinerNode(JsonElement nodeValue, CubicGroup parent, Map<String, CubeElement> elementsById, List<CubeElement> elementsByIndex, Set<CubeElement> assignedElements, Map<String, CubicGroup> groupsById, Map<String, CubicGroup> groupsByName, CubicValueReader valueReader, String path, int[] boneCounter)
    {
        if (nodeValue == null || nodeValue.isJsonNull()) return;

        if (nodeValue.isJsonPrimitive())
        {
            attachElementReference(nodeValue, parent, elementsById, elementsByIndex, assignedElements, valueReader, path);
            return;
        }

        JsonObject nodeObject = valueReader.object(nodeValue, path);
        if (nodeObject == null) return;

        if (nodeObject.has("element"))
        {
            attachElementReference(nodeObject.get("element"), parent, elementsById, elementsByIndex, assignedElements, valueReader, path + ".element");
            return;
        }

        String id = utils.firstString(valueReader, nodeObject, path, "uuid", "id");
        if (id == null || id.isBlank()) id = "group:" + groupsById.size();

        String name = utils.firstString(valueReader, nodeObject, path, "name");
        if (name == null || name.isBlank()) name = id;

        Vector3f pivot = valueReader.vector3(utils.firstExisting(nodeObject, "origin", "pivot"), path + ".origin", new Vector3f(0f, 0f, 0f));
        Vector3f rotation = valueReader.vector3(nodeObject.get("rotation"), path + ".rotation", new Vector3f(0f, 0f, 0f));
        CubicGroup group = new CubicGroup(id, name, pivot, rotation);
        group.boneIndex = boneCounter[0]++;
        parent.addChild(group);
        groupsById.putIfAbsent(id, group);
        groupsByName.putIfAbsent(name, group);

        JsonArray children = valueReader.array(nodeObject.get("children"), path + ".children");
        if (children != null)
        {
            for (int index = 0; index < children.size(); index++)
            {
                parseOutlinerNode(children.get(index), group, elementsById, elementsByIndex, assignedElements, groupsById, groupsByName, valueReader, path + ".children[" + index + "]", boneCounter);
            }
        }
    }

    private void attachElementReference(JsonElement referenceValue, CubicGroup parent, Map<String, CubeElement> elementsById, List<CubeElement> elementsByIndex, Set<CubeElement> assignedElements, CubicValueReader valueReader, String path)
    {
        CubeElement resolved = resolveElement(referenceValue, elementsById, elementsByIndex);
        if (resolved != null && !assignedElements.contains(resolved))
        {
            parent.addElement(resolved);
            assignedElements.add(resolved);
        }
    }

    private CubeElement resolveElement(JsonElement referenceValue, Map<String, CubeElement> elementsById, List<CubeElement> elementsByIndex)
    {
        if (referenceValue == null || referenceValue.isJsonNull() || !referenceValue.isJsonPrimitive()) return null;

        if (referenceValue.getAsJsonPrimitive().isNumber())
        {
            int index = Math.round(referenceValue.getAsFloat());
            return index >= 0 && index < elementsByIndex.size() ? elementsByIndex.get(index) : null;
        }

        String key = referenceValue.getAsString();
        CubeElement byId = elementsById.get(key);
        if (byId != null) return byId;

        if (key.startsWith("element:"))
        {
            try
            {
                int index = Integer.parseInt(key.substring("element:".length()));
                return index >= 0 && index < elementsByIndex.size() ? elementsByIndex.get(index) : null;
            } catch (NumberFormatException ignored)
            {
            }
        }
        return null;
    }

    public int readTextureDimension(CubicValueReader valueReader, JsonObject rootObject, String path, boolean width)
    {
        String directKey = width ? "texture_width" : "texture_height";
        String camelKey = width ? "textureWidth" : "textureHeight";
        JsonElement directValue = utils.firstExisting(rootObject, directKey, camelKey);
        if (directValue != null) return valueReader.integer(directValue, path + "." + directKey, 0);

        JsonObject texturesObject = valueReader.object(rootObject.get("textures"), path + ".textures");
        if (texturesObject != null)
        {
            String nestedKey = width ? "width" : "height";
            return valueReader.integer(utils.firstExisting(texturesObject, nestedKey, directKey, camelKey), path + ".textures." + nestedKey, 0);
        }

        JsonArray texturesArray = valueReader.array(rootObject.get("textures"), path + ".textures");
        if (texturesArray != null && texturesArray.size() > 0)
        {
            JsonObject firstTexture = valueReader.object(texturesArray.get(0), path + ".textures[0]");
            if (firstTexture != null)
            {
                String nestedKey = width ? "width" : "height";
                return valueReader.integer(utils.firstExisting(firstTexture, nestedKey, directKey, camelKey), path + ".textures[0]." + nestedKey, 0);
            }
        }
        return 0;
    }
}
