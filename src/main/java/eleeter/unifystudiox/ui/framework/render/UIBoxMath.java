package eleeter.unifystudiox.ui.framework.render;

/**
 * Pure mathematical utility class for UI bounding box layout, uniform scaling,
 * aspect-ratio fitting, and mouse-based resizing calculations.
 * Completely stateless, decoupled, and containing only pure floating-point math functions.
 */
public class UIBoxMath
{

    /**
     * Calculates the new position/offset coordinate after dragging.
     */
    public static float calculateDrag(float startOffset, float mouseStart, float mouseCurrent)
    {
        return startOffset + (mouseCurrent - mouseStart);
    }

    /**
     * Calculates the new width/height dimension after dragging a resize grip,
     * clamped securely between minimum and maximum bounds.
     */
    public static float calculateResize(float startSize, float mouseStart, float mouseCurrent, float minSize, float maxSize)
    {
        float delta = mouseCurrent - mouseStart;
        return Math.max(minSize, Math.min(maxSize, startSize + delta));
    }

    /**
     * Maps a value from one linear range to another, clamped to the target boundaries.
     */
    public static float mapLinear(float value, float fromMin, float fromMax, float toMin, float toMax)
    {
        if (Math.abs(fromMax - fromMin) < 0.0001F)
        {
            return toMin;
        }
        float progress = (value - fromMin) / (fromMax - fromMin);
        float clampedProgress = Math.max(0.0F, Math.min(1.0F, progress));
        return toMin + clampedProgress * (toMax - toMin);
    }

    /**
     * Calculates the scaling factor required to fit or fill a source size inside a target size
     * while completely preserving the aspect ratio.
     *
     * @param srcW   Source width.
     * @param srcH   Source height.
     * @param dstW   Destination target width.
     * @param dstH   Destination target height.
     * @param fit    If true, fits the entire source inside (letterbox). If false, fills the entire target (crop).
     * @return The uniform aspect ratio scaling factor.
     */
    public static float calculateAspectScale(float srcW, float srcH, float dstW, float dstH, boolean fit)
    {
        if (srcW <= 0.0F || srcH <= 0.0F || dstW <= 0.0F || dstH <= 0.0F)
        {
            return 1.0F;
        }
        float scaleX = dstW / srcW;
        float scaleY = dstH / srcH;
        return fit ? Math.min(scaleX, scaleY) : Math.max(scaleX, scaleY);
    }

    /**
     * Fits a source layout area inside a destination layout area, centering it and
     * preserving its original aspect ratio.
     *
     * @param source The source area to fit and center (modified in-place).
     * @param target The target bounding container area.
     */
    public static void fitAndCenter(Region source, Region target)
    {
        float scale = calculateAspectScale(source.w, source.h, target.w, target.h, true);
        float finalW = source.w * scale;
        float finalH = source.h * scale;
        
        float xOffset = (target.w - finalW) * 0.5F;
        float yOffset = (target.h - finalH) * 0.5F;
        
        source.set(target.x + xOffset, target.y + yOffset, finalW, finalH);
    }

    /**
     * Splits a bounding container area into two sub-areas along a specific axis
     * using a split ratio and a divider thickness, centered at the split boundary.
     *
     * @param container        The parent bounding area to partition.
     * @param ratio            The partition split ratio (0.0F to 1.0F).
     * @param dividerThickness The physical width/height of the divider bar handle.
     * @param horizontal       If true, splits horizontally (left/right columns). If false, splits vertically (top/bottom rows).
     * @param outFirst         The output Area representing the first partition (left/top) (modified in-place).
     * @param outDivider       The output Area representing the splitter bar handle (modified in-place).
     * @param outSecond        The output Area representing the second partition (right/bottom) (modified in-place).
     */
    public static void partition(Region container, float ratio, float dividerThickness, boolean horizontal,
                                 Region outFirst, Region outDivider, Region outSecond)
                                 {
        float clampedRatio = Math.max(0.0F, Math.min(1.0F, ratio));
        float halfThickness = dividerThickness * 0.5F;

        if (horizontal)
        {
            float splitX = container.x + (container.w * clampedRatio);
            
            // First partition (Left column)
            float firstW = Math.max(0.0F, (splitX - halfThickness) - container.x);
            outFirst.set(container.x, container.y, firstW, container.h);
            
            // Divider bar handle
            outDivider.set(container.x + firstW, container.y, dividerThickness, container.h);
            
            // Second partition (Right column)
            float secondX = outDivider.ex();
            float secondW = Math.max(0.0F, container.ex() - secondX);
            outSecond.set(secondX, container.y, secondW, container.h);
        } else
        {
            float splitY = container.y + (container.h * clampedRatio);
            
            // First partition (Top row)
            float firstH = Math.max(0.0F, (splitY - halfThickness) - container.y);
            outFirst.set(container.x, container.y, container.w, firstH);
            
            // Divider bar handle
            outDivider.set(container.x, container.y + firstH, container.w, dividerThickness);
            
            // Second partition (Bottom row)
            float secondY = outDivider.ey();
            float secondH = Math.max(0.0F, container.ey() - secondY);
            outSecond.set(container.x, secondY, container.w, secondH);
        }
    }

    /**
     * Calculates the new split ratio based on the current mouse coordinate along the split axis.
     *
     * @param container  The parent bounding area.
     * @param mouseCoord The current mouse X coordinate (if splitting horizontally) or Y coordinate (if splitting vertically).
     * @param horizontal If true, the split is horizontal. If false, the split is vertical.
     * @return The updated split ratio, clamped securely between 0.0F and 1.0F.
     */
    public static float calculateSplitRatio(Region container, float mouseCoord, boolean horizontal)
    {
        if (horizontal)
        {
            if (container.w <= 0.0F) return 0.5F;
            float ratio = (mouseCoord - container.x) / container.w;
            return Math.max(0.0F, Math.min(1.0F, ratio));
        } else
        {
            if (container.h <= 0.0F) return 0.5F;
            float ratio = (mouseCoord - container.y) / container.h;
            return Math.max(0.0F, Math.min(1.0F, ratio));
        }
    }
}
