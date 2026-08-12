package eleeter.unifystudiox.renderer.lighting;

import java.util.ArrayList;
import java.util.List;

import eleeter.unifystudiox.graphics.api.IShaderProgram;
import eleeter.unifystudiox.scene.Scene;
import eleeter.unifystudiox.scene.SpotlightData;
import eleeter.unifystudiox.scene.entity.SceneEntity;
import eleeter.unifystudiox.scene.entity.SpotlightEntity;

public class SpotlightUploader
{

    public static final int MAX_SPOTLIGHTS = 8;


    private SpotlightUploader() {}



    public static void upload(IShaderProgram shader, Scene scene)
    {
        List<SpotlightData> lights = collect(scene);

        int count = Math.min(lights.size(), MAX_SPOTLIGHTS);
        shader.setUniform("uSpotCount", count);

        int shadowIndex = -1;
        for (int i = 0; i < count; i++)
        {
            if (lights.get(i).castShadow)
            {
                shadowIndex = i;
                break;
            }
        }
        shader.setUniform("uSpotShadowIndex", shadowIndex);

        for (int i = 0; i < count; i++)
        {
            SpotlightData sl = lights.get(i);
            String base = "uSpot[" + i + "].";

            shader.setUniform(base + "position", sl.position.x, sl.position.y, sl.position.z);

            shader.setUniform(base + "direction", sl.direction.x, sl.direction.y, sl.direction.z);

            shader.setUniform(base + "color", sl.color.x * sl.intensity, sl.color.y * sl.intensity, sl.color.z * sl.intensity);

            shader.setUniform(base + "innerCutoff", (float) Math.cos(Math.toRadians(sl.innerCutoffDeg)));

            shader.setUniform(base + "outerCutoff", (float) Math.cos(Math.toRadians(sl.outerCutoffDeg)));

            shader.setUniform(base + "range", sl.range);
        }
    }



    private static List<SpotlightData> collect(Scene scene)
    {
        List<SpotlightData> result = new ArrayList<>(MAX_SPOTLIGHTS);
        for (SceneEntity entity : scene.getEntities())
        {
            if (entity instanceof SpotlightEntity se && se.getData().enabled)
            {
                result.add(se.getData());
                if (result.size() == MAX_SPOTLIGHTS)
                {
                    break;
                }
            }
        }
        return result;
    }
}
