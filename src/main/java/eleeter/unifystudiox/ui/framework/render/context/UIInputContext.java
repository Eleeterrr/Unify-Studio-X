package eleeter.unifystudiox.ui.framework.render.context;

import eleeter.unifystudiox.graphics.math.TransformStack;
import eleeter.unifystudiox.input.InputHandler;
import eleeter.unifystudiox.ui.Batcher;
import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.render.UIRootPanel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

public class UIInputContext
{

    /* Raw mouse snapshot */
    private float mouseX;
    private float mouseY;
    private boolean mousePressed;
    private boolean mouseDown;
    private boolean mouseReleased;
    private float scrollDelta;
    private float mouseDeltaX;
    private float mouseDeltaY;

    /* Middle mouse button state */
    private boolean middleMousePressed;
    private boolean middleMouseDown;
    private boolean middleMouseReleased;
    /* Right mouse button state */
    private boolean rightMousePressed;
    private boolean rightMouseDown;
    private boolean rightMouseReleased;

    /* Transient InputHandler for polling other states */
    private InputHandler currentInputHandler;
    private String textInputThisFrame = "";

    /* Pixels the mouse must move while held before dragging is detected. */
    private static final float DRAG_THRESHOLD = 4f;

    private float dragStartX;
    private float dragStartY;
    private boolean dragging;

    /*
     * The element that received the initial press.
     * Drag ownership stays locked to this element until release.
     */
    private UIElement pressedElement;
    private UIElement hoveredElement;
    private final List<UIElement> hitList = new ArrayList<>();

    private long lastClickTime = 0;
    private UIElement lastClickedElement = null;
    private boolean doubleClickedThisFrame = false;

    public void processInput(InputHandler input, UIRootPanel root, UIElement overlay)
    {
        this.currentInputHandler = input;
        this.textInputThisFrame = input.consumeTextInput();
        this.mouseDeltaX = (float) input.getMouseDeltaX();
        this.mouseDeltaY = (float) input.getMouseDeltaY();
        this.mouseX = (float) input.getMouseX();
        this.mouseY = (float) input.getMouseY();
        this.mousePressed = input.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT);
        this.mouseDown = input.isButtonHeld(GLFW_MOUSE_BUTTON_LEFT);
        this.mouseReleased = input.isButtonReleased(GLFW_MOUSE_BUTTON_LEFT);
        this.scrollDelta = (float) input.getScrollDelta();

        this.middleMousePressed = input.isButtonPressed(GLFW_MOUSE_BUTTON_MIDDLE);
        this.middleMouseDown = input.isButtonHeld(GLFW_MOUSE_BUTTON_MIDDLE);
        this.middleMouseReleased = input.isButtonReleased(GLFW_MOUSE_BUTTON_MIDDLE);

        this.rightMousePressed = input.isButtonPressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        this.rightMouseDown = input.isButtonHeld(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        this.rightMouseReleased = input.isButtonReleased(GLFW.GLFW_MOUSE_BUTTON_RIGHT);

        this.doubleClickedThisFrame = false;
        if (mousePressed)
        {
            this.dragStartX = this.mouseX;
            this.dragStartY = this.mouseY;
            this.dragging = false;
            this.pressedElement = this.hoveredElement;

            if (this.hoveredElement != null)
            {
                UIElement target = this.hoveredElement;
                while (target != null && target.getParent() != null && !(target.getParent() instanceof UIRootPanel))
                {
                    target = target.getParent();
                }

                if (target != null && target.getParent() instanceof UIRootPanel && target.getZIndex() < 1000)
                {
                    UIRootPanel rootPanel = (UIRootPanel) target.getParent();
                    List<UIElement> siblings = new ArrayList<>();
                    for (UIElement sibling : rootPanel.getChildren())
                    {
                        if (sibling.getZIndex() < 1000)
                        {
                            siblings.add(sibling);
                        }
                    }

                    siblings.sort(Comparator.comparingInt(UIElement::getZIndex));

                    siblings.remove(target);
                    siblings.add(target);

                    int baseZ = 10;
                    for (int i = 0; i < siblings.size(); i++)
                    {
                        siblings.get(i).setZIndex(baseZ + i);
                    }
                }
            }

            long now = System.currentTimeMillis();
            if (this.pressedElement != null && this.pressedElement == this.lastClickedElement && (now - this.lastClickTime) < 500)
            {
                this.doubleClickedThisFrame = true;
                this.lastClickTime = 0;
            } else
            {
                this.lastClickTime = now;
                this.lastClickedElement = this.pressedElement;
            }
        }

        if (this.mouseDown && !this.dragging)
        {
            float dx = mouseX - this.dragStartX;
            float dy = mouseY - this.dragStartY;
            if (dx * dx + dy * dy > DRAG_THRESHOLD * DRAG_THRESHOLD)
            {
                this.dragging = true;
            }
        }

        if (!this.mouseDown)
        {
            this.dragging = false;
            this.pressedElement = null;
        }

        this.hitList.clear();

        if (overlay != null)
        {
            overlay.collectInteractable(this.hitList);
        }
        root.collectInteractable(this.hitList);

        this.hitList.sort(Comparator.comparingInt(UIElement::getZIndex).reversed());

        if (this.mouseDown && this.pressedElement != null)
        {
            this.hoveredElement = this.pressedElement;
        } else
        {
            this.hoveredElement = null;
            for (UIElement e : this.hitList)
            {
                if (e.containsPoint(this.mouseX, this.mouseY))
                {
                    this.hoveredElement = e;
                    break;
                }
            }
        }
    }


    /**
     * Returns True on the single frame LMB transitions to pressed.
     */
    public boolean isMousePressed()
    {
        return this.mousePressed;
    }

    /**
     * Returns True while LMB is held down.
     */
    public boolean isMouseDown()
    {
        return this.mouseDown;
    }

    /**
     * Returns True on the single frame LMB is released.
     */
    public boolean isMouseReleased()
    {
        return this.mouseReleased;
    }

    /**
     * Returns cursor X in screen-space pixels (logical coordinates).
     */
    public float getMouseX()
    {
        return this.mouseX;
    }

    /**
     * Returns cursor Y in screen-space pixels (logical coordinates).
     */
    public float getMouseY()
    {
        return this.mouseY;
    }

    /**
     * Returns True once the mouse has moved past DRAG_THRESHOLD pixels while LMB is
     * held.
     */
    public boolean isMouseDragging()
    {
        return this.dragging;
    }

    /**
     * Returns True when the scroll wheel produced input this frame.
     */
    public boolean isScrolling()
    {
        return Math.abs(this.scrollDelta) > 0.001f;
    }

    /**
     * Returns raw vertical scroll amount this frame (positive = scroll up).
     */
    public float getScrollDelta()
    {
        return this.scrollDelta;
    }

    public float getMouseDeltaX()
    {
        return this.mouseDeltaX;
    }

    public float getMouseDeltaY()
    {
        return this.mouseDeltaY;
    }

    /**
     * Returns True on the single frame the middle mouse button transitions to pressed.
     */
    public boolean isMiddleMousePressed()
    {
        return this.middleMousePressed;
    }

    /**
     * Returns True while the middle mouse button is held down.
     */
    public boolean isMiddleMouseDown()
    {
        return this.middleMouseDown;
    }

    /**
     * Returns True on the single frame the middle mouse button is released.
     */
    public boolean isMiddleMouseReleased()
    {
        return this.middleMouseReleased;
    }

    /**
     * Returns True on the single frame the right mouse button transitions to pressed.
     */
    public boolean isRightMousePressed()
    {
        return this.rightMousePressed;
    }

    /**
     * Returns True while the right mouse button is held down.
     */
    public boolean isRightMouseDown()
    {
        return this.rightMouseDown;
    }

    /**
     * Returns True on the single frame the right mouse button is released.
     */
    public boolean isRightMouseReleased()
    {
        return this.rightMouseReleased;
    }

    public String consumeTextInput()
    {
        return this.textInputThisFrame;
    }

    public boolean isKeyPressed(int glfwKey)
    {
        return this.currentInputHandler != null && this.currentInputHandler.isKeyPressed(glfwKey);
    }

    public boolean isKeyHeld(int glfwKey)
    {
        return this.currentInputHandler != null && this.currentInputHandler.isKeyHeld(glfwKey);
    }

    public boolean isDoubleClicked(UIElement element)
    {
        return this.doubleClickedThisFrame && this.pressedElement == element;
    }

    public void captureCursor()
    {
        if (this.currentInputHandler != null)
            this.currentInputHandler.captureCursor();
    }

    public void releaseCursor()
    {
        if (this.currentInputHandler != null)
            this.currentInputHandler.releaseCursor();
    }

    /**
     * Delegates to the element's own flag — does not do any computation.
     */
    public boolean isVisible(UIElement e)
    {
        return e.isVisible();
    }

    /**
     * Delegates to the element's own flag — does not do any computation.
     */
    public boolean isEnabled(UIElement e)
    {
        return e.isEnabled();
    }

    /**
     * Returns True if element is the topmost interactable element under the cursor
     * this frame.
     */
    public boolean isHovered(UIElement e)
    {
        return this.hoveredElement == e;
    }

    /**
     * Returns True on the single frame LMB was pressed while element was hovered.
     * Ensures the press was actually initiated on this element (prevents click-through).
     */
    public boolean isClicked(UIElement e)
    {
        return isHovered(e) && this.mousePressed && this.pressedElement == e;
    }

    /**
     * Returns True while LMB is held and element is the hover target.
     */
    public boolean isHeld(UIElement e)
    {
        return isHovered(e) && this.mouseDown;
    }

    /**
     * Returns True while a drag gesture is active that was initiated inside the
     * element.
     */
    public boolean isDragging(UIElement e)
    {
        return this.dragging && this.pressedElement == e;
    }

    /**
     * Returns True if any visible+enabled element is under the cursor.
     */
    public boolean isAnyUIHovered()
    {
        return this.hoveredElement != null;
    }

    /**
     * Returns True if a drag gesture is currently owned by a UI element.
     */
    public boolean isAnyUIActive()
    {
        return this.dragging && this.pressedElement != null;
    }

    public boolean isUIBlockingInput()
    {
        return isAnyUIHovered() || isAnyUIActive();
    }

    /**
     * Returns True if the currently hovered element is the given root.
     */
    public boolean isHoveredWithin(UIElement root)
    {
        UIElement e = this.hoveredElement;
        while (e != null)
        {
            if (e == root)
            {
                return true;
            }
            e = e.getParent();
        }
        return false;
    }

    public UIElement getHoveredElement()
    {
        return this.hoveredElement;
    }

    private final Batcher batcher = new Batcher(new TransformStack());

    public Batcher getBatcher()
    {
        return this.batcher;
    }
}
