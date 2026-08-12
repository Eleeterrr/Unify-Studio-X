package eleeter.unifystudiox.ui.settings;

import java.util.ArrayList;
import java.util.List;

import eleeter.unifystudiox.settings.SettingEntry;
import eleeter.unifystudiox.settings.SettingType;
import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.widgets.UIButton;
import eleeter.unifystudiox.ui.widgets.UIDropdown;
import eleeter.unifystudiox.ui.widgets.UILabel;
import eleeter.unifystudiox.ui.widgets.UINumberField;
import eleeter.unifystudiox.ui.widgets.UIToggle;

public class SettingsBuilder
{
    private final UIPanel parentPanel;
    private final eleeter.unifystudiox.ui.framework.render.context.UIInputContext context;
    private UIPanel headerBar;
    private UILabel label;
    private UIButton button;
    private UIToggle toggle;
    private UILabel header;
    private UINumberField field;
    private UIDropdown dropdown;

    private float currentY = 10.0f;
    private final float rowHeight = 24.0f;
    private final float labelColumnWidth = 0.45f;
    private final float paddingX = 10.0f;
    private final float widgetSpacing = 5.0f;


    private final List<Runnable> updateHooks = new ArrayList<>();


    public UILabel getLabel()
    {
        return label;
    }

    public eleeter.unifystudiox.ui.framework.render.context.UIInputContext getContext()
    {
        return context;
    }

    public UIButton getButton()
    {
        return button;
    }

    public UILabel getHeader()
    {
        return header;
    }

    public UIPanel getHeaderBar()
    {
        return headerBar;
    }



    public SettingsBuilder(UIPanel parentPanel, eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        this.parentPanel = parentPanel;
        this.context = context;
    }

    public void addHeader(SettingEntry<?> entry)
    {
        this.currentY += 10.0f;

        this.headerBar = new UIPanel(this.parentPanel.getId() + "_header_bar_" + entry.getId());
        this.headerBar.getTransform().set(0, 0, 1.0f, 0).setPixelOffset(0, (int)this.currentY).setPixelSize(0, 22);

        this.headerBar.setBackgroundColor(0.22f, 0.22f, 0.22f, 1.0f);
        this.parentPanel.addChild(headerBar);

        this.header = new UILabel(this.parentPanel.getId() + "_header_text_" + entry.getId());
        if (entry.getLabelKey() != null)
        {
            this.header.setKey(entry.getLabelKey());
        } else
        {
            this.header.setText(entry.getLabel().toUpperCase());
        }
        this.header.getTransform().set(0, 0, 1.0f, 1.0f);
        this.header.getTransform().setPixelOffset(10, 0); // Inner padding

        this.header.setTextColor(1.0f, 1.0f, 1.0f, 1.0f); // Brightest text
        this.headerBar.addChild(this.header);
        
        this.currentY += 28.0f;
    }

    public void addSetting(SettingEntry<?> entry)
    {
        if (entry.getType() == SettingType.HEADER)
        {
            this.addHeader(entry);
            return;
        }

        if (entry.getType() == SettingType.ACTION)
        {
            this.addButton(entry, 0.0f, 1.0f);
            this.currentY += this.rowHeight + this.widgetSpacing;
            return;
        }

        float indent = 12.0f;
        
        this.label = new UILabel(entry.getId() + "_label");
        if (entry.getLabelKey() != null)
        {
            this.label.setKey(entry.getLabelKey());
        } else
        {
            this.label.setText(entry.getLabel());
        }
        this.label.getTransform().set(0, 0, this.labelColumnWidth, 0)
            .setPixelOffset((int)(this.paddingX + indent), (int)this.currentY + 4)
            .setPixelSize((int)-(this.paddingX + indent), (int)this.rowHeight);
        this.label.setTextColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.parentPanel.addChild(this.label);

        float widgetX = this.labelColumnWidth;
        float widgetW = 1.0f - this.labelColumnWidth;

        switch (entry.getType())
        {
            case TOGGLE:
                this.addToggle(entry, widgetX, widgetW);
                break;
            case SLIDER:
            case FIELD:
                this.addField(entry, widgetX, widgetW);
                break;
            case DROPDOWN:
                this.addDropdown(entry, widgetX, widgetW);
                break;
            default:
                break;
        }

        if (entry.getTooltip() != null && !entry.getTooltip().isEmpty())
        {
            this.label.tooltip(entry.getTooltip());
            if (entry.getType() == SettingType.TOGGLE && this.toggle != null)
            {
                this.toggle.tooltip(entry.getTooltip());
            }
            else if ((entry.getType() == SettingType.SLIDER || entry.getType() == SettingType.FIELD) && this.field != null)
            {
                this.field.tooltip(entry.getTooltip());
            }
        }

        this.currentY += this.rowHeight + this.widgetSpacing;
    }

    @SuppressWarnings("unchecked")
    private void addToggle(SettingEntry<?> entry, float relX, float relW)
    {
        SettingEntry<Boolean> boolEntry = (SettingEntry<Boolean>) entry;
        this.toggle = new UIToggle(entry.getId() + "_widget", this.context);
        this.toggle.setDefaultState(boolEntry.getValue());
        this.toggle.getTransform().set(relX, 0, 0, 0)
            .setPixelOffset((int)this.paddingX, (int)this.currentY + 2)
            .setPixelSize(40, (int)this.rowHeight - 4);

        this.toggle.setOnToggle(boolEntry::applyValue);
        this.parentPanel.addChild(this.toggle);

        final UIToggle capturedToggle = this.toggle;
        this.updateHooks.add(() ->
        {
            if (capturedToggle.isChecked() != boolEntry.getValue())
            {
                capturedToggle.setDefaultState(boolEntry.getValue());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void addField(SettingEntry<?> entry, float relX, float relW)
    {
        SettingEntry<Float> floatEntry = (SettingEntry<Float>) entry;
        this.field = new UINumberField(entry.getId() + "_widget", this.context);
        this.field.setValue(floatEntry.getValue());

        if (floatEntry.getMinValue() != null && floatEntry.getMaxValue() != null)
        {
            this.field.setRange(floatEntry.getMinValue(), floatEntry.getMaxValue());
        }

        this.field.setStep(floatEntry.getStep());

        this.field.getTransform().set(relX, 0, relW, 0)
            .setPixelOffset((int)this.paddingX, (int)this.currentY)
            .setPixelSize((int)-(this.paddingX * 2), (int)this.rowHeight);
        this.field.setOnValueChanged(floatEntry::applyValue);
        this.parentPanel.addChild(this.field);

        final UINumberField capturedField = this.field;
        this.updateHooks.add(() ->
        {
            if (!capturedField.isInteracting() && Math.abs(capturedField.getValue() - floatEntry.getValue()) > 0.0001F)
            {
                capturedField.setValue(floatEntry.getValue());
            }
        });
    }

    private void addButton(SettingEntry<?> entry, float relX, float relW)
    {
        this.button = new UIButton(entry.getId() + "_widget", this.context);
        if (entry.getLabelKey() != null)
        {
            this.button.setKey(entry.getLabelKey());
        } else
        {
            this.button.setText(entry.getLabel());
        }
        this.button.getTransform().set(relX, 0, relW, 0)
            .setPixelOffset((int)this.paddingX, (int)this.currentY)
            .setPixelSize((int)-(this.paddingX * 2), (int)this.rowHeight);

        this.button.setOnClick(() -> entry.applyValue(null));
        this.parentPanel.addChild(this.button);

        if (entry.getTooltip() != null && !entry.getTooltip().isEmpty())
        {
            this.button.tooltip(entry.getTooltip());
        }
    }

    @SuppressWarnings("unchecked")
    private void addDropdown(SettingEntry<?> entry, float relX, float relW)
    {
        SettingEntry<String> stringEntry = (SettingEntry<String>) entry;
        this.dropdown = new UIDropdown(entry.getId() + "_widget", this.context);

        String optionsRaw = stringEntry.getDefaultValue() != null ? stringEntry.getDefaultValue() : "";
        for (String pair : optionsRaw.split(","))
        {
            String[] parts = pair.split(":", 2);
            if (parts.length == 2)
            {
                this.dropdown.addOption(parts[0].trim(), parts[1].trim());
            }
        }

        String currentValue = stringEntry.getValue();
        if (currentValue != null && !currentValue.isEmpty())
        {
            this.dropdown.setSelected(currentValue);
        }

        this.dropdown.setOnSelect(key ->
        {
            stringEntry.applyValue(key);
        });

        this.dropdown.getTransform().set(relX, 0, relW, 0)
            .setPixelOffset((int) this.paddingX, (int) this.currentY)
            .setPixelSize((int) -(this.paddingX * 2), (int) this.rowHeight);
        this.dropdown.setZIndex(200);
        this.parentPanel.addChild(this.dropdown);

        final UIDropdown capturedDropdown = this.dropdown;
        this.updateHooks.add(() ->
        {
            String live = stringEntry.getValue();
            if (live != null && !live.equals(capturedDropdown.getSelectedKey()))
            {
                capturedDropdown.setSelected(live);
            }
        });
    }

    public float getTotalHeight()
    {
        return this.currentY + 20.0f;
    }

    public void update()
    {
        for (Runnable hook : this.updateHooks)
        {
            hook.run();
        }
    }
}
