package eleeter.unifystudiox.graphics.gl;

import static org.lwjgl.opengl.GL45C.glTextureStorage2D;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL45C;

public class GlTexture
{
    public int genTextures()
    {
        return GL11C.glGenTextures();
    }

    public void deleteTextures(int texture)
    {
        GL11C.glDeleteTextures(texture);
    }

    public void texture2D (int handle, int i, int gl, int width, int height)
    {
        glTextureStorage2D(handle, i, gl, width, height);
    }

    public void bindTexture(int target, int texture)
    {
        GL11C.glBindTexture(target, texture);
    }

    public void activeTexture(int texture)
    {
        GL13C.glActiveTexture(texture);
    }

    public void texParameteri(int target, int pname, int param)
    {
        GL11C.glTexParameteri(target, pname, param);
    }

    public void texImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, ByteBuffer pixels)
    {
        GL11C.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public void texImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, long pixels)
    {
        GL11C.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public void generateMipmap(int target)
    {
        GL30C.glGenerateMipmap(target);
    }

    public int createTextures(int target)
    {
        return GL45C.glCreateTextures(target);
    }

    public void textureStorage2D(int texture, int levels, int internalformat, int width, int height)
    {
        GL45C.glTextureStorage2D(texture, levels, internalformat, width, height);
    }

    public void copyImageSubData(int srcName, int srcTarget, int srcLevel, int srcX, int srcY, int srcZ,
                                 int dstName, int dstTarget, int dstLevel, int dstX, int dstY, int dstZ,
                                 int srcWidth, int srcHeight, int srcDepth)
    {
        GL45C.glCopyImageSubData(srcName, srcTarget, srcLevel, srcX, srcY, srcZ, dstName, dstTarget, dstLevel, dstX, dstY, dstZ, srcWidth, srcHeight, srcDepth);
    }
    public static ByteBuffer ByteBuffer(int capacity)
    {
        return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
    }
}
