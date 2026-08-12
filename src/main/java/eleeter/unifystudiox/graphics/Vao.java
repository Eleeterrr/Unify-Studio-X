package eleeter.unifystudiox.graphics;

import org.lwjgl.opengl.GL45C;

import eleeter.unifystudiox.graphics.api.IVertexArray;
import eleeter.unifystudiox.graphics.gl.GlVertexArray;
import eleeter.unifystudiox.graphics.layout.AttributeDescriptor;
import eleeter.unifystudiox.graphics.layout.BufferLayout;

public class Vao implements IVertexArray
{
    private final int handle;

    public Vao(int handle)
    {
        this.handle = handle;
    }

    /**
     * Bind this array object for rendering.
     */
    public void bind()
    {
        GlVertexArray.bindVertexArray(handle);
    }

    /**
     * Unbind the current array object.
     */
    public void unbind()
    {
        GL45C.glBindVertexArray(0);
    }

    /**
     * Release the GPU resource.
     */
    public void destroy()
    {
        GL45C.glDeleteVertexArrays(handle);
    }

    public int getHandle()
    {
        return handle;
    }

    /**
     * A new builder to construct a GpuArray.
     */
    public static Builder builder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private final int vao = GL45C.glCreateVertexArrays();
        private int nextBindingSlot = 0;


        public Builder bindVertexBuffer(VertexBuffer buffer, BufferLayout layout)
        {
            return bindVertexBuffer(buffer, layout, nextBindingSlot++);
        }

        /**
         * Bind a vertex buffer to a specific binding slot.
         */
        public Builder bindVertexBuffer(VertexBuffer buffer, BufferLayout layout, int binding)
        {
            GL45C.glVertexArrayVertexBuffer(vao, binding, buffer.getHandle(), 0, layout.getStride());

            for (AttributeDescriptor attr : layout.getAttributes())
            {
                GL45C.glEnableVertexArrayAttrib(vao, attr.location());

                if (attr.type().isInteger)
                {
                    GL45C.glVertexArrayAttribIFormat(vao, attr.location(), attr.components(), attr.type().glType, attr.offsetBytes());
                } else
                {
                    GL45C.glVertexArrayAttribFormat(vao, attr.location(), attr.components(), attr.type().glType, false, attr.offsetBytes());
                }

                GL45C.glVertexArrayAttribBinding(vao, attr.location(), binding);

                if (attr.divisor() > 0)
                {
                    GL45C.glVertexArrayBindingDivisor(vao, binding, attr.divisor());
                }
            }

            return this;
        }

        /**
         * Attach an element buffer to this array object.
         */
        public Builder elementBuffer(VertexBuffer buffer)
        {
            GL45C.glVertexArrayElementBuffer(vao, buffer.getHandle());
            return this;
        }

        public Vao build()
        {
            return new Vao(vao);
        }
    }
}
