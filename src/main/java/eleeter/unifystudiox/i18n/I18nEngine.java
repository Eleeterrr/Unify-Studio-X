package eleeter.unifystudiox.i18n;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class I18nEngine
{
    private static final Map<String, String> translations = new HashMap<>();
    private static final String DEFAULT_LANG = "en_us";

    public static void load(String lang)
    {
        translations.clear();

        loadLanguage(DEFAULT_LANG);

        if (!lang.equals(DEFAULT_LANG))
        {
            loadLanguage(lang);
        }
    }

    /**
     * Loads a language file from the resources folder and parses its content.
     */
    private static void loadLanguage(String lang)
    {
        String resourcePath = "/lang/" + lang + ".json";
        try (InputStream is = I18nEngine.class.getResourceAsStream(resourcePath))
        {
            if (is == null)
            {
                System.err.println("Could not find language file at " + resourcePath);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)))
            {
                StringBuilder jsonContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                {
                    jsonContent.append(line);
                }

                parseJson(jsonContent.toString());
                System.out.println("Successfully loaded language file: " + resourcePath);
            }
        } catch (Exception e)
        {
            System.err.println("Failed to load language file " + resourcePath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void parseJson(String json)
    {
        Pattern pattern = Pattern.compile("\\s*\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find())
        {
            String key = matcher.group(1);
            String value = matcher.group(2);
            translations.put(key, value);
        }
    }

    public static String get(I18nKey key)
    {
        return get(key.getIdentifier());
    }

    public static String get(String identifier)
    {
        return translations.getOrDefault(identifier, identifier);
    }

    public static String get(I18nKey key, Object... args)
    {
        String template = get(key);
        return String.format(template, args);
    }
}
