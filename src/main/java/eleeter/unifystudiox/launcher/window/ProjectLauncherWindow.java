package eleeter.unifystudiox.launcher.window;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.system.MemoryStack;

import eleeter.unifystudiox.input.InputHandler;
import eleeter.unifystudiox.launcher.ProjectLauncherResult;
import eleeter.unifystudiox.launcher.view.ILauncherView;

public class ProjectLauncherWindow
{
    private static final int WINDOW_W = 1100;
    private static final int WINDOW_H = 660;
    private static final double TARGET_UPS = 60.0;
    private static final double UPDATE_DELTA = 1.0 / TARGET_UPS;
    private static final double MAX_FRAME_TIME = 0.25;

    private final String title;
    private final ILauncherView view;

    private int physicalW = WINDOW_W;
    private int physicalH = WINDOW_H;
    private int logicalW = WINDOW_W;
    private int logicalH = WINDOW_H;


    public ProjectLauncherWindow(String title, ILauncherView view)
    {
        this.title = title;
        this.view = view;
    }


    public ProjectLauncherResult run()
    {
        try
        {
            long handle = this.createWindow();

            glfwSetFramebufferSizeCallback(handle, (win, w, h) ->
            {
                this.physicalW = w;
                this.physicalH = h;
            });

            GLFW.glfwSetWindowSizeCallback(handle, (win, w, h) ->
            {
                this.logicalW = w;
                this.logicalH = h;
            });

            glfwMakeContextCurrent(handle);
            glfwSwapInterval(1);
            glfwShowWindow(handle);
            GL.createCapabilities();

            InputHandler input = new InputHandler(handle);
            this.view.init();

            ProjectLauncherResult result = ProjectLauncherResult.exit();
            double previous = GLFW.glfwGetTime();
            double accumulator = 0.0;

            while (!glfwWindowShouldClose(handle))
            {
                double current = GLFW.glfwGetTime();
                double elapsed = Math.min(current - previous, MAX_FRAME_TIME);
                previous = current;
                accumulator += elapsed;

                boolean resultReady = false;
                while (accumulator >= UPDATE_DELTA)
                {
                    input.poll();
                    this.view.update(input, this.logicalW, this.logicalH);
                    accumulator -= UPDATE_DELTA;

                    /* Check inside the update loop so we exit immediately */
                    if (this.view.hasResult())
                    {
                        resultReady = true;
                        break;
                    }
                }

                GL11C.glClearColor(0.036F, 0.040F, 0.052F, 1.0F);
                GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT);
                this.view.render(this.logicalW, this.logicalH, this.physicalW, this.physicalH);
                glfwSwapBuffers(handle);
                glfwPollEvents();

                if (resultReady)
                {
                    result = this.view.consumeResult();
                    break;
                }
            }

            this.view.destroy();
            glfwDestroyWindow(handle);
            return result;
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            System.err.println("CRITICAL LAUNCHER EXCEPTION: " + t.getMessage());
            System.exit(1);
            throw new RuntimeException(t);
        }
    }

    private long createWindow()
    {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 4);

        long handle = glfwCreateWindow(WINDOW_W, WINDOW_H, this.title, NULL, NULL);
        if (handle == NULL)
        {
            throw new RuntimeException("Failed to create launcher GLFW window");
        }

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer pw = stack.mallocInt(1);
            IntBuffer ph = stack.mallocInt(1);
            glfwGetFramebufferSize(handle, pw, ph);
            this.physicalW = pw.get(0);
            this.physicalH = ph.get(0);

            IntBuffer lw = stack.mallocInt(1);
            IntBuffer lh = stack.mallocInt(1);
            glfwGetWindowSize(handle, lw, lh);
            this.logicalW = lw.get(0);
            this.logicalH = lh.get(0);

            GLFWVidMode vid = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (vid != null)
            {
                glfwSetWindowPos(handle, (vid.width() - WINDOW_W) / 2, (vid.height() - WINDOW_H) / 2);
            }
        }

        return handle;
    }
}
