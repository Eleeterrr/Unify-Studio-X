package eleeter.unifystudiox.ui.framework.style;

/**
 * Interface defining how components dynamically resolve their specific UI styles
 * depending on semantic token names and component interaction states.
 */
public interface IUITheme
{

    /**
     * Resolves the immutable style specification for a given token and interaction state.
     */
    UIStyleStateSpec getStyle(String componentToken, UIElementState state);
}
