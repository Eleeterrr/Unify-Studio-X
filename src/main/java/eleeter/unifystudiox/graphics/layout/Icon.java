package eleeter.unifystudiox.graphics.layout;

import static org.lwjgl.glfw.GLFW.glfwSetWindowIcon;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;

import eleeter.unifystudiox.graphics.stb.ImageDecoder;

public class Icon
{
    public Icon(long handle, String path)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);
            ByteBuffer image = ImageDecoder.loadFromFile(path, w, h, comp, 4);
            if (image == null)
            {
                System.err.println("Failed to load icon: " + ImageDecoder.getFailureReason());
                return;
            }
            GLFWImage icon = GLFWImage.malloc(stack);
            icon.set(w.get(0), h.get(0), image);
            GLFWImage.Buffer iconBuffer = GLFWImage.malloc(1, stack);
            iconBuffer.put(0, icon);
            glfwSetWindowIcon(handle, iconBuffer);
            ImageDecoder.freeImage(image);
        }

    }
}
