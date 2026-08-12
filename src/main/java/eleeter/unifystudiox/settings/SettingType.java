package eleeter.unifystudiox.settings;

public enum SettingType
{
    /** A simple on/off switch. */
    TOGGLE,

    /** A range-based numeric selection with a visual handle. */
    SLIDER,

    /** A precise numeric or text input field. */
    FIELD,

    /** A triggerable button that executes an operation. */
    ACTION,

    /** A non-interactive section title for grouping other settings. */
    HEADER,

    /** A collapsed list of named options. The string value is the selected option's key. */
    DROPDOWN
}
