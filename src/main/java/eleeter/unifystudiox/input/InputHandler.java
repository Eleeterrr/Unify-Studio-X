package eleeter.unifystudiox.input;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwGetMouseButton;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

public class InputHandler
{

    private static final int KEY_COUNT = 349;
    private static final int BUTTON_COUNT = 8;

    private final long windowHandle;

    private final boolean[] keysHeld = new boolean[KEY_COUNT];
    private final boolean[] keysPressed = new boolean[KEY_COUNT];
    private final boolean[] keysReleased = new boolean[KEY_COUNT];
    private final boolean[] keysPrev = new boolean[KEY_COUNT];

    private final boolean[] buttonsHeld = new boolean[BUTTON_COUNT];
    private final boolean[] buttonsPressed = new boolean[BUTTON_COUNT];
    private final boolean[] buttonsReleased = new boolean[BUTTON_COUNT];
    private final boolean[] buttonsPrev = new boolean[BUTTON_COUNT];

    private double mouseX, mouseY;
    private double mouseDeltaX, mouseDeltaY;
    private double scrollDelta;
    private boolean skipNextDelta = false;
    private boolean wrappedThisFrame = false;
    private double pendingScrollDelta = 0.0;
    
    private final StringBuilder textInputBuffer = new StringBuilder();
    private final Object textInputLock = new Object();

    public InputHandler(long windowHandle)
    {
        this.windowHandle = windowHandle;
        if (windowHandle != 0L)
        {
            registerScrollCallback();
            registerCharCallback();
        }
    }

    public void poll()
    {
        this.wrappedThisFrame = false;
        this.scrollDelta = this.pendingScrollDelta;
        this.pendingScrollDelta = 0.0;

        for (int key = GLFW_KEY_SPACE; key < KEY_COUNT; key++)
        {
            boolean current = glfwGetKey(this.windowHandle, key) == GLFW_PRESS;
            this.keysPressed [key] = current && !this.keysPrev[key];
            this.keysReleased[key] = !current && this.keysPrev[key];
            this.keysHeld [key] = current;
            this.keysPrev [key] = current;
        }

        for (int btn = 0; btn < BUTTON_COUNT; btn++)
        {
            boolean current = glfwGetMouseButton(this.windowHandle, btn) == GLFW_PRESS;
            this.buttonsPressed [btn] = current && !this.buttonsPrev[btn];
            this.buttonsReleased[btn] = !current && this.buttonsPrev[btn];
            this.buttonsHeld [btn] = current;
            this.buttonsPrev [btn] = current;
        }

        double[] xArr = new double[1], yArr = new double[1];
        glfwGetCursorPos(this.windowHandle, xArr, yArr);

        if (this.skipNextDelta)
        {
            this.mouseDeltaX = 0;
            this.mouseDeltaY = 0;
            this.mouseX = xArr[0];
            this.mouseY = yArr[0];
            this.skipNextDelta = false;
        }
        else
        {
            this.mouseDeltaX = xArr[0] - this.mouseX;
            this.mouseDeltaY = yArr[0] - this.mouseY;
            this.mouseX = xArr[0];
            this.mouseY = yArr[0];
        }
    }

    public boolean wasWrappedThisFrame()
    {
        return this.wrappedThisFrame;
    }

    public void wrapMouse(int width, int height)
    {
        boolean dragging = false;
        for (int i = 0; i < BUTTON_COUNT; i++)
        {
            if (buttonsHeld[i])
            {
                dragging = true;
                break;
            }
        }
        if (!dragging)
        {
            return;
        }

        double margin = 5.0;
        double nx = mouseX;
        double ny = mouseY;
        boolean wrapped = false;

        if (mouseX < margin)
        {
            nx = width - margin - 1;
            wrapped = true;
        }
        else if (mouseX > width - margin)
        {
            nx = margin + 1;
            wrapped = true;
        }

        if (mouseY < margin)
        {
            ny = height - margin - 1;
            wrapped = true;
        }
        else if (mouseY > height - margin)
        {
            ny = margin + 1;
            wrapped = true;
        }

        if (wrapped)
        {
            glfwSetCursorPos(this.windowHandle, nx, ny);
            this.mouseX = nx;
            this.mouseY = ny;
            this.skipNextDelta = true;
            this.wrappedThisFrame = true;
        }
    }


    public boolean isKeyHeld(int glfwKey)
    {
        return this.keysHeld[glfwKey];
    }

    public boolean isKeyPressed(int glfwKey)
    {
        return this.keysPressed [glfwKey];
    }

    public boolean isKeyReleased(int glfwKey)
    {
        return this.keysReleased[glfwKey];
    }

    public boolean isButtonHeld(int btn)
    {
        return this.buttonsHeld[btn];
    }

    public boolean isButtonPressed (int btn)
    {
        return this.buttonsPressed[btn];
    }

    public boolean isButtonReleased(int btn)
    {
        return this.buttonsReleased[btn];
    }

    public double getMouseX()
    {
        return this.mouseX;
    }

    public double getMouseY()
    {
        return this.mouseY;
    }

    public double getMouseDeltaX()
    {
        return this.mouseDeltaX;
    }

    public double getMouseDeltaY()
    {
        return this.mouseDeltaY;
    }

    public double getScrollDelta()
    {
        return this.scrollDelta;
    }

    private void registerScrollCallback()
    {
        glfwSetScrollCallback(this.windowHandle, (win, xOffset, yOffset) ->
        {
            this.pendingScrollDelta += yOffset;
        });
    }

    private void registerCharCallback()
    {
        glfwSetCharCallback(this.windowHandle, (win, codepoint) ->
        {
            synchronized (this.textInputLock)
            {
                this.textInputBuffer.append((char) codepoint);
            }
        });
    }

    public String consumeTextInput()
    {
        synchronized (this.textInputLock)
        {
            String text = this.textInputBuffer.toString();
            this.textInputBuffer.setLength(0);
            return text;
        }
    }

    public void captureCursor()
    {
        glfwSetInputMode(this.windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    public void releaseCursor()
    {
        glfwSetInputMode(this.windowHandle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }
}
