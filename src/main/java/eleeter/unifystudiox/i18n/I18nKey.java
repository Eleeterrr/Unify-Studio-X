package eleeter.unifystudiox.i18n;

public class I18nKey
{
    private final String identifier;
    private boolean requested;

    public I18nKey(String identifier)
    {
        this.identifier = identifier;
        this.requested = false;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public boolean isRequested()
    {
        return requested;
    }

    public String getValue()
    {
        this.requested = true;
        return I18nEngine.get(this.identifier);
    }

    public String format(Object... args)
    {
        this.requested = true;
        return String.format(getValue(), args);
    }

    @Override
    public String toString()
    {
        return getValue();
    }
}
