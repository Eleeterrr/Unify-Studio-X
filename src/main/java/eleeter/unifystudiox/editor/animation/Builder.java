package eleeter.unifystudiox.editor.animation;

import eleeter.unifystudiox.ui.model_editor.SceneHierarchyPanelDataSource;
import eleeter.unifystudiox.ui.model_editor.UIModelEditSpace;
import eleeter.unifystudiox.ui.model_editor.UIModelHierarchyController;

public class Builder
{
    private final UIModelEditSpace viewportPanel = new UIModelEditSpace();
    private final UIModelHierarchyController hierarchyController;
    private final HierarchyPanelDataSource dataSource;

    private Builder()
    {
        this.dataSource = new SceneHierarchyPanelDataSource(() -> this.viewportPanel.getViewport().getScene(), () -> this.viewportPanel.getViewport().getCurrentEntityId());
        this.hierarchyController = new UIModelHierarchyController(this.viewportPanel.getHierarchyPanel(), this.dataSource);
        this.hierarchyController.init();
    }

}
