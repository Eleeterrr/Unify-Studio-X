package eleeter.unifystudiox.graphics;

import eleeter.unifystudiox.graphics.layout.Icon;
import eleeter.unifystudiox.input.InputHandler;
import eleeter.unifystudiox.renderer.Renderer;
import eleeter.unifystudiox.renderer.core.RenderSettings;
import eleeter.unifystudiox.graphics.gl.GLGraphicsBackend;
import eleeter.unifystudiox.scene.Scene;
import eleeter.unifystudiox.ui.UIKey;
import eleeter.unifystudiox.util.ScreenshotCapture;
import java.nio.IntBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
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
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window
{

    private static final double TARGET_UPS = 60.0;
    private static final double UPDATE_DELTA = 1.0 / TARGET_UPS;

    private final String title;
    private int width;
    private int height;
    private int logicalWidth;
    private int logicalHeight;
    private long windowHandle;

    private InputHandler inputHandler;
    private Icon icon;
    private final ScreenshotCapture screenshotCapture;


    public Icon icon(long handle, String path)
    {
        return new Icon(handle, path);
    }

    public Window(String title, int width, int height)
    {
        this.title = title;
        this.width = width;
        this.height = height;
        this.screenshotCapture = new ScreenshotCapture();
    }



    public void run(Scene scene, Renderer renderer, GLGraphicsBackend backend)
    {
        try
        {
            this.init();

            Shaders.init(backend);

            this.inputHandler = new InputHandler(this.windowHandle);
            this.screenshotCapture.init();

            glfwSetFramebufferSizeCallback(this.windowHandle, (win, w, h) ->
            {
                this.width = w;
                this.height = h;

                int[] lw = {0}, lh = {0};
                glfwGetWindowSize(win, lw, lh);
                this.logicalWidth = lw[0];
                this.logicalHeight = lh[0];

                renderer.resize(w, h);
            });

            renderer.init(scene);

            this.loop(scene, renderer);
        } finally
        {
            renderer.cleanup();
            Shaders.cleanup();
            glfwFreeCallbacks(this.windowHandle);
            glfwDestroyWindow(this.windowHandle);
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }




    public long getHandle()
    {
        return this.windowHandle;
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return this.height;
    }

    public int getLogicalWidth()
    {
        return this.logicalWidth;
    }

    public int getLogicalHeight()
    {
        return this.logicalHeight;
    }


    private void init()
    {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
        {
            throw new IllegalStateException("Unable to initialise GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 4);

        this.windowHandle = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
        if (this.windowHandle == NULL)
        {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        //TODO: For now we just hardcode the File Location,
        // But In the future we'll use a Config File or A Resource Class to load the Icon.
        this.icon(this.windowHandle, "assets/logo.png");
        centreWindow();

        
        int[] lw = {0}, lh = {0};
        glfwGetWindowSize(this.windowHandle, lw, lh);
        this.logicalWidth = lw[0];
        this.logicalHeight = lh[0];
        
        int[] pw = {0}, ph = {0};
        GLFW.glfwGetFramebufferSize(this.windowHandle, pw, ph);
        this.width = pw[0];
        this.height = ph[0];

        glfwMakeContextCurrent(this.windowHandle);
        glfwSwapInterval(1);
        glfwShowWindow(this.windowHandle);

        GL.createCapabilities();
    }

    /**
     * TODO: For now, update as many times as needed, render once.
     */
    private void loop(Scene scene, Renderer renderer)
    {
        double previous = glfwGetTime();
        double accumulator = 0.0;
        int lastVsync = -1;

        while (!glfwWindowShouldClose(this.windowHandle))
        {
            int currentVsync = RenderSettings.VSYNC_ENABLED ? 1 : 0;
            if (currentVsync != lastVsync)
            {
                glfwSwapInterval(currentVsync);
                lastVsync = currentVsync;
            }

            double current = glfwGetTime();
            double elapsed = current - previous;
            previous = current;
            accumulator += Math.min(elapsed, 0.25);

            while (accumulator >= UPDATE_DELTA)
            {
                this.inputHandler.poll();
                
                if (!scene.getUi().getContext().isUIBlockingInput())
                {
                    this.inputHandler.wrapMouse(this.logicalWidth, this.logicalHeight);
                }
                
                scene.update(UPDATE_DELTA, this.inputHandler, this);
                
                if (this.inputHandler.isKeyPressed(UIKey.SCREENSHOT_KEY))
                {
                    this.screenshotCapture.requestCapture();
                }

                accumulator -= UPDATE_DELTA;
            }

            renderer.render(scene, this.width, this.height, this.logicalWidth, this.logicalHeight);
            this.screenshotCapture.processPendingCaptures(scene, renderer);

            glfwSwapBuffers(this.windowHandle);
            glfwPollEvents();
            
            double fpsLimit = RenderSettings.VSYNC_ENABLED ? 9999.0 : RenderSettings.TARGET_FPS;
            double syncTime = previous + (1.0 / fpsLimit);
            while (glfwGetTime() < syncTime)
            {
                try
                {
                    Thread.sleep(1);
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    public void close()
    {
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void centreWindow()
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(this.windowHandle, pWidth, pHeight);
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (vidMode != null)
            {
                glfwSetWindowPos(this.windowHandle, (vidMode.width() - pWidth.get(0)) / 2, (vidMode.height() - pHeight.get(0)) / 2);
            }
        }
    }
}
