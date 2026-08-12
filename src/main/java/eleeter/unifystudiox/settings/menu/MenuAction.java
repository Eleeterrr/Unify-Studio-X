package eleeter.unifystudiox.settings.menu;

import eleeter.unifystudiox.i18n.I18nKey;
import eleeter.unifystudiox.ui.menu.IconDrawer;

public class MenuAction
{
    private final String title;
    private IconDrawer icon = null;
    private final String shortcut;
    private final Runnable action;
    private final String tooltipDescription;
    private boolean enabled = true;
    private boolean separator = false;

    public MenuAction(String title, String shortcut, I18nKey tooltipDescription, Runnable action)
    {
        this.title = title;
        this.shortcut = shortcut;
        this.tooltipDescription = tooltipDescription.getValue();
        this.action = action;
    }

    public MenuAction setIcon(IconDrawer icon)
    {
        this.icon = icon;
        return this;
    }

    public IconDrawer getIcon()
    {
        return this.icon;
    }

    /* Constructor for separators */
    private MenuAction()
    {
        this.title = "";
        this.shortcut = "";
        this.tooltipDescription = "";
        this.action = null;
        this.separator = true;
        this.enabled = false;
    }

    public static MenuAction separator()
    {
        return new MenuAction();
    }

    public String getTitle()
    {
        return this.title;
    }

    public String getShortcut()
    {
        return this.shortcut;
    }

    public String getTooltipDescription()
    {
        return this.tooltipDescription;
    }

    public Runnable getAction()
    {
        return this.action;
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isSeparator()
    {
        return this.separator;
    }
}
