package eleeter.unifystudiox.renderer.core;

public class RenderSettings
{


    /**
     * Toggles anisotropic filtering globally.
     */
    public static boolean ANISOTROPY_ENABLED = true;


    public static float GLOBAL_LOD_BIAS = 0.35f;

    /**
     * Toggles night mode.
     */
    public static boolean NIGHT_MODE = false;

    public static boolean CLOUDS_ENABLED = false;
    public static float CLOUD_COVERAGE = 0.90f;
    public static float CLOUD_SPEED = 0.10f;
    public static float CLOUD_DENSITY = 1.70f;
    public static float CLOUD_ALTITUDE = 0.16f;


    public static boolean SHADOWS_ENABLED = true;
    public static int SHADOW_RESOLUTION = 4096;
    public static boolean PARTICLE_LIGHT_SHADOWS_ENABLED = true;
    public static final int MAX_PARTICLE_LIGHTS = 8;
    public static final int PARTICLE_LIGHT_SHADOW_TEXTURE_UNIT = 3;
    public static int MSAA_SAMPLES = 4;
    public static boolean FOG_ENABLED = true;
    public static boolean BLOOM_ENABLED = true;
    public static float BLOOM_THRESHOLD = 0.85F;
    public static float BLOOM_STRENGTH = 1.2F;
    public static int BLOOM_RADIUS = 8;
    
    public static boolean ACES_ENABLED = false;
    public static float ACES_EXPOSURE = 1.0F;

    public static boolean COLOR_GRADING_ENABLED = false;
    public static float CG_CONTRAST = 1.1F;
    public static float CG_BRIGHTNESS = 0.0F;
    public static float CG_SATURATION = 1.1F;

    public static boolean VIGNETTE_ENABLED = true;
    public static float VIGNETTE_INTENSITY = 0.5F;

    public static boolean CHROMA_ENABLED = false;
    public static float CHROMA_STRENGTH = 0.008F;

    public static boolean DOF_ENABLED = false;

    public static boolean SOFT_PARTICLES_ENABLED = true;

    public static volatile boolean PENDING_QUALITY_UPDATE = false;

    public static boolean VSYNC_ENABLED = true;
    public static float TARGET_FPS = 144.0f;

    public enum GraphicsQuality
    {
        ULTRA_LOW, LOW, MID, HIGH
    }

    private static GraphicsQuality currentQuality = GraphicsQuality.HIGH;

    public static GraphicsQuality getCurrentQuality()
    {
        return currentQuality;
    }

    public static void applyQuality(GraphicsQuality quality)
    {
        currentQuality = quality;
        switch (quality)
        {
            case ULTRA_LOW:
                SHADOWS_ENABLED = false;
                SHADOW_RESOLUTION = 0;
                MSAA_SAMPLES = 1;
                ANISOTROPY_ENABLED = false;
                CLOUDS_ENABLED = false;
                FOG_ENABLED = false;
                break;
            case LOW:
                SHADOWS_ENABLED = true;
                SHADOW_RESOLUTION = 1024;
                MSAA_SAMPLES = 1;
                ANISOTROPY_ENABLED = false;
                CLOUDS_ENABLED = false;
                FOG_ENABLED = true;
                break;
            case MID:
                SHADOWS_ENABLED = true;
                SHADOW_RESOLUTION = 2048;
                MSAA_SAMPLES = 4;
                ANISOTROPY_ENABLED = true;
                CLOUDS_ENABLED = true;
                FOG_ENABLED = true;
                break;
            case HIGH:
                SHADOWS_ENABLED = true;
                SHADOW_RESOLUTION = 4096;
                MSAA_SAMPLES = 4;
                ANISOTROPY_ENABLED = true;
                CLOUDS_ENABLED = true;
                FOG_ENABLED = true;
                break;
        }
        PENDING_QUALITY_UPDATE = true;
    }
}
