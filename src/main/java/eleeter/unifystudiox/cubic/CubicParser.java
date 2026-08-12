package eleeter.unifystudiox.cubic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joml.Vector3f;

public class CubicParser
{

    private final CubicGeometryParser geometryParser = new CubicGeometryParser();
    private final CubicAnimationParser animationParser = new CubicAnimationParser();
    private final CubicParserUtils utils = new CubicParserUtils();

    public CubicModel parse(String path)
    {
        try (Reader reader = Files.newBufferedReader(Path.of(path)))
        {
            return parse(reader, path);
        } catch (IOException exception)
        {
            throw new RuntimeException("[LwgjbParser] Failed to read file: " + path, exception);
        }
    }

    public CubicModel parse(Reader reader, String sourceName)
    {
        JsonElement rootElement = JsonParser.parseReader(reader);
        if (rootElement == null || !rootElement.isJsonObject())
        {
            throw new RuntimeException("[LwgjbParser] Root JSON value must be an object for: " + sourceName);
        }

        JsonObject rootObject = rootElement.getAsJsonObject();
        List<String> warnings = new ArrayList<>();
        CubicValueReader valueReader = new CubicValueReader(sourceName, warnings);

        String formatVersion = this.utils.firstString(valueReader, rootObject, "root", "format_version", "format", "version");
        if (formatVersion == null) formatVersion = "unknown";

        int textureWidth = this.geometryParser.readTextureDimension(valueReader, rootObject, "root", true);
        int textureHeight = this.geometryParser.readTextureDimension(valueReader, rootObject, "root", false);

        CubicGroup root = new CubicGroup("__root__", "root", new Vector3f(0f, 0f, 0f));
        root.boneIndex = 0;
        CubicModel model = new CubicModel(sourceName, formatVersion, textureWidth, textureHeight, root);

        Map<String, CubeElement> elementsById = new LinkedHashMap<>();
        List<CubeElement> elementsByIndex = new ArrayList<>();
        Set<CubeElement> assignedElements = new LinkedHashSet<>();
        Map<String, CubicGroup> groupsById = new LinkedHashMap<>();
        Map<String, CubicGroup> groupsByName = new LinkedHashMap<>();
        groupsById.put(root.id, root);
        groupsByName.put(root.name, root);

        this.geometryParser.parseElements(rootObject.get("elements"), model, elementsById, elementsByIndex, valueReader);

        int[] boneCounter = {1};
        this.geometryParser.parseOutliner(rootObject.get("outliner"), root, elementsById, elementsByIndex, assignedElements, groupsById, groupsByName, valueReader, "root.outliner", boneCounter);

        attachUnassignedElements(root, model.elements, assignedElements);
        this.animationParser.parse(rootObject.get("animations"), model, groupsById, groupsByName, valueReader, "root.animations");

        model.warnings.addAll(warnings);
        return model;
    }

    private void attachUnassignedElements(CubicGroup root, List<CubeElement> elements, Set<CubeElement> assignedElements)
    {
        for (CubeElement element : elements)
        {
            if (!assignedElements.contains(element))
            {
                root.addElement(element);
            }
        }
    }
}
