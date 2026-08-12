package eleeter.unifystudiox.settings.menu;

import java.util.ArrayList;
import java.util.List;

import eleeter.unifystudiox.ui.menu.IconDrawer;

public class MenuCategory
{
    private final String id;
    private IconDrawer icon = null;


    private final String title;
    private final List<MenuAction> actions = new ArrayList<>();

    public MenuCategory(String id, String title)
    {
        this.id = id;
        this.title = title;
    }
    public MenuCategory setIcon(IconDrawer icon)
    {
        this.icon = icon;
        return this;
    }

    public void addAction(MenuAction action)
    {
        this.actions.add(action);
    }

    public void addSeparator()
    {
        this.actions.add(MenuAction.separator());
    }

    public String getId()
    {
        return this.id;
    }

    public IconDrawer getIcon()
    {
        return this.icon;
    }

    public String getTitle()
    {
        return this.title;
    }

    public List<MenuAction> getActions()
    {
        return this.actions;
    }
}
