package eleeter.unifystudiox.launcher.view;

import eleeter.unifystudiox.input.InputHandler;
import eleeter.unifystudiox.launcher.IProjectRegistry;
import eleeter.unifystudiox.launcher.ProjectEntry;
import eleeter.unifystudiox.launcher.ProjectLauncherResult;
import eleeter.unifystudiox.renderer.FontManager;
import eleeter.unifystudiox.ui.framework.render.context.UISystem;
import eleeter.unifystudiox.ui.framework.render.gl.GLUIRenderer;
import eleeter.unifystudiox.ui.launcher.IProjectManagerListener;
import eleeter.unifystudiox.ui.launcher.UIProjectManagerPanel;

public class LauncherUIView implements ILauncherView, IProjectManagerListener
{
    private static final String FONT_KEY = "inter";
    private static final String FONT_JSON = "assets/fonts/atlas.json";
    private static final String FONT_ATLAS = "/textures/fonts/atlas.png";
    private static final double FIXED_DELTA = 1.0 / 60.0;

    private final IProjectRegistry registry;

    private UISystem uiSystem;
    private UIProjectManagerPanel panel;
    private ProjectLauncherResult pendingResult;

    public LauncherUIView(IProjectRegistry registry)
    {
        this.registry = registry;
    }

    @Override
    public void init()
    {
        FontManager.load(FONT_KEY, FONT_JSON, FONT_ATLAS);

        this.uiSystem = new UISystem(new GLUIRenderer());

        this.panel = new UIProjectManagerPanel("launcher_panel", this.uiSystem.getContext(), this.registry, this);

        this.panel.getTransform().set(0.0F, 0.0F, 1.0F, 1.0F);
        this.uiSystem.getRoot().addChild(this.panel);
    }

    @Override
    public void update(InputHandler input, int logicalW, int logicalH)
    {
        this.uiSystem.processInput(input, (float) logicalW, (float) logicalH);
        this.uiSystem.update(FIXED_DELTA, (float) logicalW, (float) logicalH);
    }

    @Override
    public void render(int logicalW, int logicalH, int physicalW, int physicalH)
    {
        this.uiSystem.render((float) logicalW, (float) logicalH, (float) physicalW, (float) physicalH);
    }

    @Override
    public boolean hasResult()
    {
        return this.pendingResult != null;
    }

    @Override
    public ProjectLauncherResult consumeResult()
    {
        ProjectLauncherResult result = this.pendingResult;
        this.pendingResult = null;
        return result;
    }

    @Override
    public void destroy()
    {
        if (this.panel != null)
        {
            this.panel.cleanup();
        }
        this.uiSystem.cleanup();
        FontManager.cleanup();
    }


    @Override
    public void onProjectOpened(ProjectEntry entry)
    {
        this.pendingResult = ProjectLauncherResult.openProject(entry);
    }

    @Override
    public void onNewProjectRequested(String name, String path)
    {
    }

    @Override
    public void onProjectDeleted(String path)
    {
    }

    @Override
    public void onExitRequested()
    {
        this.pendingResult = ProjectLauncherResult.exit();
    }
}
