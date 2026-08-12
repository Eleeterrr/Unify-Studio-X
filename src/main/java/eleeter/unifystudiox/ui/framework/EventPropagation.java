package eleeter.unifystudiox.ui.framework;

/**
 * Defines how input gestures (mouse and keyboard events) cascade down or block
 * throughout the active UI element hierarchy.
 */
public enum EventPropagation
{
    /**
     * Cascade the input events down through children and backgrounds naturally.
     */
    PASS,

    /**
     * Stop propagation entirely, preventing sibling or parent interactions.
     */
    BLOCK,

    /**
     * Stop propagation only if the interaction lies inside this element's physical area.
     */
    BLOCK_INSIDE
}
