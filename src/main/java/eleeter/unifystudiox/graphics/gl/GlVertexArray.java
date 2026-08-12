package eleeter.unifystudiox.graphics.gl;

import static org.lwjgl.opengl.GL30C.glBindVertexArray;

import org.lwjgl.opengl.ARBDirectStateAccess;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

public class GlVertexArray
{

    private GlVertexArray()
    {
    }
    public void bindVao(int handle)
    {
        glBindVertexArray(handle);
    }

    public static int genVertexArrays()
    {
        return GL30C.glGenVertexArrays();
    }

    public static void deleteVertexArrays(int array)
    {
        GL30C.glDeleteVertexArrays(array);
    }

    public static void bindVertexArray(int array)
    {
        GL30C.glBindVertexArray(array);
    }

    public static void enableVertexAttribArray(int index)
    {
        GL20C.glEnableVertexAttribArray(index);
    }

    public static void disableVertexAttribArray(int index)
    {
        GL20C.glDisableVertexAttribArray(index);
    }

    public static void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointer)
    {
        GL20C.glVertexAttribPointer(index, size, type, normalized, stride, pointer);
    }

    public static int createVertexArrays()
    {
        return ARBDirectStateAccess.glCreateVertexArrays();
    }

    public static void enableVertexArrayAttrib(int vaobj, int index)
    {
        ARBDirectStateAccess.glEnableVertexArrayAttrib(vaobj, index);
    }

    public static void vertexArrayAttribBinding(int vaobj, int attribindex, int bindingindex)
    {
        ARBDirectStateAccess.glVertexArrayAttribBinding(vaobj, attribindex, bindingindex);
    }

    public static void vertexArrayAttribFormat(int vaobj, int attribindex, int size, int type, boolean normalized, int relativeoffset)
    {
        ARBDirectStateAccess.glVertexArrayAttribFormat(vaobj, attribindex, size, type, normalized, relativeoffset);
    }

    public static void vertexArrayElementBuffer(int vaobj, int buffer)
    {
        ARBDirectStateAccess.glVertexArrayElementBuffer(vaobj, buffer);
    }

    public static void vertexArrayVertexBuffer(int vaobj, int bindingindex, int buffer, long offset, int stride)
    {
        ARBDirectStateAccess.glVertexArrayVertexBuffer(vaobj, bindingindex, buffer, offset, stride);
    }
}
