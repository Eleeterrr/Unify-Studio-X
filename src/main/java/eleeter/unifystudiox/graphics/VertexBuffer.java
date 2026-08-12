package eleeter.unifystudiox.graphics;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL45C;

import eleeter.unifystudiox.graphics.api.IVertexBuffer;
import eleeter.unifystudiox.graphics.buffer.GpuBufferUsage;

public class VertexBuffer implements IVertexBuffer
{

    private final int handle;
    private final GpuBufferUsage usage;
    private long sizeBytes;

    public VertexBuffer(GpuBufferUsage usage)
    {
        this.handle = GL45C.glCreateBuffers();
        this.usage = usage;
        this.sizeBytes = 0;
    }

    /**
     * Create and upload float data to the GPU.
     */
    public VertexBuffer(float[] data, GpuBufferUsage usage)
    {
        this(usage);
        upload(data);
    }

    /**
     * Create and upload int data to the GPU.
     */
    public VertexBuffer(int[] data, GpuBufferUsage usage)
    {
        this(usage);
        upload(data);
    }

    /**
     * Pre-allocate a block of memory on the GPU without uploading data.
     */
    public void allocate(long size)
    {
        this.sizeBytes = size;
        GL45C.glNamedBufferData(handle, size, usage.toGlUsage());
    }

    /**
     * Upload an array of floats to the buffer.
     */
    public void upload(float[] data)
    {
        this.sizeBytes = (long) data.length * Float.BYTES;
        GL45C.glNamedBufferData(handle, data, usage.toGlUsage());
    }

    /**
     * Upload an array of ints to the buffer.
     */
    public void upload(int[] data)
    {
        this.sizeBytes = (long) data.length * Integer.BYTES;
        GL45C.glNamedBufferData(handle, data, usage.toGlUsage());
    }

    /**
     * Upload a raw byte buffer to the GPU.
     */
    public void upload(ByteBuffer data)
    {
        this.sizeBytes = data.remaining();
        GL45C.glNamedBufferData(handle, data, usage.toGlUsage());
    }

    public void uploadPartial(float[] data, long offsetBytes)
    {
        GL45C.glNamedBufferSubData(handle, offsetBytes, data);
    }


    public void uploadPartial(FloatBuffer data, long offsetBytes)
    {
        GL45C.glNamedBufferSubData(handle, offsetBytes, data);
    }


    public void uploadPartial(IntBuffer data, long offsetBytes)
    {
        GL45C.glNamedBufferSubData(handle, offsetBytes, data);
    }

    public int getHandle()
    {
        return handle;
    }

    public long getSizeBytes()
    {
        return sizeBytes;
    }

    @Override
    public void bind()
    {
        GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, handle);
    }

    @Override
    public void unbind()
    {
        GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void destroy()
    {
        GL15C.glDeleteBuffers(handle);
    }
}
