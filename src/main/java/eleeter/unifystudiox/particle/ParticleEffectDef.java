package eleeter.unifystudiox.particle;

import java.util.List;

public class ParticleEffectDef
{
    public static class EffectDef
    {
        public boolean lightEnabled = false;
        public float lightWeight = 1.0F;
    }

    public EffectDef effect = new EffectDef();

    public static class EmitterDef
    {
        public String texturePath = "/textures/particles/default.png";
        public String blendMode = "ALPHA";
        public String emitterMode = "CONTINUOUS";
        public float emissionRate = 20.0F;
        public int maxParticles = 200;
        public boolean distortion = false;
        public float bloomIntensity = 1.0F;
        public float hdrMultiplier = 1.0F;
        public float velocityStretch = 0.0F;
        public float lightWeight = -1.0F;
        public boolean castsLight = false;
        public boolean castsShadow = false;
        public float[] shadowDirection = {0.0F, -1.0F, 0.0F};
        public boolean receivesLight = true;

        public ShapeDef shape = new ShapeDef();

        public float lifetimeMin = 1.0F;
        public float lifetimeMax = 2.0F;
        public float speedMin = 1.0F;
        public float speedMax = 3.0F;
        public float startSize = 0.3F;
        public float endSize = 0.6F;

        public float[] startColor = {1.0F, 1.0F, 1.0F};
        public float[] endColor = {1.0F, 1.0F, 1.0F};
        public float startAlpha = 1.0F;
        public float endAlpha = 0.0F;

        public float rotationVelocityMin = -1.0F;
        public float rotationVelocityMax = 1.0F;

        public SpriteDef spriteSheet = new SpriteDef();
        public List<ForceDef> forces = List.of();

        public CurveDef alphaCurve = null;
        public CurveDef sizeCurve = null;
        public CurveDef colorRCurve = null;
        public CurveDef colorGCurve = null;
        public CurveDef colorBCurve = null;

        public float loopInterval = 2.0F;
    }

    public static class ShapeDef
    {
        public String type = "POINT";
        public float radius = 0.0F;
        public float angle = 15.0F;
        public float sizeX = 1.0F;
        public float sizeY = 1.0F;
        public float sizeZ = 1.0F;
    }

    public static class SpriteDef
    {
        public int rows = 1;
        public int cols = 1;
        public float fps = 0.0F;
    }

    public static class ForceDef
    {
        public String type = "GRAVITY";
        public float strength = 9.81F;
        public float[] direction = null;
        public float coefficient = 0.3F;
        public float frequency = 1.0F;
        public float[] position = {0.0F, 0.0F, 0.0F};
        public float range = 10.0F;
    }

    public static class CurveDef
    {
        public float[][] keys = {{0.0F, 1.0F}, {1.0F, 0.0F}};
    }

    public List<EmitterDef> emitters = List.of();
}
