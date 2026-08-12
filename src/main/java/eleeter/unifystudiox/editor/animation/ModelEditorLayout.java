package eleeter.unifystudiox.editor.animation;

import eleeter.unifystudiox.assets.IModelAsset;
import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.model_editor.SceneHierarchyPanelDataSource;
import eleeter.unifystudiox.ui.model_editor.UIModelEditSpace;
import eleeter.unifystudiox.ui.model_editor.UIModelHierarchyController;

public class ModelEditorLayout
{
    private final UIModelEditSpace viewportPanel;
    private final UIModelHierarchyController hierarchyController;


    private ModelEditorLayout(Builder builder)
    {
        this.viewportPanel = builder.viewportPanel;
        this.hierarchyController = builder.hierarchyController;
    }

    /**
     * Registers the unified editor workspace panel with the given root UI panel.
     */
    public void addTo(UIPanel root)
    {
        root.addChild(this.viewportPanel);
    }

    /**
     * Opens the editor and activates the workspace for the specified model asset.
     */
    public void open(IModelAsset model)
    {
        this.viewportPanel.setModel(model);
    }

    /**
     * Closes the editor and deactivates the workspace.
     */
    public void close()
    {
        this.viewportPanel.setModel(null);
    }

    /**
     * Called every frame to drive the hierarchy outliner state synchronization.
     */
    public void update()
    {
        this.hierarchyController.update();
    }

    /**
     * Performs final cleanup of GPU framebuffer resources. Call on system shutdown.
     */
    public void cleanup()
    {
        this.viewportPanel.cleanup();
    }

    public UIModelEditSpace getViewportPanel()
    {
        return this.viewportPanel;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static final class Builder
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

        public ModelEditorLayout build()
        {
            return new ModelEditorLayout(this);
        }
    }
}
