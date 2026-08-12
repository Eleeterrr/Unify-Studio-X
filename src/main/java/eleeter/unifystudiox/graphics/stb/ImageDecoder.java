package eleeter.unifystudiox.graphics.stb;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.stb.STBImage;

public class ImageDecoder
{
    private ImageDecoder()
    {
    }

    public static void setFlipVerticallyOnLoad(boolean flip)
    {
        STBImage.stbi_set_flip_vertically_on_load(flip);
    }

    public static ByteBuffer loadFromMemory(ByteBuffer buffer, IntBuffer w, IntBuffer h, IntBuffer comp, int reqComp)
    {
        return STBImage.stbi_load_from_memory(buffer, w, h, comp, reqComp);
    }

    public static ByteBuffer loadFromFile(String path, IntBuffer w, IntBuffer h, IntBuffer comp, int reqComp)
    {
        return STBImage.stbi_load(path, w, h, comp, reqComp);
    }

    public static String getFailureReason()
    {
        return STBImage.stbi_failure_reason();
    }

    public static void freeImage(ByteBuffer image)
    {
        STBImage.stbi_image_free(image);
    }
}
