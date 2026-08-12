package eleeter.unifystudiox.settings;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import eleeter.unifystudiox.i18n.I18nKey;

public class SettingsRegistry
{
    private static final Map<String, SettingEntry<?>> settings = new LinkedHashMap<>();

    /**
     * Registers a pre-constructed SettingEntry.
     */
    public static void register(SettingEntry<?> entry)
    {
        settings.put(entry.getId(), entry);
    }

    /**
     * Registers a boolean toggle setting.
     */
    public static SettingEntry<Boolean> registerToggle(String id, String category, String label, boolean defaultValue, Supplier<Boolean> getter, Consumer<Boolean> setter)
    {
        SettingEntry<Boolean> entry = new SettingEntry<>(id, category, label, SettingType.TOGGLE, defaultValue, getter, setter);
        register(entry);
        return entry;
    }

    /**
     * Registers a numeric slider setting.
     */
    public static SettingEntry<Float> registerSlider(String id, String category, String label, float min, float max, float step, float defaultValue, Supplier<Float> getter, Consumer<Float> setter)
    {
        SettingEntry<Float> entry = new SettingEntry<>(id, category, label, SettingType.SLIDER, defaultValue, getter, setter);
        entry.setRange(min, max, step);
        register(entry);
        return entry;
    }

    /**
     * Registers a precise numeric field setting.
     */
    public static SettingEntry<Float> registerField(String id, String category, String label, float min, float max, float step, float defaultValue, Supplier<Float> getter, Consumer<Float> setter)
    {
        SettingEntry<Float> entry = new SettingEntry<>(id, category, label, SettingType.FIELD, defaultValue, getter, setter);
        entry.setRange(min, max, step);
        register(entry);
        return entry;
    }

    /**
     * Registers a triggerable action (button).
     */
    public static SettingEntry<?> registerAction(String id, String category, String label, Runnable action)
    {
        SettingEntry<?> entry = new SettingEntry<>(id, category, label, SettingType.ACTION, null, () -> null, val -> action.run());
        register(entry);
        return entry;
    }

    /**
     * Registers a dropdown whose options are expressed as a comma-separated
     * "key:Label" string packed into defaultValue. The getter and setter operate
     * on the selected key string.
     */
    public static SettingEntry<String> registerDropdown(String id, String category, String label, String optionsPacked, String initialKey, Supplier<String> getter, Consumer<String> setter)
    {
        SettingEntry<String> entry = new SettingEntry<>(id, category, label, SettingType.DROPDOWN, optionsPacked, getter, setter);
        register(entry);
        return entry;
    }

    /**
     * Registers a UI header for grouping.
     */
    public static SettingEntry<?> registerHeader(String category, String label)
    {
        SettingEntry<?> entry = new SettingEntry<>("header_" + category + "_" + label, category, label, SettingType.HEADER, null, null, null);
        register(entry);
        return entry;
    }

    public static SettingEntry<?> registerHeader(String category, I18nKey labelKey)
    {
        SettingEntry<?> entry = new SettingEntry<>("header_" + category + "_" + labelKey.getValue(), category, labelKey.getValue(), SettingType.HEADER, null, null, null).withLabel(labelKey);
        register(entry);
        return entry;
    }

    public static SettingEntry<?> getSetting(String id)
    {
        return settings.get(id);
    }

    public static List<SettingEntry<?>> getAllSettings()
    {
        return new ArrayList<>(settings.values());
    }

    /**
     * Returns all settings grouped by category.
     */
    public static Map<String, List<SettingEntry<?>>> getSettingsByCategory()
    {
        Map<String, List<SettingEntry<?>>> grouped = new LinkedHashMap<>();
        for (SettingEntry<?> entry : settings.values())
        {
            grouped.computeIfAbsent(entry.getCategory(), k -> new ArrayList<>()).add(entry);
        }
        return grouped;
    }
}
