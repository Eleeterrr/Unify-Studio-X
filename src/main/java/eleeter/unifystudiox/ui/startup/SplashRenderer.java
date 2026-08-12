package eleeter.unifystudiox.ui.startup;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.ui.framework.render.gl.GLUIRenderer;
import eleeter.unifystudiox.ui.widgets.UIProgressBar;


public class SplashRenderer
{

    private TextureGL logoTex;
    private GLUIRenderer uiRenderer;
    private UIProgressBar uiProgressBar;

    public void init()
    {
        GL.createCapabilities();

        logoTex = new TextureGL("assets/mainlogo.png");

        uiRenderer = new GLUIRenderer();
        uiRenderer.init();

        uiProgressBar = new UIProgressBar("splash_progress");
        uiProgressBar.cx = 120;
        uiProgressBar.cy = 320;
        uiProgressBar.cw = 400;
        uiProgressBar.ch = 4;
        uiProgressBar.setTrackColor(0.1f, 0.12f, 0.15f, 1.0f);
        uiProgressBar.setFillColor(0.75f, 0.45f, 0.25f, 1.0f);
    }

    public void render(float currentProgress)
    {
        GL11.glClearColor(0.08f, 0.09f, 0.12f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        uiProgressBar.setProgress(currentProgress);

        uiRenderer.beginFrame(640, 360, 640, 360);

        uiRenderer.drawTexture(0, 0, 640, 360, logoTex, 1.0f, 1.0f, 1.0f, 1.0f);

        uiProgressBar.render(uiRenderer);

        uiRenderer.endFrame();
    }

    public void cleanup()
    {
        if (logoTex != null) logoTex.cleanup();
        if (uiRenderer != null) uiRenderer.cleanup();
    }
}
