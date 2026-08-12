package eleeter.unifystudiox.settings;

import java.util.function.Consumer;
import java.util.function.Supplier;

import eleeter.unifystudiox.i18n.I18nKey;

public class SettingEntry<T>
{
    private final String id;
    private final String label;
    private final String category;
    private final SettingType type;

    private final T defaultValue;
    private T minValue;
    private T maxValue;
    private float step = 1.0f;

    private final Supplier<T> getter;
    private final Consumer<T> setter;

    private boolean shouldSave = true;
    private I18nKey tooltipKey = null;
    private I18nKey labelKey = null;

    public SettingEntry(String id, String category, String label, SettingType type, T defaultValue, Supplier<T> getter, Consumer<T> setter)
    {
        this.id = id;
        this.category = category;
        this.label = label;
        this.type = type;
        this.defaultValue = defaultValue;
        this.getter = getter;
        this.setter = setter;
    }

    public SettingEntry<T> withTooltip(I18nKey key)
    {
        this.tooltipKey = key;
        return this;
    }

    public SettingEntry<T> withLabel(I18nKey key)
    {
        this.labelKey = key;
        return this;
    }

    public String getTooltip()
    {
        return this.tooltipKey != null ? this.tooltipKey.getValue() : "";
    }

    public void setRange(T min, T max, float step)
    {
        this.minValue = min;
        this.maxValue = max;
        this.step = step;
    }

    public void applyValue(T value)
    {
        if (this.setter != null)
        {
            this.setter.accept(value);
        }
    }

    public T getValue()
    {
        return this.getter != null ? this.getter.get() : this.defaultValue;
    }

    public void resetToDefault()
    {
        this.applyValue(this.defaultValue);
    }

    public String getId()
    {
        return this.id;
    }

    public String getLabel()
    {
        return this.labelKey != null ? this.labelKey.getValue() : this.label;
    }

    public I18nKey getLabelKey()
    {
        return this.labelKey;
    }

    public String getCategory()
    {
        return this.category;
    }

    public SettingType getType()
    {
        return this.type;
    }

    public T getDefaultValue()
    {
        return this.defaultValue;
    }

    public T getMinValue()
    {
        return this.minValue;
    }

    public T getMaxValue()
    {
        return this.maxValue;
    }

    public float getStep()
    {
        return this.step;
    }

    public boolean isShouldSave()
    {
        return this.shouldSave;
    }

    /* Why it's here? Not sure and don't ask */
    public void setShouldSave(boolean shouldSave)
    {
        this.shouldSave = shouldSave;
    }
}
