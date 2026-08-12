package eleeter.unifystudiox.ui.overlay;

import java.util.ArrayList;
import java.util.List;

import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.widgets.UILabel;
import eleeter.unifystudiox.util.PerformanceMonitor;


public class UIDebugOverlay extends UIElement
{
    private final PerformanceMonitor monitor;
    private final List<UILabel> labels = new ArrayList<>();
    private float updateTimer = 0.0f;

    public UIDebugOverlay(PerformanceMonitor monitor)
    {
        super("debug_overlay");
        this.monitor = monitor;

        this.getTransform().set(0.015f, 0.015f, 0.24f, 0.20f).setAnchor(0f, 0f);

        this.setVisible(false);
        this.initializeLabels();
    }

    private void initializeLabels()
    {
        String[] titles = { "FPS", "Frame Time", "CPU Load", "Memory", "GPU", "Vendor" };
        float lineRelHeight = 1.0f / (titles.length + 2);

        for (int i = 0; i < titles.length; i++)
        {
            UILabel label = new UILabel(this.getId() + "_line_" + i);
            label.setAlignment(UILabel.Align.LEFT);
            label.setTextColor(1.0f, 1.0f, 1.0f, 1.0f);

            label.getTransform().set(0.05f, lineRelHeight * (i + 1), 0.9f, lineRelHeight);

            this.labels.add(label);
            this.addChild(label);
        }
    }

    @Override
    public void updateLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        if (context.isKeyPressed(292))
        {
            this.toggleVisibility();
            this.markDirty();
        }

        super.updateLogic(context, deltaTime);
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        if (!this.isVisible())
            return;

        this.updateTimer += (float) deltaTime;
        if (this.updateTimer >= 0.1f)
        {
            this.refreshMetrics();
            this.updateTimer = 0.0f;
        }
    }

    @Override
    public void updateLayout(float parentX, float parentY, float parentW, float parentH)
    {
        super.updateLayout(parentX, parentY, parentW, parentH);
    }

    private void refreshMetrics()
    {
        this.labels.get(0).setText(String.format("FPS: %.1f", this.monitor.getFps()));
        this.labels.get(1).setText(String.format("Frame: %.2f ms", this.monitor.getFrameTimeMs()));
        this.labels.get(2).setText(String.format("CPU: %.1f%%", this.monitor.getCpuLoad()));
        this.labels.get(3).setText(String.format("Mem: %d / %d MB", this.monitor.getUsedMemoryMb(), this.monitor.getMaxMemoryMb()));

        String gpu = this.monitor.getGpuInfo();
        if (gpu.length() > 40) gpu = gpu.substring(0, 37) + "...";

        this.labels.get(4).setText("GPU: " + gpu);

        String vendor = this.monitor.getGpuVendor();
        this.labels.get(5).setText("Vendor: " + vendor);
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        this.monitor.recordFrame();

        if (!this.isVisible())
            return;

        float x = this.getComputedX();
        float y = this.getComputedY();
        float w = this.getComputedWidth();
        float h = this.getComputedHeight();

    }


    public void toggleVisibility()
    {
        this.setVisible(!this.isVisible());
    }
}
