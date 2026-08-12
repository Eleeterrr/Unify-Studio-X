package eleeter.unifystudiox.graphics;

import static org.lwjgl.opengl.GL11C.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_RGB;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.GL_RGBA8;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL14C.GL_DEPTH_COMPONENT24;
import static org.lwjgl.opengl.GL30C.GL_DEPTH24_STENCIL8;
import static org.lwjgl.opengl.GL30C.GL_DEPTH_COMPONENT32F;
import static org.lwjgl.opengl.GL30C.GL_DEPTH_STENCIL;
import static org.lwjgl.opengl.GL30C.GL_HALF_FLOAT;
import static org.lwjgl.opengl.GL30C.GL_RGB16F;
import static org.lwjgl.opengl.GL30C.GL_RGBA32F;
import static org.lwjgl.opengl.GL30C.GL_UNSIGNED_INT_24_8;

public enum TextureFormatBit
{
    RGBA8(GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE, false),
    RGB16F(GL_RGB16F, GL_RGB, GL_HALF_FLOAT, false),
    RGBA32F(GL_RGBA32F, GL_RGBA, GL_FLOAT, false),
    DEPTH24(GL_DEPTH_COMPONENT24, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, true),
    DEPTH32F(GL_DEPTH_COMPONENT32F, GL_DEPTH_COMPONENT, GL_FLOAT, true),
    DEPTH24_STENCIL8(GL_DEPTH24_STENCIL8, GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, true);

    public final int internalFormat;
    public final int pixelFormat;
    public final int dataType;
    public final boolean isDepth;

    TextureFormatBit(int internalFormat, int pixelFormat, int dataType, boolean isDepth)
    {
        this.internalFormat = internalFormat;
        this.pixelFormat = pixelFormat;
        this.dataType = dataType;
        this.isDepth = isDepth;
    }
}
