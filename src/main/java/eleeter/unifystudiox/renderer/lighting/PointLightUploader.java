package eleeter.unifystudiox.renderer.lighting;

import java.util.ArrayList;
import java.util.List;

import eleeter.unifystudiox.graphics.api.IShaderProgram;
import eleeter.unifystudiox.scene.PointLightData;
import eleeter.unifystudiox.scene.Scene;
import eleeter.unifystudiox.scene.entity.PointLightEntity;
import eleeter.unifystudiox.scene.entity.SceneEntity;

public class PointLightUploader
{

    public static final int MAX_POINT_LIGHTS = 8;


    private PointLightUploader() {}


    public static void upload(IShaderProgram shader, Scene scene)
    {
        List<PointLightData> lights = collect(scene);

        int count = Math.min(lights.size(), MAX_POINT_LIGHTS);
        shader.setUniform("uPointCount", count);

        for (int i = 0; i < count; i++)
        {
            PointLightData pl = lights.get(i);
            String base = "uPoint[" + i + "].";

            shader.setUniform(base + "position", pl.position.x, pl.position.y, pl.position.z);

            shader.setUniform(base + "color", pl.color.x * pl.intensity, pl.color.y * pl.intensity, pl.color.z * pl.intensity);

            shader.setUniform(base + "range", pl.range);
        }
    }


    private static List<PointLightData> collect(Scene scene)
    {
        List<PointLightData> result = new ArrayList<>(MAX_POINT_LIGHTS);
        for (SceneEntity entity : scene.getEntities())
        {
            if (entity instanceof PointLightEntity pe && pe.getData().enabled)
            {
                result.add(pe.getData());
                if (result.size() == MAX_POINT_LIGHTS)
                {
                    break;
                }
            }
        }
        return result;
    }
}
