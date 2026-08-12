package eleeter.unifystudiox.ui.launcher;

import eleeter.unifystudiox.launcher.ProjectEntry;

/**
 * Callback interface separating the Project Manager UI panel from
 * the launcher window/view layer. The panel never knows how its actions
 * are handled — it only fires events through this interface.
 */
public interface IProjectManagerListener
{
    /** Called when the user activates an existing project entry. */
    void onProjectOpened(ProjectEntry entry);

    /** Called when the user submits the New Project form. */
    void onNewProjectRequested(String name, String path);

    /** Called when the user removes a project from the recent list. */
    void onProjectDeleted(String path);

    /** Called when the user closes the window without selecting a project. */
    void onExitRequested();
}
