package eleeter.unifystudiox.renderer.lighting;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import eleeter.unifystudiox.graphics.api.IShaderProgram;
import eleeter.unifystudiox.particle.EmitterLightSnapshot;
import eleeter.unifystudiox.particle.ParticleEffectEntity;
import eleeter.unifystudiox.particle.ParticleEmitter;
import eleeter.unifystudiox.renderer.core.RenderSettings;
import eleeter.unifystudiox.scene.Scene;
import eleeter.unifystudiox.scene.entity.SceneEntity;

public class ParticleLightUploader
{
    public static final class ShadowBinding
    {
        public final int lightIndex;
        public final EmitterLightSnapshot caster;

        public ShadowBinding(int lightIndex, EmitterLightSnapshot caster)
        {
            this.lightIndex = lightIndex;
            this.caster = caster;
        }
    }

    private ParticleLightUploader()
    {
    }

    public static void upload(IShaderProgram shader, Scene scene, FloatBuffer matrixBuffer)
    {
        shader.setUniform("uParticleLightCount", 0);
        shader.setUniform("uHasParticleLightShadow", false);
        shader.setUniform("uParticleLightShadowIndex", -1);
        shader.setUniform("uParticleLightShadowMap", RenderSettings.PARTICLE_LIGHT_SHADOW_TEXTURE_UNIT);
    }

    public static List<EmitterLightSnapshot> collect(Scene scene)
    {
        List<EmitterLightSnapshot> result = new ArrayList<>(RenderSettings.MAX_PARTICLE_LIGHTS);

        for (SceneEntity entity : scene.getEntities())
        {
            if (!(entity instanceof ParticleEffectEntity effect))
            {
                continue;
            }

            for (ParticleEmitter emitter : effect.getEmitters())
            {
                EmitterLightSnapshot snap = emitter.getEmissionLight();

                if (snap.active)
                {
                    result.add(snap);

                    if (result.size() == RenderSettings.MAX_PARTICLE_LIGHTS)
                    {
                        return result;
                    }
                }
            }
        }

        return result;
    }

    public static ShadowBinding resolveShadowBinding(List<EmitterLightSnapshot> lights, int uploadedCount)
    {
        int bestIndex = -1;
        float bestIntensity = 0.0F;
        EmitterLightSnapshot best = null;

        for (int i = 0; i < uploadedCount; i++)
        {
            EmitterLightSnapshot snap = lights.get(i);

            if (snap.castsShadow && snap.intensity > bestIntensity)
            {
                bestIntensity = snap.intensity;
                bestIndex = i;
                best = snap;
            }
        }

        return new ShadowBinding(bestIndex, best);
    }

    public static EmitterLightSnapshot resolveShadowCaster(Scene scene)
    {
        EmitterLightSnapshot best = null;
        float bestIntensity = 0.0F;

        for (SceneEntity entity : scene.getEntities())
        {
            if (!(entity instanceof ParticleEffectEntity effect))
            {
                continue;
            }

            for (ParticleEmitter emitter : effect.getEmitters())
            {
                EmitterLightSnapshot snap = emitter.getEmissionLight();

                if (snap.active && snap.castsShadow && snap.intensity > bestIntensity)
                {
                    bestIntensity = snap.intensity;
                    best = snap;
                }
            }
        }

        return best;
    }

    public static int indexOf(List<EmitterLightSnapshot> lights, int uploadedCount, EmitterLightSnapshot target)
    {
        if (target == null)
        {
            return -1;
        }

        for (int i = 0; i < uploadedCount; i++)
        {
            if (lights.get(i) == target)
            {
                return i;
            }
        }

        return -1;
    }

    private static void uploadShadowUniforms(IShaderProgram shader, ShadowBinding binding, FloatBuffer matrixBuffer)
    {
        shader.setUniform("uParticleLightShadowMap", RenderSettings.PARTICLE_LIGHT_SHADOW_TEXTURE_UNIT);

        if (RenderSettings.PARTICLE_LIGHT_SHADOWS_ENABLED && binding.caster != null && binding.lightIndex >= 0)
        {
            shader.setUniform("uHasParticleLightShadow", true);
            shader.setUniform("uParticleLightShadowIndex", binding.lightIndex);
            shader.setUniformMatrix4f("uParticleLightShadowMatrix", binding.caster.lightSpaceMatrix.get(matrixBuffer));
        } else
        {
            shader.setUniform("uHasParticleLightShadow", false);
            shader.setUniform("uParticleLightShadowIndex", -1);
        }
    }
}
