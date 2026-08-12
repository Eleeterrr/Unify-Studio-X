package eleeter.unifystudiox.cubic;

import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector4f;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class CubicValueReader
{
    private final String sourceName;
    private final List<String> warnings;

    CubicValueReader(String sourceName, List<String> warnings)
    {
        this.sourceName = sourceName;
        this.warnings = warnings;
    }

    void warn(String path, String message)
    {
        this.warnings.add("[" + this.sourceName + "] " + path + ": " + message);
    }

    JsonObject object(JsonElement element, String path)
    {
        if (element == null || element.isJsonNull())
        {
            return null;
        }
        if (!element.isJsonObject())
        {
            warn(path, "Expected object but found " + describe(element));
            return null;
        }
        return element.getAsJsonObject();
    }

    JsonArray array(JsonElement element, String path)
    {
        if (element == null || element.isJsonNull())
        {
            return null;
        }
        if (!element.isJsonArray())
        {
            warn(path, "Expected array but found " + describe(element));
            return null;
        }
        return element.getAsJsonArray();
    }

    String string(JsonObject object, String key, String path, String defaultValue)
    {
        if (object == null || !object.has(key))
        {
            return defaultValue;
        }
        JsonElement element = object.get(key);
        if (element == null || element.isJsonNull())
        {
            return defaultValue;
        }
        if (element.isJsonPrimitive())
        {
            try
            {
                return element.getAsString();
            } catch (RuntimeException exception)
            {
                warn(path + "." + key, "Unable to read string value");
            }
        } else
        {
            warn(path + "." + key, "Expected primitive string value");
        }
        return defaultValue;
    }

    float floating(JsonElement element, String path, float defaultValue)
    {
        if (element == null || element.isJsonNull())
        {
            return defaultValue;
        }
        try
        {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber())
            {
                return element.getAsFloat();
            }
            if (element.isJsonPrimitive())
            {
                String raw = element.getAsString();
                String sanitized = raw == null ? "" : raw.trim().replaceAll("[\\s\\p{Cntrl}]+", "");
                if (sanitized.isEmpty())
                {
                    warn(path, "Empty numeric string, using default " + defaultValue);
                    return defaultValue;
                }
                if (sanitized.startsWith("#"))
                {
                    sanitized = sanitized.substring(1);
                }
                return Float.parseFloat(sanitized);
            }
        } catch (RuntimeException exception)
        {
            warn(path, "Invalid numeric value '" + element + "', using default " + defaultValue);
            return defaultValue;
        }

        warn(path, "Expected numeric value but found " + describe(element));
        return defaultValue;
    }

    int integer(JsonElement element, String path, int defaultValue)
    {
        return Math.round(floating(element, path, defaultValue));
    }

    Vector3f vector3(JsonElement element, String path, Vector3f defaultValue)
    {
        Vector3f fallback = new Vector3f(defaultValue);
        if (element == null || element.isJsonNull())
        {
            return fallback;
        }

        JsonArray array = array(element, path);
        if (array != null)
        {
            return new Vector3f(
                    floating(array.size() > 0 ? array.get(0) : null, path + "[0]", fallback.x),
                    floating(array.size() > 1 ? array.get(1) : null, path + "[1]", fallback.y),
                    floating(array.size() > 2 ? array.get(2) : null, path + "[2]", fallback.z));
        }

        JsonObject object = object(element, path);
        if (object != null)
        {
            return new Vector3f(
                    floating(object.get("x"), path + ".x", fallback.x),
                    floating(object.get("y"), path + ".y", fallback.y),
                    floating(object.get("z"), path + ".z", fallback.z));
        }

        return fallback;
    }

    Vector4f uv(JsonElement element, String path, Vector4f defaultValue)
    {
        Vector4f fallback = new Vector4f(defaultValue);
        JsonArray array = array(element, path);
        if (array == null)
        {
            return fallback;
        }

        return new Vector4f(
                floating(array.size() > 0 ? array.get(0) : null, path + "[0]", fallback.x),
                floating(array.size() > 1 ? array.get(1) : null, path + "[1]", fallback.y),
                floating(array.size() > 2 ? array.get(2) : null, path + "[2]", fallback.z),
                floating(array.size() > 3 ? array.get(3) : null, path + "[3]", fallback.w));
    }

    String describe(JsonElement element)
    {
        if (element == null || element.isJsonNull())
        {
            return "null";
        }
        if (element.isJsonObject())
        {
            return "object";
        }
        if (element.isJsonArray())
        {
            return "array";
        }
        return "primitive";
    }
}
