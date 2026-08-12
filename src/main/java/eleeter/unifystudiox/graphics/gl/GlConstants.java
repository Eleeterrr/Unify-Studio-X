package eleeter.unifystudiox.graphics.gl;

import org.lwjgl.opengl.ARBVertexArrayObject;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL14C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL21C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL32C;

public class GlConstants
{

    /* Data types */
    public static final int GL_BYTE = GL11C.GL_BYTE;
    public static final int GL_UNSIGNED_BYTE = GL11C.GL_UNSIGNED_BYTE;
    public static final int GL_SHORT = GL11C.GL_SHORT;
    public static final int GL_UNSIGNED_SHORT = GL11C.GL_UNSIGNED_SHORT;
    public static final int GL_INT = GL11C.GL_INT;
    public static final int GL_UNSIGNED_INT = GL11C.GL_UNSIGNED_INT;
    public static final int GL_FLOAT = GL11C.GL_FLOAT;
    public static final int GL_DOUBLE = GL11C.GL_DOUBLE;

    /* Primitives */
    public static final int GL_POINTS = GL11C.GL_POINTS;
    public static final int GL_LINES = GL11C.GL_LINES;
    public static final int GL_LINE_STRIP = GL11C.GL_LINE_STRIP;
    public static final int GL_LINE_LOOP = GL11C.GL_LINE_LOOP;
    public static final int GL_TRIANGLES = GL11C.GL_TRIANGLES;
    public static final int GLC_TRIANGLES = GL_TRIANGLES;
    public static final int GL_TRIANGLE_STRIP = GL11C.GL_TRIANGLE_STRIP;
    public static final int GL_TRIANGLE_FAN = GL11C.GL_TRIANGLE_FAN;

    /* State & Capabilities */
    public static final int GL_BLEND = GL11C.GL_BLEND;
    public static final int GL_CULL_FACE = GL11C.GL_CULL_FACE;
    public static final int GL_DEPTH_TEST = GL11C.GL_DEPTH_TEST;
    public static final int GL_SCISSOR_TEST = GL11C.GL_SCISSOR_TEST;

    /* Buffer Bits */
    public static final int GL_COLOR_BUFFER_BIT = GL11C.GL_COLOR_BUFFER_BIT;
    public static final int GL_DEPTH_BUFFER_BIT = GL11C.GL_DEPTH_BUFFER_BIT;
    public static final int GL_STENCIL_BUFFER_BIT = GL11C.GL_STENCIL_BUFFER_BIT;

    /* Booleans */
    public static final int GL_TRUE = GL11C.GL_TRUE;
    public static final int GL_FALSE = GL11C.GL_FALSE;

    /* Blend Functions */
    public static final int GL_SRC_ALPHA = GL11C.GL_SRC_ALPHA;
    public static final int GL_ONE_MINUS_SRC_ALPHA = GL11C.GL_ONE_MINUS_SRC_ALPHA;
    public static final int GL_BLEND_SRC_RGB = GL14C.GL_BLEND_SRC_RGB;
    public static final int GL_BLEND_DST_RGB = GL14C.GL_BLEND_DST_RGB;

    /* Textures */
    public static final int GL_TEXTURE_2D = GL11C.GL_TEXTURE_2D;
    public static final int GL_TEXTURE_3D = GL12C.GL_TEXTURE_3D;
    public static final int GL_TEXTURE0 = GL13C.GL_TEXTURE0;
    
    /* Texture Parameters */
    public static final int GL_TEXTURE_MAG_FILTER = GL11C.GL_TEXTURE_MAG_FILTER;
    public static final int GL_TEXTURE_MIN_FILTER = GL11C.GL_TEXTURE_MIN_FILTER;
    public static final int GL_TEXTURE_WRAP_S = GL11C.GL_TEXTURE_WRAP_S;
    public static final int GL_TEXTURE_WRAP_T = GL11C.GL_TEXTURE_WRAP_T;
    public static final int GL_TEXTURE_WRAP_R = GL12C.GL_TEXTURE_WRAP_R;

    /* Texture Filtering & Wrapping */
    public static final int GL_NEAREST = GL11C.GL_NEAREST;
    public static final int GL_LINEAR = GL11C.GL_LINEAR;
    public static final int GL_NEAREST_MIPMAP_NEAREST = GL11C.GL_NEAREST_MIPMAP_NEAREST;
    public static final int GL_LINEAR_MIPMAP_NEAREST = GL11C.GL_LINEAR_MIPMAP_NEAREST;
    public static final int GL_NEAREST_MIPMAP_LINEAR = GL11C.GL_NEAREST_MIPMAP_LINEAR;
    public static final int GL_LINEAR_MIPMAP_LINEAR = GL11C.GL_LINEAR_MIPMAP_LINEAR;
    public static final int GL_REPEAT = GL11C.GL_REPEAT;
    public static final int GL_CLAMP_TO_EDGE = GL12C.GL_CLAMP_TO_EDGE;

    /* Formats */
    public static final int GL_RGB = GL11C.GL_RGB;
    public static final int GL_RGBA = GL11C.GL_RGBA;
    public static final int GL_RGBA8 = GL11C.GL_RGBA8;
    public static final int GL_SRGB8_ALPHA8 = GL21C.GL_SRGB8_ALPHA8;
    public static final int GL_DEPTH_COMPONENT = GL11C.GL_DEPTH_COMPONENT;
    public static final int GL_DEPTH_COMPONENT24 = GL14C.GL_DEPTH_COMPONENT24;
    public static final int GL_DEPTH24_STENCIL8 = GL30C.GL_DEPTH24_STENCIL8;

    /* Buffers */
    public static final int GL_ARRAY_BUFFER = GL15C.GL_ARRAY_BUFFER;
    public static final int GL_ELEMENT_ARRAY_BUFFER = GL15C.GL_ELEMENT_ARRAY_BUFFER;
    public static final int GL_STATIC_DRAW = GL15C.GL_STATIC_DRAW;
    public static final int GL_DYNAMIC_DRAW = GL15C.GL_DYNAMIC_DRAW;
    public static final int GL_STREAM_DRAW = GL15C.GL_STREAM_DRAW;

    /* Shaders */
    public static final int GL_VERTEX_SHADER = GL20C.GL_VERTEX_SHADER;
    public static final int GL_FRAGMENT_SHADER = GL20C.GL_FRAGMENT_SHADER;
    public static final int GL_GEOMETRY_SHADER = GL32C.GL_GEOMETRY_SHADER;
    public static final int GL_COMPILE_STATUS = GL20C.GL_COMPILE_STATUS;
    public static final int GL_LINK_STATUS = GL20C.GL_LINK_STATUS;
    public static final int GL_VALIDATE_STATUS = GL20C.GL_VALIDATE_STATUS;
    public static final int GL_CURRENT_PROGRAM = GL20C.GL_CURRENT_PROGRAM;

    /* Vertex Arrays */
    public static final int GL_VERTEX_ARRAY_BINDING = ARBVertexArrayObject.GL_VERTEX_ARRAY_BINDING;
    
    /* Framebuffers */
    public static final int GL_FRAMEBUFFER = GL30C.GL_FRAMEBUFFER;
    public static final int GL_COLOR_ATTACHMENT0 = GL30C.GL_COLOR_ATTACHMENT0;
    public static final int GL_DEPTH_ATTACHMENT = GL30C.GL_DEPTH_ATTACHMENT;
    public static final int GL_DEPTH_STENCIL_ATTACHMENT = GL30C.GL_DEPTH_STENCIL_ATTACHMENT;

    /* Other Constants */
    public static final int GL_SCISSOR_BOX = GL11C.GL_SCISSOR_BOX;

    private GlConstants()
    {
    }
}
