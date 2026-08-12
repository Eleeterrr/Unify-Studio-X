package eleeter.unifystudiox.ui.inspector;

import java.util.ArrayList;
import java.util.List;

import eleeter.unifystudiox.scene.entity.SceneEntity;

public class InspectorRegistry
{
    private final List<EditorEntry<?>> editors = new ArrayList<>();

    private static class EditorEntry<T>
    {
        final Class<T> type;
        final EntityEditor<T> editor;

        EditorEntry(Class<T> type, EntityEditor<T> editor)
        {
            this.type = type;
            this.editor = editor;
        }
    }

    public <T> void register(Class<T> type, EntityEditor<T> editor)
    {
        this.editors.add(new EditorEntry<>(type, editor));
    }

    @SuppressWarnings("unchecked")
    public void buildUIForEntity(SceneEntity entity, InspectorBuilder builder)
    {
        if (entity == null) return;
        
        // Find all matching editors (e.g. interfaces, superclasses, exact class)
        // We evaluate in the order they were registered.
        for (EditorEntry<?> entry : editors)
        {
            if (entry.type.isInstance(entity))
            {
                // Safe cast because we checked isInstance
                ((EntityEditor<Object>) entry.editor).onInspect(entity, builder);
            }
        }
    }
}
