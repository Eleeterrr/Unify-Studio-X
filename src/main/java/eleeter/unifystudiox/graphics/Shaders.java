package eleeter.unifystudiox.graphics;

import eleeter.unifystudiox.graphics.api.IGraphicsBackend;
import eleeter.unifystudiox.graphics.api.IShaderProgram;

public class Shaders
{
    private static IShaderProgram particle;
    private static IShaderProgram cloud;
    private static IShaderProgram sky;

    private Shaders()
    {
    }

    public static void init(IGraphicsBackend backend)
    {
        particle = backend.createShaderProgram(
                "/shaders/particle.vert",
                "/shaders/particle.frag",
                null
        );

        cloud = backend.createShaderProgram(
                "/shaders/cloud.vert",
                "/shaders/cloud.frag",
                null
        );

        sky = backend.createShaderProgram(
                "/shaders/sky.vert",
                "/shaders/sky.frag",
                null
        );

        if (particle == null)
        {
            throw new IllegalStateException(
                    "Failed to create particle shader: /shaders/particle.vert + /shaders/particle.frag"
            );
        }

        if (cloud == null)
        {
            throw new IllegalStateException(
                    "Failed to create cloud shader: /shaders/cloud.vert + /shaders/cloud.frag"
            );
        }

        if (sky == null)
        {
            throw new IllegalStateException(
                    "Failed to create sky shader: /shaders/sky.vert + /shaders/sky.frag"
            );
        }
    }

    public static IShaderProgram particle()
    {
        return particle;
    }

    public static IShaderProgram Cloud()
    {
        return cloud;
    }

    public static IShaderProgram Sky()
    {
        return sky;
    }

    public static void cleanup()
    {
        if (particle != null)
        {
            particle.cleanup();
            particle = null;
        }

        if (cloud != null)
        {
            cloud.cleanup();
            cloud = null;
        }

        if (sky != null)
        {
            sky.cleanup();
            sky = null;
        }
    }
}
