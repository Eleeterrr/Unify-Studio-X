package eleeter.unifystudiox.launcher;

public class ProjectLauncherResult
{
    private final boolean shouldExit;
    private final ProjectEntry selectedProject;

    private ProjectLauncherResult(boolean shouldExit, ProjectEntry selectedProject)
    {
        this.shouldExit = shouldExit;
        this.selectedProject = selectedProject;
    }

    /** Creates a result that signals the engine should shut down entirely. */
    public static ProjectLauncherResult exit()
    {
        return new ProjectLauncherResult(true, null);
    }

    /** Creates a result that signals a project was chosen and the editor should open. */
    public static ProjectLauncherResult openProject(ProjectEntry entry)
    {
        return new ProjectLauncherResult(false, entry);
    }

    public boolean shouldExit()
    {
        return this.shouldExit;
    }

    public ProjectEntry getSelectedProject()
    {
        return this.selectedProject;
    }
}
