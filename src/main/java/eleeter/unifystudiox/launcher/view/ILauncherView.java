package eleeter.unifystudiox.launcher.view;

import eleeter.unifystudiox.input.InputHandler;
import eleeter.unifystudiox.launcher.ProjectLauncherResult;

public interface ILauncherView
{
    void init();

    void update(InputHandler input, int logicalW, int logicalH);

    void render(int logicalW, int logicalH, int physicalW, int physicalH);

    boolean hasResult();

    ProjectLauncherResult consumeResult();

    void destroy();
}
