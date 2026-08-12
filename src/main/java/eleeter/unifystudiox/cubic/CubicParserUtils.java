package eleeter.unifystudiox.cubic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CubicParserUtils
{

    public JsonElement firstExisting(JsonObject object, String... keys)
    {
        if (object == null) return null;
        for (String key : keys)
        {
            if (object.has(key)) return object.get(key);
        }
        return null;
    }

    public String firstString(CubicValueReader valueReader, JsonObject object, String path, String... keys)
    {
        for (String key : keys)
        {
            String value = valueReader.string(object, key, path, null);
            if (value != null) return value;
        }
        return null;
    }
}
