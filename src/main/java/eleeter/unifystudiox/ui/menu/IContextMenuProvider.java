package eleeter.unifystudiox.ui.menu;

/**
 * Interface for elements that can provide a context menu upon right-click.
 */
public interface IContextMenuProvider
{
    /**
     * Returns the context menu to display, or null if no menu should be shown.
     */
    UIContextMenu getContextMenu(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context);
}
