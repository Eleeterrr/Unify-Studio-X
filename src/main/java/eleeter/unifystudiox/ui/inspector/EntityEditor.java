package eleeter.unifystudiox.ui.inspector;

/**
 * A bridge that instructs the InspectorBuilder on how to draw UI for a specific entity type.
 * Lives entirely in the UI package, keeping the core engine perfectly decoupled.
 */
public interface EntityEditor<T>
{
    void onInspect(T entity, InspectorBuilder builder);
}
