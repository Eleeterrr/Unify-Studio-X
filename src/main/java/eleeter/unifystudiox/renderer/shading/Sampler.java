package eleeter.unifystudiox.renderer.shading;

import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.glGetFloat;
import static org.lwjgl.opengl.GL33.GL_TEXTURE_LOD_BIAS;
import static org.lwjgl.opengl.GL33.glBindSampler;
import static org.lwjgl.opengl.GL33.glDeleteSamplers;
import static org.lwjgl.opengl.GL33.glGenSamplers;
import static org.lwjgl.opengl.GL33.glSamplerParameterf;
import static org.lwjgl.opengl.GL33.glSamplerParameteri;

import org.lwjgl.opengl.GL;

import eleeter.unifystudiox.renderer.core.RenderSettings;

public class Sampler
{

    private static int trilinearSamplerId = -1;
    private static int pixelPerfectSamplerId = -1;
    private static int linearSamplerId = -1;

    private static float maxAnisotropy = -1.0F;

    /**
     * Initializes the sampler profiles.
     */
    public static void init()
    {
        if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic)
        {
            maxAnisotropy = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
        }

        trilinearSamplerId = glGenSamplers();
        glSamplerParameteri(trilinearSamplerId, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glSamplerParameteri(trilinearSamplerId, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glSamplerParameteri(trilinearSamplerId, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glSamplerParameteri(trilinearSamplerId, GL_TEXTURE_WRAP_T, GL_REPEAT);

        pixelPerfectSamplerId = glGenSamplers();
        glSamplerParameteri(pixelPerfectSamplerId, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glSamplerParameteri(pixelPerfectSamplerId, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glSamplerParameteri(pixelPerfectSamplerId, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glSamplerParameteri(pixelPerfectSamplerId, GL_TEXTURE_WRAP_T, GL_REPEAT);

        linearSamplerId = glGenSamplers();
        glSamplerParameteri(linearSamplerId, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glSamplerParameteri(linearSamplerId, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glSamplerParameteri(linearSamplerId, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glSamplerParameteri(linearSamplerId, GL_TEXTURE_WRAP_T, GL_REPEAT);

    }


    public static void bind(int unit, TextureSampling mode)
    {
        switch (mode)
        {
            case TRILINEAR ->
            {
                if (trilinearSamplerId != -1)
                {
                    glSamplerParameterf(trilinearSamplerId, GL_TEXTURE_LOD_BIAS, RenderSettings.GLOBAL_LOD_BIAS);
                    if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic)
                    {
                        float val = RenderSettings.ANISOTROPY_ENABLED ? maxAnisotropy : 1.0F;
                        glSamplerParameterf(trilinearSamplerId, GL_TEXTURE_MAX_ANISOTROPY_EXT, Math.min(val, 16.0F));
                    }
                    glBindSampler(unit, trilinearSamplerId);
                } else
                {
                    glBindSampler(unit, 0);
                }
            }
            case PIXEL_PERFECT ->
            {
                if (pixelPerfectSamplerId != -1)
                {
                    glBindSampler(unit, pixelPerfectSamplerId);
                } else
                {
                    glBindSampler(unit, 0);
                }
            }
            case LINEAR ->
            {
                if (linearSamplerId != -1)
                {
                    glBindSampler(unit, linearSamplerId);
                } else
                {
                    glBindSampler(unit, 0);
                }
            }
        }
    }

    public static void cleanup()
    {
        if (trilinearSamplerId != -1)
        {
            glDeleteSamplers(trilinearSamplerId);
        }

        if (pixelPerfectSamplerId != -1)
        {
            glDeleteSamplers(pixelPerfectSamplerId);
        }

        if (linearSamplerId != -1)
        {
            glDeleteSamplers(linearSamplerId);
        }
    }
}
