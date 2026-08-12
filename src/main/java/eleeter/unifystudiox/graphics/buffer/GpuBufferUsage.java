package eleeter.unifystudiox.graphics.buffer;

import org.lwjgl.opengl.GL15C;

public enum GpuBufferUsage
{
    STATIC,
    DYNAMIC,
    STREAMING;

    public int toGlUsage()
    {
        return switch (this)
        {
            case STATIC -> GL15C.GL_STATIC_DRAW;
            case DYNAMIC -> GL15C.GL_DYNAMIC_DRAW;
            case STREAMING -> GL15C.GL_STREAM_DRAW;
        };
    }
}
