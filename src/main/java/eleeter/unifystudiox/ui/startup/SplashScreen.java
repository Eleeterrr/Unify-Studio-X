package eleeter.unifystudiox.ui.startup;

import org.lwjgl.glfw.GLFW;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import eleeter.unifystudiox.graphics.stb.ImageDecoder;
import eleeter.unifystudiox.ui.framework.render.gl.GLUIRenderer;
import eleeter.unifystudiox.ui.widgets.UIProgressBar;


public class SplashScreen
{
    private static long window;
    private static int logoTextureId = -1;
    private static Thread renderThread;
    private static volatile boolean running = false;
    private static volatile boolean engineFinished = false;

    private static float currentProgress = 0.0F;
    private static float targetProgress = 0.0F;

    public static void show()
    {
        if (!GLFW.glfwInit())
            return;

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_FLOATING, GLFW.GLFW_TRUE);

        window = GLFW.glfwCreateWindow(640, 360, "AniMatrix Startup", 0L, 0L);
        if (window == 0L)
            return;

        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        if (vidMode != null)
        {
            GLFW.glfwSetWindowPos(window, (vidMode.width() - 640) / 2, (vidMode.height() - 360) / 2);
        }

        GLFW.glfwShowWindow(window);

        GLFW.glfwMakeContextCurrent(MemoryUtil.NULL);

        running = true;
        engineFinished = false;
        currentProgress = 0.0F;
        targetProgress = 0.0F;

        renderThread = new Thread(SplashScreen::renderLoop, "SplashRenderThread");
        renderThread.start();
    }

    public static void close()
    {
        if (window == 0L)
            return;

        engineFinished = true;

        try
        {
            if (renderThread != null)
                renderThread.join(800L);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        running = false;
        GLFW.glfwDestroyWindow(window);
        window = 0L;
    }

    public static void pollEvents()
    {
        if (window != 0L)
            GLFW.glfwPollEvents();
    }


    private static GLUIRenderer uiRenderer;
    private static UIProgressBar uiProgressBar;

    private static void renderLoop()
    {
        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();

        logoTextureId = loadTexture("assets/mainlogo.png");

        uiRenderer = new GLUIRenderer();
        uiRenderer.init();

        uiProgressBar = new UIProgressBar("splash_progress");
        uiProgressBar.cx = 120.0F;
        uiProgressBar.cy = 320.0F;
        uiProgressBar.cw = 400.0F;
        uiProgressBar.ch = 4.0F;
        uiProgressBar.setTrackColor(0.1F, 0.12F, 0.15F, 1.0F);
        uiProgressBar.setFillColor(0.75F, 0.45F, 0.25F, 1.0F);

        long startTime = System.currentTimeMillis();

        while (running)
        {
            updateProgress(startTime);
            renderFrame();

            GLFW.glfwSwapBuffers(window);
            try
            {
                Thread.sleep(16L);
            } catch (InterruptedException e)
            {
                break;
            }
        }

        cleanup();
    }

    private static void updateProgress(long startTime)
    {
        float time = (System.currentTimeMillis() - startTime) / 1000.0F;

        if (engineFinished)
        {
            targetProgress = 1.0F;
            currentProgress += (targetProgress - currentProgress) * 0.15F;
            if (currentProgress > 0.99F)
            {
                running = false;
            }
        } else
        {
            targetProgress = (float) (0.9D - 0.9D * Math.exp(-time * 0.4F));
            currentProgress += (targetProgress - currentProgress) * 0.1F;
        }
    }

    private static void renderFrame()
    {
        GL45C.glClearColor(0.08F, 0.09F, 0.12F, 1.0F);
        GL45C.glClear(GL45C.GL_COLOR_BUFFER_BIT);

        if (uiRenderer != null)
        {
            uiRenderer.beginFrame(640.0F, 360.0F, 640.0F, 360.0F);

            if (logoTextureId != -1)
            {
                uiRenderer.drawFramebufferTexture(0.0F, 0.0F, 640.0F, 360.0F, logoTextureId, 1.0F, 1.0F, 1.0F, 1.0F);
            }

            if (uiProgressBar != null)
            {
                uiProgressBar.setProgress(currentProgress);
                uiProgressBar.render(uiRenderer);
            }

            uiRenderer.endFrame();
        }
    }

    private static void cleanup()
    {
        if (logoTextureId != -1)
        {
            GL11C.glDeleteTextures(logoTextureId);
            logoTextureId = -1;
        }
        if (uiRenderer != null)
        {
            uiRenderer.cleanup();
        }
        GLFW.glfwMakeContextCurrent(MemoryUtil.NULL);
    }

    private static int loadTexture(String path)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);
            
            ImageDecoder.setFlipVerticallyOnLoad(true);
            
            ByteBuffer data = ImageDecoder.loadFromFile(path, w, h, comp, 4);
            if (data == null)
            {
                return -1;
            }

            int tex = GL11C.glGenTextures();
            GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, tex);
            GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
            GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
            GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA, w.get(), h.get(), 0, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, data);
            ImageDecoder.freeImage(data);
            return tex;
        }
    }
}
