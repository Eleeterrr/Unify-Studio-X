package eleeter.unifystudiox.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.lwjgl.system.MemoryUtil;

import eleeter.unifystudiox.graphics.Framebuffer;
import eleeter.unifystudiox.renderer.Renderer;
import eleeter.unifystudiox.scene.Scene;
import eleeter.unifystudiox.util.log.AniLogger;

public class ScreenshotCapture
{
    private static final int DEFAULT_CAPTURE_WIDTH = 3840;
    private static final int DEFAULT_CAPTURE_HEIGHT = 2160;

    private int captureWidth = DEFAULT_CAPTURE_WIDTH;
    private int captureHeight = DEFAULT_CAPTURE_HEIGHT;
    private boolean captureRequested = false;

    public void init()
    {
    }

    public void requestCapture()
    {
        this.captureRequested = true;
    }

    public boolean isCaptureRequested()
    {
        return this.captureRequested;
    }

    public void setResolution(int width, int height)
    {
        this.captureWidth = width;
        this.captureHeight = height;
    }


    public void processPendingCaptures(Scene scene, Renderer renderer)
    {
        if (!this.captureRequested)
        {
            return;
        }

        this.captureRequested = false;

        AniLogger.info("ScreenshotRecorder", "Capturing at " + this.captureWidth + "x" + this.captureHeight);

        Framebuffer resolveFb = renderer.renderForScreenshot(scene, this.captureWidth, this.captureHeight);

        ByteBuffer buffer = MemoryUtil.memAlloc(this.captureWidth * this.captureHeight * 4);
        resolveFb.readPixels(0, 0, this.captureWidth, this.captureHeight, buffer);
        resolveFb.destroy();

        this.save(buffer, this.captureWidth, this.captureHeight);
    }

    public void destroy()
    {
    }

    private void save(ByteBuffer buffer, int width, int height)
    {
        new Thread(() ->
        {
            try
            {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File directory = new File("captures");
                if (!directory.exists())
                {
                    directory.mkdirs();
                }
                File file = new File(directory, "screenshot_" + timestamp + ".png");

                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                int[] pixels = new int[width * height];

                for (int y = 0; y < height; y++)
                {
                    for (int x = 0; x < width; x++)
                    {
                        int i = (x + (height - y - 1) * width) * 4;
                        int r = buffer.get(i) & 0xFF;
                        int g = buffer.get(i + 1) & 0xFF;
                        int b = buffer.get(i + 2) & 0xFF;
                        int a = buffer.get(i + 3) & 0xFF;
                        pixels[x + y * width] = (a << 24) | (r << 16) | (g << 8) | b;
                    }
                }
                image.setRGB(0, 0, width, height, pixels, 0, width);

                ImageIO.write(image, "png", file);
                AniLogger.info("ScreenshotRecorder", "Saved: " + file.getAbsolutePath());
            }
            catch (Exception e)
            {
                AniLogger.error("ScreenshotRecorder", "Async capture failed: " + e.getMessage());
            }
            finally
            {
                MemoryUtil.memFree(buffer);
            }
        }, "Screenshot-Worker").start();
    }
}
