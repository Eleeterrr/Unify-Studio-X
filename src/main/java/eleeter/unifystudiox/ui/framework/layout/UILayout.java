package eleeter.unifystudiox.ui.framework.layout;

import java.util.HashMap;
import java.util.Map;

import eleeter.unifystudiox.ui.framework.UIElement;

public class UILayout extends UILayoutContainer
{
    private Axis axis = Axis.HORIZONTAL;
    private Alignment justifyContent = Alignment.START;
    private Alignment alignItems = Alignment.STRETCH;

    private static class ChildConfig
    {
        float grow = 0.0F;
        float shrink = 1.0F;
        float basis = -1.0F; /* -1 means use pixel width/height */
    }

    private final Map<UIElement, ChildConfig> configs = new HashMap<>();

    public UILayout(String id)
    {
        super(id);
    }

    public UILayout(String id, Axis axis)
    {
        super(id);
        this.axis = axis;
    }

    public void setJustifyContent(Alignment alignment)
    {
        this.justifyContent = alignment;
        this.markDirty();
    }

    public void setAlignItems(Alignment alignment)
    {
        this.alignItems = alignment;
        this.markDirty();
    }

    public void setFlex(UIElement child, float grow, float shrink)
    {
        ChildConfig config = this.configs.computeIfAbsent(child, k -> new ChildConfig());
        config.grow = grow;
        config.shrink = shrink;
        this.markDirty();
    }

    @Override
    public void addChild(UIElement child)
    {
        this.addChild(child, 0.0F);
    }

    public void addChild(UIElement child, float grow)
    {
        super.addChild(child);
        this.setFlex(child, grow, 1.0F);
    }

    @Override
    public void removeChild(UIElement child)
    {
        super.removeChild(child);
        this.configs.remove(child);
    }

    @Override
    public void clearChildren()
    {
        super.clearChildren();
        this.configs.clear();
    }

    @Override
    protected void performLayout(float x, float y, float w, float h)
    {
        float totalPreferredSize = 0.0F;
        float totalGrowFactor = 0.0F;
        float totalShrinkFactor = 0.0F;
        int visibleCount = 0;

        for (UIElement child : this.getChildren())
        {
            if (!child.isVisible())
            {
                continue;
            }
            visibleCount++;

            ChildConfig config = this.configs.getOrDefault(child, new ChildConfig());
            float preferred = (this.axis == Axis.HORIZONTAL) ? child.getTransform().getPixelWidth() : child.getTransform().getPixelHeight();
            
            if (config.basis >= 0.0F)
            {
                preferred = config.basis;
            }

            totalPreferredSize += preferred;
            totalGrowFactor += config.grow;
            totalShrinkFactor += (preferred * config.shrink);
        }

        float gaps = (visibleCount > 1) ? (visibleCount - 1) * this.gap : 0.0F;
        float containerSize = (this.axis == Axis.HORIZONTAL) ? w : h;
        float remainingSpace = containerSize - totalPreferredSize - gaps;

        float currentPos = 0.0F;

        float extraGap = 0.0F;
        if (remainingSpace > 0 && totalGrowFactor == 0)
        {
            if (this.justifyContent == Alignment.CENTER)
            {
                currentPos = remainingSpace / 2.0F;
            }
            else if (this.justifyContent == Alignment.END)
            {
                currentPos = remainingSpace;
            }
            else if (this.justifyContent == Alignment.SPACE_BETWEEN && visibleCount > 1)
            {
                extraGap = remainingSpace / (visibleCount - 1);
            }
            else if (this.justifyContent == Alignment.SPACE_AROUND)
            {
                extraGap = remainingSpace / visibleCount;
                currentPos = extraGap / 2.0F;
            }
        }

        for (UIElement child : this.getChildren())
        {
            if (!child.isVisible())
            {
                continue;
            }

            ChildConfig config = this.configs.getOrDefault(child, new ChildConfig());
            float preferred = (this.axis == Axis.HORIZONTAL) ? (config.basis >= 0.0F ? config.basis : child.getTransform().getPixelWidth()) : (config.basis >= 0.0F ? config.basis : child.getTransform().getPixelHeight());

            float childMainSize = preferred;

            if (remainingSpace > 0 && totalGrowFactor > 0)
            {
                childMainSize += (config.grow / totalGrowFactor) * remainingSpace;
            }
            else if (remainingSpace < 0 && totalShrinkFactor > 0)
            {
                float shrinkWeight = (preferred * config.shrink) / totalShrinkFactor;
                childMainSize += shrinkWeight * remainingSpace;
            }

            float crossOffset = 0.0F;
            float childCrossSize = (this.axis == Axis.HORIZONTAL) ? h : w;

            if (this.alignItems != Alignment.STRETCH)
            {
                float naturalCross = (this.axis == Axis.HORIZONTAL) ? child.getTransform().getPixelHeight() : child.getTransform().getPixelWidth();
                
                childCrossSize = naturalCross;
                
                float maxCross = (this.axis == Axis.HORIZONTAL) ? h : w;
                if (this.alignItems == Alignment.CENTER)
                {
                    crossOffset = (maxCross - naturalCross) / 2.0F;
                }
                else if (this.alignItems == Alignment.END)
                {
                    crossOffset = maxCross - naturalCross;
                }
            }

            if (this.axis == Axis.HORIZONTAL)
            {
                child.getTransform().set(0.0F, 0.0F, 0.0F, 0.0F).setAnchor(0.0F, 0.0F).setPixelOffset((int) (x + currentPos), (int) (y + crossOffset)).setPixelSize(childMainSize, childCrossSize);
            }
            else
            {
                child.getTransform().set(0.0F, 0.0F, 0.0F, 0.0F).setAnchor(0.0F, 0.0F).setPixelOffset((int) (x + crossOffset), (int) (y + currentPos)).setPixelSize(childCrossSize, childMainSize);
            }

            currentPos += childMainSize + this.gap + extraGap;
        }
    }
}
