package eleeter.unifystudiox.gltf.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JsonParser
{
    private final String input;
    private int pos = 0;
    private final int length;

    private JsonParser(String input)
    {
        this.input = input;
        this.length = input.length();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parse(String json)
    {
        Object result = new JsonParser(json).parseValue();
        if (result instanceof Map)
        {
            return (Map<String, Object>) result;
        }
        throw new RuntimeException("[GltfToAmb] Expected JSON root to be an object.");
    }

    private Object parseValue()
    {
        skipWhitespace();
        if (pos >= length) return null;

        char c = input.charAt(pos);
        if (c == '{') return parseObject();
        if (c == '[') return parseArray();
        if (c == '"') return parseString();
        if (c == 't' || c == 'f') return parseBoolean();
        if (c == 'n') return parseNull();
        if (c == '-' || (c >= '0' && c <= '9')) return parseNumber();

        throw new RuntimeException("[GltfToAmb] JSON parse error: unexpected char '" + c + "' at " + pos);
    }

    private Map<String, Object> parseObject()
    {
        Map<String, Object> map = new HashMap<>();
        pos++; // Skip '{'
        skipWhitespace();
        if (pos < length && input.charAt(pos) == '}')
        {
            pos++; // Empty object
            return map;
        }

        while (pos < length)
        {
            skipWhitespace();
            if (input.charAt(pos) != '"')
            {
                throw new RuntimeException("[GltfToAmb] JSON parse error: expected string key at " + pos);
            }
            String key = parseString();
            skipWhitespace();
            if (input.charAt(pos) != ':')
            {
                throw new RuntimeException("[GltfToAmb] JSON parse error: expected ':' after key at " + pos);
            }
            pos++; // Skip ':'
            Object value = parseValue();
            map.put(key, value);

            skipWhitespace();
            if (pos >= length) break;
            char c = input.charAt(pos);
            if (c == '}')
            {
                pos++;
                break;
            } else if (c == ',')
            {
                pos++;
            } else
            {
                throw new RuntimeException("[GltfToAmb] JSON parse error: expected ',' or '}' at " + pos);
            }
        }
        return map;
    }

    private List<Object> parseArray()
    {
        List<Object> list = new ArrayList<>();
        pos++; // Skip '['
        skipWhitespace();
        if (pos < length && input.charAt(pos) == ']')
        {
            pos++;
            return list;
        }

        while (pos < length)
        {
            list.add(parseValue());
            skipWhitespace();
            if (pos >= length) break;
            char c = input.charAt(pos);
            if (c == ']')
            {
                pos++;
                break;
            } else if (c == ',')
            {
                pos++;
            } else
            {
                throw new RuntimeException("[GltfToAmb] JSON parse error: expected ',' or ']' at " + pos);
            }
        }
        return list;
    }

    private String parseString()
    {
        pos++;
        StringBuilder sb = new StringBuilder();
        while (pos < length)
        {
            char c = input.charAt(pos++);
            if (c == '"')
            {
                return sb.toString();
            } else if (c == '\\')
            {
                if (pos >= length) break;
                char escape = input.charAt(pos++);
                switch (escape)
                {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        if (pos + 4 <= length)
                        {
                            String hex = input.substring(pos, pos + 4);
                            sb.append((char) Integer.parseInt(hex, 16));
                            pos += 4;
                        }
                        break;
                    default: sb.append(escape); break;
                }
            } else
            {
                sb.append(c);
            }
        }
        throw new RuntimeException("[GltfToAmb] JSON parse error: unterminated string");
    }

    private Object parseNumber()
    {
        int start = pos;
        while (pos < length)
        {
            char c = input.charAt(pos);
            if ((c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E')
            {
                pos++;
            } else
            {
                break;
            }
        }
        String str = input.substring(start, pos);
        if (str.contains(".") || str.contains("e") || str.contains("E"))
        {
            return Double.parseDouble(str);
        } else
        {
            return Double.parseDouble(str);
        }
    }

    private Boolean parseBoolean()
    {
        if (input.startsWith("true", pos))
        {
            pos += 4;
            return true;
        } else if (input.startsWith("false", pos))
        {
            pos += 5;
            return false;
        }
        throw new RuntimeException("[GltfToAmb] JSON parse error: expected boolean at " + pos);
    }

    private Object parseNull()
    {
        if (input.startsWith("null", pos))
        {
            pos += 4;
            return null;
        }
        throw new RuntimeException("[GltfToAmb] JSON parse error: expected null at " + pos);
    }

    private void skipWhitespace()
    {
        while (pos < length)
        {
            char c = input.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r')
            {
                pos++;
            } else
            {
                break;
            }
        }
    }
}
