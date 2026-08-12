package eleeter.unifystudiox.particle;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eleeter.unifystudiox.particle.api.EmitterShape;
import eleeter.unifystudiox.particle.api.ParticleForce;
import eleeter.unifystudiox.particle.curve.FloatCurve;
import eleeter.unifystudiox.particle.emitter.EmitterMode;
import eleeter.unifystudiox.particle.emitter.shape.BoxEmitterShape;
import eleeter.unifystudiox.particle.emitter.shape.ConeEmitterShape;
import eleeter.unifystudiox.particle.emitter.shape.PointEmitterShape;
import eleeter.unifystudiox.particle.emitter.shape.SphereEmitterShape;
import eleeter.unifystudiox.particle.force.AttractorForce;
import eleeter.unifystudiox.particle.force.DragForce;
import eleeter.unifystudiox.particle.force.GravityForce;
import eleeter.unifystudiox.particle.force.TurbulenceForce;

public class ParticleEffectLoader
{
    private static final Gson GSON = new GsonBuilder().create();

    private ParticleEffectLoader()
    {
    }


    public static ParticleEffectDef loadDef(String path)
    {
        try
        {
            Reader reader = openReader(path);
            ParticleEffectDef def = GSON.fromJson(reader, ParticleEffectDef.class);
            reader.close();
            return def;
        } catch (Exception e)
        {
            throw new RuntimeException("Failed to load particle effect definition: " + path, e);
        }
    }


    public static ParticleEffectEntity build(String entityId, String path)
    {
        return build(entityId, loadDef(path));
    }


    public static ParticleEffectEntity build(String entityId, ParticleEffectDef def)
    {
        List<ParticleEmitter> emitters = new ArrayList<>();

        for (ParticleEffectDef.EmitterDef ed : def.emitters)
        {
            emitters.add(buildEmitter(ed));
        }

        return new ParticleEffectEntity(entityId, emitters);
    }

    private static ParticleEmitter buildEmitter(ParticleEffectDef.EmitterDef ed)
    {
        EmitterShape shape = buildShape(ed.shape);
        EmitterMode mode = parseMode(ed.emitterMode);

        boolean additive = "ADDITIVE".equalsIgnoreCase(ed.blendMode);

        ParticleEmitter.Builder builder = new ParticleEmitter.Builder().shape(shape).mode(mode).emissionRate(ed.emissionRate).maxParticles(ed.maxParticles).lifetime(ed.lifetimeMin, ed.lifetimeMax).speed(ed.speedMin, ed.speedMax).startSize(ed.startSize).endSize(ed.endSize).startColor(safeGet(ed.startColor, 0, 1.0F), safeGet(ed.startColor, 1, 1.0F), safeGet(ed.startColor, 2, 1.0F)).endColor(safeGet(ed.endColor, 0, 1.0F), safeGet(ed.endColor, 1, 1.0F), safeGet(ed.endColor, 2, 1.0F)).startAlpha(ed.startAlpha).endAlpha(ed.endAlpha).rotationVelocity(ed.rotationVelocityMin, ed.rotationVelocityMax).spriteSheet(ed.spriteSheet.rows, ed.spriteSheet.cols, ed.spriteSheet.fps).texture(ed.texturePath).additive(additive).castsLight(ed.castsLight || additive).castsShadow(ed.castsShadow).shadowDirection(safeGet(ed.shadowDirection, 0, 0.0F), safeGet(ed.shadowDirection, 1, -1.0F), safeGet(ed.shadowDirection, 2, 0.0F)).loopInterval(ed.loopInterval);

        if (ed.lightWeight >= 0.0F)
        {
            builder.lightWeight(ed.lightWeight);
        }

        if (ed.alphaCurve != null)
        {
            builder.alphaCurve(parseCurve(ed.alphaCurve));
        }

        if (ed.sizeCurve != null)
        {
            builder.sizeCurve(parseCurve(ed.sizeCurve));
        }

        if (ed.colorRCurve != null)
        {
            builder.colorRCurve(parseCurve(ed.colorRCurve));
        }

        if (ed.colorGCurve != null)
        {
            builder.colorGCurve(parseCurve(ed.colorGCurve));
        }

        if (ed.colorBCurve != null)
        {
            builder.colorBCurve(parseCurve(ed.colorBCurve));
        }


        for (ParticleEffectDef.ForceDef fd : ed.forces)
        {
            ParticleForce force = buildForce(fd);
            if (force != null)
            {
                builder.addForce(force);
            }
        }

        return builder.build();
    }

    private static EmitterShape buildShape(ParticleEffectDef.ShapeDef sd)
    {
        if (sd == null)
        {
            return new PointEmitterShape();
        }

        return switch (sd.type.toUpperCase())
        {
            case "SPHERE" -> new SphereEmitterShape(sd.radius);
            case "CONE" -> new ConeEmitterShape(sd.radius, sd.angle);
            case "BOX" -> new BoxEmitterShape(sd.sizeX, sd.sizeY, sd.sizeZ);
            default -> new PointEmitterShape();
        };
    }

    private static EmitterMode parseMode(String name)
    {
        return switch (name.toUpperCase())
        {
            case "BURST" -> EmitterMode.BURST;
            case "ONE_SHOT" -> EmitterMode.ONE_SHOT;
            case "LOOPING" -> EmitterMode.LOOPING;
            default -> EmitterMode.CONTINUOUS;
        };
    }

    private static ParticleForce buildForce(ParticleEffectDef.ForceDef fd)
    {
        return switch (fd.type.toUpperCase())
        {
            case "GRAVITY" ->
            {
                if (fd.direction != null && fd.direction.length >= 3)
                {
                    yield new GravityForce(fd.direction[0], fd.direction[1], fd.direction[2]);
                }
                yield new GravityForce(fd.strength);
            }
            case "DRAG" -> new DragForce(fd.coefficient);
            case "TURBULENCE" -> new TurbulenceForce(fd.strength, fd.frequency);
            case "ATTRACTOR" -> new AttractorForce(
                    safeGet(fd.position, 0, 0.0F), safeGet(fd.position, 1, 0.0F), safeGet(fd.position, 2, 0.0F),
                    fd.strength, fd.range);
            default -> null;
        };
    }

    private static FloatCurve parseCurve(ParticleEffectDef.CurveDef cd)
    {
        int n = cd.keys.length;
        float[] times = new float[n];
        float[] values = new float[n];

        for (int i = 0; i < n; i++)
        {
            times[i] = cd.keys[i][0];
            values[i] = cd.keys[i][1];
        }

        return new FloatCurve(times, values);
    }

    private static float safeGet(float[] arr, int index, float fallback)
    {
        if (arr == null || index >= arr.length)
        {
            return fallback;
        }

        return arr[index];
    }

    private static Reader openReader(String path) throws Exception
    {
        if (path.startsWith("/"))
        {
            InputStream stream = ParticleEffectLoader.class.getResourceAsStream(path);

            if (stream == null)
            {
                throw new FileNotFoundException("Classpath resource not found: " + path);
            }

            return new InputStreamReader(stream, StandardCharsets.UTF_8);
        }

        return new FileReader(path, StandardCharsets.UTF_8);
    }
}
