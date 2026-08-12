package eleeter.unifystudiox.graphics.gl;

import static org.lwjgl.opengl.GL30C.glBindFramebuffer;
import static org.lwjgl.opengl.GL45C.glBlitNamedFramebuffer;
import static org.lwjgl.opengl.GL45C.glCheckNamedFramebufferStatus;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.ARBDirectStateAccess;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL45C;

public class GlBuffer
{
    public GlBuffer()
    {
    }

    public static int genBuffers()
    {
        return GL15C.glGenBuffers();
    }

    public void deleteBuffers(int buffer)
    {
        GL15C.glDeleteBuffers(buffer);
    }

    public int namedBufferStatus(int i, int buffer)
    {
        return glCheckNamedFramebufferStatus(i, buffer);
    }

    public void namedBuffer(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11)
    {
        glBlitNamedFramebuffer(i, i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11);
    }

    public void bindFrameBuffer(int target, int buffer)
    {
        GL45C.glBindFramebuffer(target, buffer);
    }

    public void fBufferBind(int gl, int handle)
    {
        glBindFramebuffer(gl, handle);
    }

    public void bindBuffer(int target, int buffer)
    {
        GL15C.glBindBuffer(target, buffer);
    }

    public void bufferData(int target, long size, int usage)
    {
        GL15C.glBufferData(target, size, usage);
    }

    public void bufferData(int target, FloatBuffer data, int usage)
    {
        GL15C.glBufferData(target, data, usage);
    }

    public void bufferData(int target, IntBuffer data, int usage)
    {
        GL15C.glBufferData(target, data, usage);
    }

    public void bufferData(int target, ByteBuffer data, int usage)
    {
        GL15C.glBufferData(target, data, usage);
    }

    public void bufferData(int target, short[] data, int usage)
    {
        GL15C.glBufferData(target, data, usage);
    }

    public void bufferData(int target, int[] data, int usage)
    {
        GL15C.glBufferData(target, data, usage);
    }

    public void bufferData(int target, float[] data, int usage)
    {
        GL15C.glBufferData(target, data, usage);
    }

    /* ARB Direct State Access methods */
    public int createBuffers()
    {
        return ARBDirectStateAccess.glCreateBuffers();
    }

    public void namedBufferData(int buffer, FloatBuffer data, int usage)
    {
        ARBDirectStateAccess.glNamedBufferData(buffer, data, usage);
    }

    public void namedBufferData(int buffer, IntBuffer data, int usage)
    {
        ARBDirectStateAccess.glNamedBufferData(buffer, data, usage);
    }

    public void namedBufferData(int buffer, long size, int usage)
    {
        ARBDirectStateAccess.glNamedBufferData(buffer, size, usage);
    }
}
