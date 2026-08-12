package eleeter.unifystudiox.util.json;

import java.util.List;
import java.util.Map;

public class JsonBuilder
{
    private static final String INDENT = "    ";

    /**
     * Converts the given object into a formatted JSON string.
     */
    public static String toJson(Object value, int depth)
    {
        if (value == null)
        {
            return "null";
        }

        if (value instanceof Map<?, ?> map)
        {
            return mapToJson(map, depth);
        }

        if (value instanceof List<?> list)
        {
            return listToJson(list, depth);
        }

        if (value instanceof float[] arr)
        {
            return floatArrayToJson(arr);
        }

        if (value instanceof String s)
        {
            return "\"" + escapeString(s) + "\"";
        }

        if (value instanceof Boolean || value instanceof Integer || value instanceof Long)
        {
            return value.toString();
        }

        if (value instanceof Float f)
        {
            return Float.toString(f);
        }

        if (value instanceof Double d)
        {
            return Double.toString(d);
        }

        if (value instanceof Number n)
        {
            return n.toString();
        }

        return "\"" + escapeString(value.toString()) + "\"";
    }

    private static String mapToJson(Map<?, ?> map, int depth)
    {
        if (map.isEmpty())
        {
            return "{}";
        }

        String indent = indent(depth + 1);
        String closingIndent = indent(depth);
        StringBuilder sb = new StringBuilder("{\n");
        int i = 0;
        int size = map.size();

        for (Map.Entry<?, ?> entry : map.entrySet())
        {
            sb.append(indent);
            sb.append("\"").append(escapeString(entry.getKey().toString())).append("\": ");
            sb.append(toJson(entry.getValue(), depth + 1));
            if (i < size - 1)
            {
                sb.append(",");
            }
            sb.append("\n");
            i++;
        }

        sb.append(closingIndent).append("}");
        return sb.toString();
    }

    private static String listToJson(List<?> list, int depth)
    {
        if (list.isEmpty())
        {
            return "[]";
        }

        boolean allNumeric = list.stream().allMatch(v -> v instanceof Number);
        if (allNumeric)
        {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++)
            {
                sb.append(toJson(list.get(i), depth));
                if (i < list.size() - 1) sb.append(", ");
            }
            sb.append("]");
            return sb.toString();
        }

        String indent = indent(depth + 1);
        String closingIndent = indent(depth);
        StringBuilder sb = new StringBuilder("[\n");

        for (int i = 0; i < list.size(); i++)
        {
            sb.append(indent).append(toJson(list.get(i), depth + 1));
            if (i < list.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append(closingIndent).append("]");
        return sb.toString();
    }

    private static String floatArrayToJson(float[] arr)
    {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++)
        {
            sb.append(arr[i]);
            if (i < arr.length - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String indent(int depth)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++)
        {
            sb.append(INDENT);
        }
        return sb.toString();
    }

    private static String escapeString(String s)
    {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
