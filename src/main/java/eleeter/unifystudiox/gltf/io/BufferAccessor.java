package eleeter.unifystudiox.gltf.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import eleeter.unifystudiox.gltf.model.GltfAccessor;
import eleeter.unifystudiox.gltf.model.GltfBufferView;
import eleeter.unifystudiox.gltf.model.GltfModel;


public class BufferAccessor
{

    private static int getNumComponents(String type)
    {
        if ("SCALAR".equals(type)) return 1;
        if ("VEC2".equals(type)) return 2;
        if ("VEC3".equals(type)) return 3;
        if ("VEC4".equals(type)) return 4;
        if ("MAT2".equals(type)) return 4;
        if ("MAT3".equals(type)) return 9;
        if ("MAT4".equals(type)) return 16;
        return 1;
    }

    private static int getComponentByteSize(int componentType)
    {
        switch (componentType)
        {
            case 5120: return 1; // byte
            case 5121: return 1; // ubyte
            case 5122: return 2; // short
            case 5123: return 2; // ushort
            case 5125: return 4; // uint
            case 5126: return 4; // float
            default: return 1;
        }
    }

    public static float[] readFloats(GltfModel model, GltfAccessor accessor)
    {
        if (accessor == null) return null;

        int numComponents = getNumComponents(accessor.type);
        float[] result = new float[accessor.count * numComponents];
        
        if (accessor.bufferViewIndex < 0)
        {
            return result;
        }

        GltfBufferView view = model.bufferViews.get(accessor.bufferViewIndex);
        byte[] data = model.buffers.get(view.bufferIndex).data;

        int byteOffset = view.byteOffset + accessor.byteOffset;
        int compSize = getComponentByteSize(accessor.componentType);
        int stride = view.byteStride > 0 ? view.byteStride : (numComponents * compSize);

        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < accessor.count; i++)
        {
            int elementOffset = byteOffset + (i * stride);
            for (int c = 0; c < numComponents; c++)
            {
                int compOffset = elementOffset + (c * compSize);
                
                float val = 0.0f;
                if (accessor.componentType == 5126)
                { // float
                    val = bb.getFloat(compOffset);
                } else if (accessor.componentType == 5123)
                { // ushort (normalized?)
                    int u = bb.getShort(compOffset) & 0xFFFF;
                    val = (float) u / 65535.0f;
                } else if (accessor.componentType == 5121)
                { // ubyte (normalized?)
                    int u = bb.get(compOffset) & 0xFF;
                    val = (float) u / 255.0f;
                } else if (accessor.componentType == 5122)
                { // short
                    val = (float) bb.getShort(compOffset);
                } else if (accessor.componentType == 5120)
                { // byte
                    val = (float) bb.get(compOffset);
                }
                
                result[i * numComponents + c] = val;
            }
        }

        return result;
    }

    public static int[] readInts(GltfModel model, GltfAccessor accessor)
    {
        if (accessor == null) return null;

        int numComponents = getNumComponents(accessor.type);
        int[] result = new int[accessor.count * numComponents];

        if (accessor.bufferViewIndex < 0)
        {
            return result;
        }

        GltfBufferView view = model.bufferViews.get(accessor.bufferViewIndex);
        byte[] data = model.buffers.get(view.bufferIndex).data;

        int byteOffset = view.byteOffset + accessor.byteOffset;
        int compSize = getComponentByteSize(accessor.componentType);
        int stride = view.byteStride > 0 ? view.byteStride : (numComponents * compSize);

        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < accessor.count; i++)
        {
            int elementOffset = byteOffset + (i * stride);
            for (int c = 0; c < numComponents; c++)
            {
                int compOffset = elementOffset + (c * compSize);
                
                int val = 0;
                if (accessor.componentType == 5125)
                { // uint
                    val = bb.getInt(compOffset);
                } else if (accessor.componentType == 5123)
                { // ushort
                    val = bb.getShort(compOffset) & 0xFFFF;
                } else if (accessor.componentType == 5121)
                { // ubyte
                    val = bb.get(compOffset) & 0xFF;
                } else if (accessor.componentType == 5122)
                { // short
                    val = bb.getShort(compOffset);
                } else if (accessor.componentType == 5120)
                { // byte
                    val = bb.get(compOffset);
                }
                
                result[i * numComponents + c] = val;
            }
        }

        return result;
    }
}
