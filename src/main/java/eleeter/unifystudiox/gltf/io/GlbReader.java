package eleeter.unifystudiox.gltf.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;


public class GlbReader
{
    public static final int MAGIC_GLTF = 0x46546C67;
    public static final int CHUNK_JSON = 0x4E4F534A;
    public static final int CHUNK_BIN = 0x004E4942;

    public String jsonChunk;
    public byte[] binChunk;

    public static GlbReader read(String path)
    {
        try
        {
            byte[] fileBytes = Files.readAllBytes(Paths.get(path));
            if (fileBytes.length < 12)
            {
                return null;
            }

            ByteBuffer buffer = ByteBuffer.wrap(fileBytes).order(ByteOrder.LITTLE_ENDIAN);
            int magic = buffer.getInt();
            if (magic != MAGIC_GLTF)
            {
                return null;
            }

            int version = buffer.getInt();
            int length = buffer.getInt();

            GlbReader reader = new GlbReader();
            
            while (buffer.remaining() >= 8)
            {
                int chunkLength = buffer.getInt();
                int chunkType = buffer.getInt();
                
                if (chunkType == CHUNK_JSON)
                {
                    byte[] jsonBytes = new byte[chunkLength];
                    buffer.get(jsonBytes);
                    reader.jsonChunk = new String(jsonBytes, "UTF-8");
                } else if (chunkType == CHUNK_BIN)
                {
                    reader.binChunk = new byte[chunkLength];
                    buffer.get(reader.binChunk);
                } else
                {
                    buffer.position(buffer.position() + chunkLength);
                }
            }

            return reader;
        } catch (IOException ioException)
        {
            throw new RuntimeException("[GltfToAmb] Error reading GLB file: " + path, ioException);
        }
    }
}
