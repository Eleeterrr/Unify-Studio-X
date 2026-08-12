package eleeter.unifystudiox.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class SettingsIO
{
    private static final String DEFAULT_FILE = "settings.cfg";

    public static void save()
    {
        save(DEFAULT_FILE);
    }

    public static void save(String filename)
    {
        Properties props = new Properties();

        for (SettingEntry<?> entry : SettingsRegistry.getAllSettings())
        {
            if (entry.isShouldSave() && entry.getType() != SettingType.ACTION && entry.getType() != SettingType.HEADER)
            {
                Object value = entry.getValue();
                if (value != null)
                {
                    props.setProperty(entry.getId(), value.toString());
                }
            }
        }

        try (FileOutputStream out = new FileOutputStream(filename))
        {
            props.store(out, "AniMatrix Engine Settings");
        }
        catch (IOException e)
        {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }

    public static void load()
    {
        load(DEFAULT_FILE);
    }

    public static void load(String filename)
    {
        File file = new File(filename);
        if (!file.exists())
        {
            return;
        }

        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(file))
        {
            props.load(in);
        }
        catch (IOException e)
        {
            System.err.println("Failed to load settings: " + e.getMessage());
            return;
        }

        for (SettingEntry<?> entry : SettingsRegistry.getAllSettings())
        {
            String valueStr = props.getProperty(entry.getId());
            if (valueStr == null)
            {
                continue;
            }

            try
            {
                applyStringValue(entry, valueStr);
            }
            catch (Exception e)
            {
                System.err.println("Failed to parse setting '" + entry.getId() + "': " + e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void applyStringValue(SettingEntry<T> entry, String valueStr)
    {
        SettingType type = entry.getType();

        if (type == SettingType.TOGGLE)
        {
            Boolean val = Boolean.parseBoolean(valueStr);
            entry.applyValue((T) val);
        }
        else if (type == SettingType.SLIDER || type == SettingType.FIELD)
        {
            Float val = Float.parseFloat(valueStr);
            
            Float min = (Float) entry.getMinValue();
            Float max = (Float) entry.getMaxValue();
            if (min != null && val < min) val = min;
            if (max != null && val > max) val = max;
            
            entry.applyValue((T) val);
        }
    }
}
