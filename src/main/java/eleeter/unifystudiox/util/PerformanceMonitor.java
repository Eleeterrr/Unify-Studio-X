package eleeter.unifystudiox.util;

import java.lang.management.ManagementFactory;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.sun.management.OperatingSystemMXBean;

public class PerformanceMonitor
{
    private static final int ROLLING_SAMPLES = 20;
    private final float[] frameTimes = new float[ROLLING_SAMPLES];
    private int sampleIndex = 0;

    private float currentFps = 0;
    private float averageFrameTime = 0;
    private String gpuInfo = "Unknown";
    private String gpuVendor = "Unknown";

    private final OperatingSystemMXBean osBean;
    private boolean gpuInfoInitialized = false;
    private double lastTimestamp = -1;

    public PerformanceMonitor()
    {
        this.osBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    private void ensureGpuInfo()
    {
        if (!this.gpuInfoInitialized)
        {
            try
            {
                this.gpuInfo = GL11.glGetString(GL11.GL_RENDERER);
                this.gpuVendor = GL11.glGetString(GL11.GL_VENDOR);
                this.gpuInfoInitialized = true;
            } catch (Exception e)
            {
            }
        }
    }

    /**
     * Records a frame.
     */
    public void recordFrame()
    {
        this.ensureGpuInfo();

        double currentTimestamp = GLFW.glfwGetTime();
        if (this.lastTimestamp != -1)
        {
            float deltaTime = (float) (currentTimestamp - this.lastTimestamp);

            this.frameTimes[this.sampleIndex] = deltaTime;
            this.sampleIndex = (this.sampleIndex + 1) % ROLLING_SAMPLES;

            float totalTime = 0;
            for (float time : this.frameTimes)
            {
                totalTime += time;
            }

            this.averageFrameTime = (totalTime / ROLLING_SAMPLES) * 1000.0f;
            this.currentFps = totalTime > 0 ? ROLLING_SAMPLES / totalTime : 0;
        }

        this.lastTimestamp = currentTimestamp;
    }

    public float getFps()
    {
        return this.currentFps;
    }

    public float getFrameTimeMs()
    {
        return this.averageFrameTime;
    }

    public float getCpuLoad()
    {
        return (float) (this.osBean.getProcessCpuLoad() * 100.0);
    }

    public long getUsedMemoryMb()
    {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }

    public long getMaxMemoryMb()
    {
        return Runtime.getRuntime().maxMemory() / (1024 * 1024);
    }

    public String getGpuInfo()
    {
        return this.gpuInfo;
    }

    public String getGpuVendor()
    {
        return this.gpuVendor;
    }


}
