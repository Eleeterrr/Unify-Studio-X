package eleeter.unifystudiox.settings.menu;

import java.util.ArrayList;
import java.util.List;

public class MenuBarRegistry
{
    private final List<MenuCategory> categories = new ArrayList<>();

    public MenuCategory registerCategory(String id, String title)
    {
        /* Check if already exists to prevent duplicates */
        for (MenuCategory category : this.categories)
        {
            if (category.getId().equals(id))
            {
                return category;
            }
        }

        MenuCategory category = new MenuCategory(id, title);
        this.categories.add(category);
        return category;
    }

    public List<MenuCategory> getCategories()
    {
        return this.categories;
    }

    public MenuCategory getCategory(String id)
    {
        for (MenuCategory category : this.categories)
        {
            if (category.getId().equals(id))
            {
                return category;
            }
        }
        return null;
    }
}
