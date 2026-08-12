package eleeter.unifystudiox.util.log;

import static org.lwjgl.opengl.GL11.*;

public class GPUInfo
{
    public static String getRenderer()
    {
        return glGetString(GL_RENDERER);
    }

    public static String getVendor()
    {
        return glGetString(GL_VENDOR);
    }

    public static String getVersion()
    {
        return glGetString(GL_VERSION);
    }
}
