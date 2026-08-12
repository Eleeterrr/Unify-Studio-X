package eleeter.unifystudiox.renderer.lighting;

import org.joml.Vector3f;

import eleeter.unifystudiox.particle.EmitterLightSnapshot;


public  class PointLightShadowMatrixBuilder
{
    private static final float NEAR_PLANE = 0.1F;
    private static final float FOV_Y_RAD = (float) Math.toRadians(110.0F);
    private static final float FAR_RANGE_SCALE = 1.6F;
    private static final float ORTHO_EXTENT_SCALE = 0.85F;

    private PointLightShadowMatrixBuilder()
    {
    }

    public static void update(EmitterLightSnapshot light, float dirX, float dirY, float dirZ)
    {
        Vector3f pos = new Vector3f(light.x, light.y, light.z);
        Vector3f dir = new Vector3f(dirX, dirY, dirZ);

        if (dir.lengthSquared() < 1.0E-6F)
        {
            dir.set(0.0F, -1.0F, 0.0F);
        }
        dir.normalize();

        if (dir.y < -0.5F)
        {
            float halfExtent = light.range * ORTHO_EXTENT_SCALE;
            Vector3f eye = new Vector3f(pos.x, pos.y + light.range, pos.z);
            Vector3f center = new Vector3f(pos.x, pos.y - light.range, pos.z);

            light.lightProjection.identity().ortho(-halfExtent, halfExtent, -halfExtent, halfExtent, NEAR_PLANE,
                    light.range * 2.0F);
            light.lightView.identity().lookAt(eye, center, new Vector3f(0.0F, 0.0F, -1.0F));
        } else
        {
            Vector3f target = new Vector3f(pos).fma(light.range, dir);
            Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);

            if (Math.abs(dir.y) > 0.95F)
            {
                up.set(1.0F, 0.0F, 0.0F);
            }

            light.lightView.identity().lookAt(pos, target, up);
            light.lightProjection.identity().perspective(FOV_Y_RAD, 1.0F, NEAR_PLANE, light.range * FAR_RANGE_SCALE);
        }

        light.lightProjection.mul(light.lightView, light.lightSpaceMatrix);
    }
}
