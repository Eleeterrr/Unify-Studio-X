package eleeter.unifystudiox.particle;

import eleeter.unifystudiox.particle.api.EmitterShape;
import eleeter.unifystudiox.particle.api.ParticleForce;
import eleeter.unifystudiox.particle.curve.FloatCurve;
import eleeter.unifystudiox.particle.emitter.EmitterMode;
import eleeter.unifystudiox.particle.emitter.shape.PointEmitterShape;
import eleeter.unifystudiox.particle.pool.ParticlePool;
import eleeter.unifystudiox.renderer.lighting.PointLightShadowMatrixBuilder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.joml.Vector3f;

public class ParticleEmitter
{
    public static final int FLOATS_PER_INSTANCE = 14;

    private static final float FRAME_TIME_MIN = 1.0F / 120.0F;

    private final ParticlePool pool;
    private final EmitterShape shape;
    private final List<ParticleForce> forces;
    private final EmitterMode mode;

    private final FloatCurve sizeCurve;
    private final FloatCurve alphaCurve;
    private final FloatCurve colorRCurve;
    private final FloatCurve colorGCurve;
    private final FloatCurve colorBCurve;

    private final float emissionRate;
    private final float lifetimeMin;
    private final float lifetimeMax;
    private final float speedMin;
    private final float speedMax;
    private final float startSize;
    private final float endSize;
    private final float startColorR;
    private final float startColorG;
    private final float startColorB;
    private final float endColorR;
    private final float endColorG;
    private final float endColorB;
    private final float startAlpha;
    private final float endAlpha;
    private final float rotationVelocityMin;
    private final float rotationVelocityMax;

    private final int spriteRows;
    private final int spriteCols;
    private final float spriteFps;

    private final String texturePath;
    private final boolean isAdditive;
    private final boolean castsLight;
    private final boolean castsShadow;
    private final float lightWeight;
    private final float shadowDirX;
    private final float shadowDirY;
    private final float shadowDirZ;
    private final float loopInterval;

    private final EmitterLightSnapshot emissionLight = new EmitterLightSnapshot();

    private final Random rng = new Random();
    private final Vector3f spawnPos = new Vector3f();
    private final Vector3f spawnDir = new Vector3f();

    private float emissionAccumulator;
    private float loopTimer;
    private boolean hasBursted;
    private boolean isFinished;
    private int liveCount;

    private float worldX;
    private float worldY;
    private float worldZ;

    ParticleEmitter(Builder b)
    {
        this.pool = new ParticlePool(b.maxParticles);
        this.shape = b.shape;
        this.forces = List.copyOf(b.forces);
        this.mode = b.mode;
        this.sizeCurve = b.sizeCurve;
        this.alphaCurve = b.alphaCurve;
        this.colorRCurve = b.colorRCurve;
        this.colorGCurve = b.colorGCurve;
        this.colorBCurve = b.colorBCurve;
        this.emissionRate = b.emissionRate;
        this.lifetimeMin = b.lifetimeMin;
        this.lifetimeMax = b.lifetimeMax;
        this.speedMin = b.speedMin;
        this.speedMax = b.speedMax;
        this.startSize = b.startSize;
        this.endSize = b.endSize;
        this.startColorR = b.startColorR;
        this.startColorG = b.startColorG;
        this.startColorB = b.startColorB;
        this.endColorR = b.endColorR;
        this.endColorG = b.endColorG;
        this.endColorB = b.endColorB;
        this.startAlpha = b.startAlpha;
        this.endAlpha = b.endAlpha;
        this.rotationVelocityMin = b.rotationVelocityMin;
        this.rotationVelocityMax = b.rotationVelocityMax;
        this.spriteRows = b.spriteRows;
        this.spriteCols = b.spriteCols;
        this.spriteFps = b.spriteFps;
        this.texturePath = b.texturePath;
        this.isAdditive = b.isAdditive;
        this.castsLight = b.castsLight;
        this.castsShadow = b.castsShadow;
        this.lightWeight = b.lightWeight;
        this.shadowDirX = b.shadowDirX;
        this.shadowDirY = b.shadowDirY;
        this.shadowDirZ = b.shadowDirZ;
        this.loopInterval = b.loopInterval;
    }


    public void update(float dt, float emitterWorldX, float emitterWorldY, float emitterWorldZ)
    {
        this.worldX = emitterWorldX;
        this.worldY = emitterWorldY;
        this.worldZ = emitterWorldZ;

        if (!this.isFinished)
        {
            emitParticles(dt);
        }

        this.liveCount = 0;
        ParticleData[] slots = this.pool.all();

        for (int i = 0; i < slots.length; i++)
        {
            ParticleData p = slots[i];

            if (!p.isAlive)
            {
                continue;
            }

            p.life -= dt;

            if (p.life <= 0.0F)
            {
                p.isAlive = false;
                continue;
            }

            for (int f = 0; f < this.forces.size(); f++)
            {
                this.forces.get(f).apply(p, dt);
            }

            p.positionX += p.velocityX * dt;
            p.positionY += p.velocityY * dt;
            p.positionZ += p.velocityZ * dt;
            p.rotation += p.rotationVelocity * dt;

            float normalised = 1.0F - (p.life / p.maxLife);

            p.size = this.sizeCurve.evaluate(normalised);
            p.alpha = this.alphaCurve.evaluate(normalised);
            p.colorR = this.colorRCurve.evaluate(normalised);
            p.colorG = this.colorGCurve.evaluate(normalised);
            p.colorB = this.colorBCurve.evaluate(normalised);

            advanceFrame(p, dt);
            this.liveCount++;
        }

        if (this.mode == EmitterMode.ONE_SHOT && this.hasBursted && this.liveCount == 0)
        {
            this.isFinished = true;
        }

        if (this.mode == EmitterMode.LOOPING)
        {
            this.loopTimer -= dt;

            if (this.loopTimer <= 0.0F)
            {
                this.hasBursted = false;
                this.loopTimer = this.loopInterval;
            }
        }

        updateEmissionLight();
    }


    public EmitterLightSnapshot getEmissionLight()
    {
        return this.emissionLight;
    }


    public int fillInstanceBuffer(FloatBuffer buf)
    {
        ParticleData[] slots = this.pool.all();
        int written = 0;

        for (int i = 0; i < slots.length; i++)
        {
            ParticleData p = slots[i];

            if (!p.isAlive)
            {
                continue;
            }

            int totalFrames = this.spriteRows * this.spriteCols;
            int col = (totalFrames > 0) ? (p.frame % this.spriteCols) : 0;
            int row = (totalFrames > 0) ? (p.frame / this.spriteCols) : 0;

            float cellW = (this.spriteCols > 0) ? 1.0F / this.spriteCols : 1.0F;
            float cellH = (this.spriteRows > 0) ? 1.0F / this.spriteRows : 1.0F;

            float u0 = col * cellW;
            float v0 = row * cellH;
            float u1 = u0 + cellW;
            float v1 = v0 + cellH;

            buf.put(p.positionX);
            buf.put(p.positionY);
            buf.put(p.positionZ);
            buf.put(p.size);
            buf.put(p.colorR);
            buf.put(p.colorG);
            buf.put(p.colorB);
            buf.put(p.alpha);
            buf.put(p.rotation);
            buf.put(u0);
            buf.put(v0);
            buf.put(u1);
            buf.put(v1);
            buf.put(0.0F);

            written++;
        }

        return written;
    }

    /**
     * return the number of live particles at the end of the last update tick.
     */
    public int getLiveCount()
    {
        return this.liveCount;
    }


    public boolean isFinished()
    {
        return this.isFinished;
    }

    public String getTexturePath()
    {
        return this.texturePath;
    }


    public boolean isAdditive()
    {
        return this.isAdditive;
    }

    public int getSpriteRows()
    {
        return this.spriteRows;
    }

    public int getSpriteCols()
    {
        return this.spriteCols;
    }

    public int getMaxParticles()
    {
        return this.pool.getCapacity();
    }

    public float getWorldX()
    {
        return this.worldX;
    }

    public float getWorldY()
    {
        return this.worldY;
    }

    public float getWorldZ()
    {
        return this.worldZ;
    }

    private void emitParticles(float dt)
    {
        if (this.mode == EmitterMode.CONTINUOUS)
        {
            this.emissionAccumulator += this.emissionRate * dt;

            while (this.emissionAccumulator >= 1.0F)
            {
                spawnOne();
                this.emissionAccumulator -= 1.0F;
            }
        } else if ((this.mode == EmitterMode.BURST || this.mode == EmitterMode.ONE_SHOT
                || this.mode == EmitterMode.LOOPING)
                && !this.hasBursted)
        {
            int count = this.pool.getCapacity();

            for (int i = 0; i < count; i++)
            {
                spawnOne();
            }

            this.hasBursted = true;

            if (this.mode == EmitterMode.LOOPING)
            {
                this.loopTimer = this.loopInterval;
            }
        }
    }

    private void spawnOne()
    {
        ParticleData p = this.pool.acquire();

        if (p == null)
        {
            return;
        }

        this.shape.spawn(this.spawnPos, this.spawnDir, this.rng);

        p.positionX = this.worldX + this.spawnPos.x;
        p.positionY = this.worldY + this.spawnPos.y;
        p.positionZ = this.worldZ + this.spawnPos.z;

        float speed = this.speedMin + this.rng.nextFloat() * (this.speedMax - this.speedMin);
        p.velocityX = this.spawnDir.x * speed;
        p.velocityY = this.spawnDir.y * speed;
        p.velocityZ = this.spawnDir.z * speed;

        p.maxLife = this.lifetimeMin + this.rng.nextFloat() * (this.lifetimeMax - this.lifetimeMin);
        p.life = p.maxLife;

        p.colorR = this.startColorR;
        p.colorG = this.startColorG;
        p.colorB = this.startColorB;
        p.colorEndR = this.endColorR;
        p.colorEndG = this.endColorG;
        p.colorEndB = this.endColorB;
        p.alpha = this.startAlpha;
        p.size = this.startSize;

        p.rotation = this.rng.nextFloat() * 2.0F * (float) Math.PI;
        p.rotationVelocity = this.rotationVelocityMin
                + this.rng.nextFloat() * (this.rotationVelocityMax - this.rotationVelocityMin);

        p.frame = 0;
        p.frameTimer = 0.0F;
    }

    private void updateEmissionLight()
    {
        if (!this.castsLight || this.liveCount == 0)
        {
            this.emissionLight.active = false;
            return;
        }

        float sumX = 0.0F;
        float sumY = 0.0F;
        float sumZ = 0.0F;
        float sumR = 0.0F;
        float sumG = 0.0F;
        float sumB = 0.0F;
        float sumWeight = 0.0F;

        ParticleData[] slots = this.pool.all();

        for (int i = 0; i < slots.length; i++)
        {
            ParticleData p = slots[i];

            if (!p.isAlive)
            {
                continue;
            }

            float w = p.alpha * p.size * p.size;

            if (w <= 0.0001F)
            {
                continue;
            }

            sumX += p.positionX * w;
            sumY += p.positionY * w;
            sumZ += p.positionZ * w;
            sumR += p.colorR * w;
            sumG += p.colorG * w;
            sumB += p.colorB * w;
            sumWeight += w;
        }

        if (sumWeight <= 0.0001F)
        {
            this.emissionLight.active = false;
            return;
        }

        float inv = 1.0F / sumWeight;
        float cx = sumX * inv;
        float cy = sumY * inv;
        float cz = sumZ * inv;

        float maxDistSq = 0.0F;

        for (int i = 0; i < slots.length; i++)
        {
            ParticleData p = slots[i];

            if (!p.isAlive)
            {
                continue;
            }

            float dx = p.positionX - cx;
            float dy = p.positionY - cy;
            float dz = p.positionZ - cz;
            float distSq = dx * dx + dy * dy + dz * dz;

            if (distSq > maxDistSq)
            {
                maxDistSq = distSq;
            }
        }

        this.emissionLight.active = true;
        this.emissionLight.castsShadow = this.castsShadow;
        this.emissionLight.shadowDirX = this.shadowDirX;
        this.emissionLight.shadowDirY = this.shadowDirY;
        this.emissionLight.shadowDirZ = this.shadowDirZ;
        this.emissionLight.x = cx;
        this.emissionLight.y = cy;
        this.emissionLight.z = cz;
        this.emissionLight.r = sumR * inv;
        this.emissionLight.g = sumG * inv;
        this.emissionLight.b = sumB * inv;
        this.emissionLight.intensity = Math.min(sumWeight * this.lightWeight * 4.0F, 24.0F);
        this.emissionLight.range = Math.min(4.0F + (float) Math.sqrt(maxDistSq) * 2.5F + this.liveCount * 0.04F, 22.0F);

        if (this.castsShadow)
        {
            PointLightShadowMatrixBuilder.update(this.emissionLight, this.shadowDirX, this.shadowDirY, this.shadowDirZ);
        }
    }

    private void advanceFrame(ParticleData p, float dt)
    {
        int totalFrames = this.spriteRows * this.spriteCols;

        if (totalFrames <= 1 || this.spriteFps < FRAME_TIME_MIN)
        {
            return;
        }

        float frameInterval = 1.0F / this.spriteFps;
        p.frameTimer += dt;

        while (p.frameTimer >= frameInterval)
        {
            p.frame = (p.frame + 1) % totalFrames;
            p.frameTimer -= frameInterval;
        }
    }


    public static final class Builder
    {
        private EmitterShape shape = new PointEmitterShape();
        private final ArrayList<ParticleForce> forces = new ArrayList<>();
        private EmitterMode mode = EmitterMode.CONTINUOUS;
        private FloatCurve sizeCurve = FloatCurve.linear(0.3F, 0.6F);
        private FloatCurve alphaCurve = FloatCurve.linear(1.0F, 0.0F);
        private FloatCurve colorRCurve = FloatCurve.constant(1.0F);
        private FloatCurve colorGCurve = FloatCurve.constant(1.0F);
        private FloatCurve colorBCurve = FloatCurve.constant(1.0F);
        private float emissionRate = 20.0F;
        private int maxParticles = 200;
        private float lifetimeMin = 1.0F;
        private float lifetimeMax = 2.0F;
        private float speedMin = 1.0F;
        private float speedMax = 2.0F;
        private float startSize = 0.3F;
        private float endSize = 0.6F;
        private float startColorR = 1.0F;
        private float startColorG = 1.0F;
        private float startColorB = 1.0F;
        private float endColorR = 1.0F;
        private float endColorG = 1.0F;
        private float endColorB = 1.0F;
        private float startAlpha = 1.0F;
        private float endAlpha = 0.0F;
        private float rotationVelocityMin = -1.0F;
        private float rotationVelocityMax = 1.0F;
        private int spriteRows = 1;
        private int spriteCols = 1;
        private float spriteFps = 0.0F;
        private String texturePath = "/textures/particles/default.png";
        private boolean isAdditive = false;
        private boolean castsLight = false;
        private boolean castsShadow = false;
        private float lightWeight = 1.0F;
        private float shadowDirX = 0.0F;
        private float shadowDirY = -1.0F;
        private float shadowDirZ = 0.0F;
        private float loopInterval = 2.0F;

        public Builder shape(EmitterShape shape)
        {
            this.shape = shape;
            return this;
        }

        public Builder addForce(ParticleForce force)
        {
            this.forces.add(force);
            return this;
        }

        public Builder mode(EmitterMode mode)
        {
            this.mode = mode;
            return this;
        }

        public Builder sizeCurve(FloatCurve c)
        {
            this.sizeCurve = c;
            return this;
        }

        public Builder alphaCurve(FloatCurve c)
        {
            this.alphaCurve = c;
            return this;
        }

        public Builder colorRCurve(FloatCurve c)
        {
            this.colorRCurve = c;
            return this;
        }

        public Builder colorGCurve(FloatCurve c)
        {
            this.colorGCurve = c;
            return this;
        }

        public Builder colorBCurve(FloatCurve c)
        {
            this.colorBCurve = c;
            return this;
        }

        public Builder emissionRate(float r)
        {
            this.emissionRate = r;
            return this;
        }

        public Builder maxParticles(int n)
        {
            this.maxParticles = n;
            return this;
        }

        public Builder lifetime(float min, float max)
        {
            this.lifetimeMin = min;
            this.lifetimeMax = max;
            return this;
        }

        public Builder speed(float min, float max)
        {
            this.speedMin = min;
            this.speedMax = max;
            return this;
        }

        public Builder startSize(float s)
        {
            this.startSize = s;
            return this;
        }

        public Builder endSize(float s)
        {
            this.endSize = s;
            return this;
        }

        public Builder startColor(float r, float g, float b)
        {
            this.startColorR = r;
            this.startColorG = g;
            this.startColorB = b;
            return this;
        }

        public Builder endColor(float r, float g, float b)
        {
            this.endColorR = r;
            this.endColorG = g;
            this.endColorB = b;
            return this;
        }

        public Builder startAlpha(float a)
        {
            this.startAlpha = a;
            return this;
        }

        public Builder endAlpha(float a)
        {
            this.endAlpha = a;
            return this;
        }

        public Builder rotationVelocity(float min, float max)
        {
            this.rotationVelocityMin = min;
            this.rotationVelocityMax = max;
            return this;
        }

        public Builder spriteSheet(int rows, int cols, float fps)
        {
            this.spriteRows = rows;
            this.spriteCols = cols;
            this.spriteFps = fps;
            return this;
        }

        public Builder texture(String path)
        {
            this.texturePath = path;
            return this;
        }

        public Builder additive(boolean additive)
        {
            this.isAdditive = additive;
            this.castsLight = additive;
            return this;
        }

        public Builder castsLight(boolean castsLight)
        {
            this.castsLight = castsLight;
            return this;
        }

        public Builder castsShadow(boolean castsShadow)
        {
            this.castsShadow = castsShadow;
            return this;
        }

        public Builder shadowDirection(float x, float y, float z)
        {
            this.shadowDirX = x;
            this.shadowDirY = y;
            this.shadowDirZ = z;
            return this;
        }

        public Builder lightWeight(float weight)
        {
            this.lightWeight = weight;
            return this;
        }

        public Builder loopInterval(float seconds)
        {
            this.loopInterval = seconds;
            return this;
        }

        public ParticleEmitter build()
        {
            return new ParticleEmitter(this);
        }
    }
}
