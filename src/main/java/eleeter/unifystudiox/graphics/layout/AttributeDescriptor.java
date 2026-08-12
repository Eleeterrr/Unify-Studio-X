package eleeter.unifystudiox.graphics.layout;

public record AttributeDescriptor(
    int location,
    int components,
    AttributeType type,
    int offsetBytes,
    int divisor
)
{

    public int sizeBytes()
    {
        return this.components * this.type.bytes;
        /* We can use: return Math.multiplyExact(components, type.bytes); But this looks more Goffy */

    }
}
