package eleeter.unifystudiox.graphics.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BufferLayout
{
    private final List<AttributeDescriptor> attributes;
    private final int strideBytes;

    private BufferLayout(List<AttributeDescriptor> attributes, int strideBytes)
    {
        this.attributes = Collections.unmodifiableList(attributes);
        this.strideBytes = strideBytes;
    }

    public List<AttributeDescriptor> getAttributes()
    {
        return this.attributes;
    }

    public int getStride()
    {
        return this.strideBytes;
    }


    public static Builder builder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private final List<AttributeDescriptor> attrs = new ArrayList<>();
        private int cursor = 0;


        public Builder add(int location, int components, AttributeType type)
        {
            this.attrs.add(new AttributeDescriptor(location, components, type, this.cursor, 0));
            this.cursor += components * type.bytes;
            return this;
        }


        public Builder addInstanced(int location, int components, AttributeType type, int divisor)
        {
            this.attrs.add(new AttributeDescriptor(location, components, type, this.cursor, divisor));
            this.cursor += components * type.bytes;
            return this;
        }


        public Builder skip(int bytes)
        {
            this.cursor += bytes;
            return this;
        }

        public BufferLayout build()
        {
            return new BufferLayout(new ArrayList<>(this.attrs), this.cursor);
        }
    }
}
