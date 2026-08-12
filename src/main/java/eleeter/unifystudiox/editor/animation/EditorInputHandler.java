package eleeter.unifystudiox.editor.animation;

import eleeter.unifystudiox.input.InputHandler;


public class EditorInputHandler extends InputHandler
{
    private eleeter.unifystudiox.ui.framework.render.context.UIInputContext context;
    private float contentX;
    private float contentY;

    public EditorInputHandler(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, float contentX, float contentY)
    {
        super(0L);
        this.context = context;
        this.contentX = contentX;
        this.contentY = contentY;
    }

    @Override
    public void poll()
    {
    }

    @Override
    public void wrapMouse(int width, int height)
    {
    }


    public void update(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, float contentX, float contentY)
    {
        this.context = context;
        this.contentX = contentX;
        this.contentY = contentY;
    }

    @Override
    public double getMouseX()
    {
        return this.context.getMouseX() - this.contentX;
    }

    @Override
    public double getMouseY()
    {
        return this.context.getMouseY() - this.contentY;
    }

    @Override
    public double getMouseDeltaX()
    {
        return this.context.getMouseDeltaX();
    }

    @Override
    public double getMouseDeltaY()
    {
        return this.context.getMouseDeltaY();
    }

    @Override
    public double getScrollDelta()
    {
        return this.context.getScrollDelta();
    }

    @Override
    public boolean isButtonHeld(int btn)
    {
        if (btn == 0)
        {
            return this.context.isMouseDown();
        }
        if (btn == 1)
        {
            return this.context.isRightMouseDown();
        }
        if (btn == 2)
        {
            return this.context.isMiddleMouseDown();
        }
        return false;
    }

    @Override
    public boolean isButtonPressed(int btn)
    {
        if (btn == 0)
        {
            return this.context.isMousePressed();
        }
        if (btn == 1)
        {
            return this.context.isRightMousePressed();
        }
        if (btn == 2)
        {
            return this.context.isMiddleMousePressed();
        }
        return false;
    }

    @Override
    public boolean isButtonReleased(int btn)
    {
        if (btn == 0)
        {
            return this.context.isMouseReleased();
        }
        if (btn == 1)
        {
            return this.context.isRightMouseReleased();
        }
        if (btn == 2)
        {
            return this.context.isMiddleMouseReleased();
        }
        return false;
    }

    @Override
    public boolean isKeyHeld(int glfwKey)
    {
        return this.context.isKeyHeld(glfwKey);
    }

    @Override
    public boolean isKeyPressed(int glfwKey)
    {
        return this.context.isKeyPressed(glfwKey);
    }

    @Override
    public boolean isKeyReleased(int glfwKey)
    {
        return false;
    }


    public boolean isMouseInsideViewport(int vpW, int vpH)
    {
        double relX = this.context.getMouseX() - this.contentX;
        double relY = this.context.getMouseY() - this.contentY;
        return relX >= 0.0D && relX < vpW && relY >= 0.0D && relY < vpH;
    }

}
