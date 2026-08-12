package eleeter.unifystudiox.vfx.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eleeter.unifystudiox.vfx.force.VFXForce;
import eleeter.unifystudiox.vfx.parameter.VFXCurve;
import eleeter.unifystudiox.vfx.parameter.VFXGradient;
import eleeter.unifystudiox.vfx.parameter.VFXRandom;

public class VFXEmitter
{

    public enum RenderType
    {SPRITE, RIBBON, RING}

    public enum BlendMode
    {ADDITIVE, NORMAL, MULTIPLY, SUBTRACT, SCREEN}

    public enum EmitterShape
    {POINT, SPHERE, BOX, CIRCLE, CYLINDER, LIGHTNING_BOLT}

    public enum UVMode
    {SINGLE, RANDOM, FLIPBOOK}


    public String name = "Emitter";
    public boolean enabled = true;
    public RenderType renderType = RenderType.SPRITE;
    public BlendMode blendMode = BlendMode.ADDITIVE;

    /* Spawn settings */
    public float spawnRate = 50.0F;
    public int burstCount = 0;
    public float startDelay = 0.0F;
    public float duration = -1.0F;
    public int maxParticles = 500;

    /* Lifetime */

    public VFXRandom lifetime = new VFXRandom(0.5F, 1.5F);

    /* Emitter shape */

    public EmitterShape shape = EmitterShape.POINT;
    public float shapeRadius = 1.0F;
    public float shapeWidth = 1.0F;
    public float shapeHeight = 1.0F;
    public float shapeDepth = 1.0F;

    /* Position offset */

    public VFXRandom offsetX = new VFXRandom(0.0F, 0.0F);
    public VFXRandom offsetY = new VFXRandom(0.0F, 0.0F);
    public VFXRandom offsetZ = new VFXRandom(0.0F, 0.0F);

    /* Velocity */

    public VFXRandom initialSpeed = new VFXRandom(1.0F, 3.0F);
    public float spreadAngle = 30.0F;
    public float dirX = 0.0F, dirY = 1.0F, dirZ = 0.0F;
    public VFXCurve speedOverLifetime = VFXCurve.constant(1.0F);

    /* Rotation */

    public VFXRandom initialRotation = new VFXRandom(0.0F, 360.0F);
    public VFXRandom rotationSpeed = new VFXRandom(-90.0F, 90.0F);
    public boolean alignToVelocity = false;

    /* Size */

    public VFXRandom initialSize = new VFXRandom(0.1F, 0.3F);
    public VFXCurve sizeOverLifetime = VFXCurve.fadeOut();

    /* Color */

    public VFXGradient colorOverLifetime = VFXGradient.white();
    public float[] startColor = {1.0F, 1.0F, 1.0F, 1.0F};


    public int textureId = 0;
    public int uvCols = 1;
    public int uvRows = 1;
    public UVMode uvMode = UVMode.SINGLE;
    public float flipbookFPS = 24.0F;
    public float uvScrollX = 0.0F;
    public float uvScrollY = 0.0F;


    public float gravity = 0.0F;
    public float drag = 0.02F;
    public boolean collideFloor = false;
    public float floorY = 0.0F;
    public float bounciness = 0.3F;


    public boolean turbulenceEnabled = false;
    public float turbulenceStrength = 1.0F;
    public float turbulenceFreq = 1.0F;


    public int ribbonHistory = 20;
    public float ribbonWidth = 0.1F;
    public VFXCurve ribbonWidthCurve = VFXCurve.constant(1.0F);


    public float ringRadius = 1.0F;
    public float ringThickness = 0.1F;
    public int ringSegments = 32;


    public boolean distortionEnabled = false;
    public float distortionStrength = 0.05F;
    public int distortionTexId = 0;


    public final List<VFXForce> forces = new ArrayList<>();


    private float emitTimer = 0.0F;
    private float elapsedTime = 0.0F;
    private boolean burstFired = false;
    private final Random rand = new Random();

    /**
     * Pre-allocated particle pool - size fixed at construction time.
     */
    public final VFXParticle[] pool;

    /**
     * Number of live particles at the front of the pool array.
     */
    public int activeCount = 0;

    public VFXEmitter()
    {
        this.pool = new VFXParticle[this.maxParticles];
        for (int i = 0; i < this.pool.length; i++)
        {
            this.pool[i] = new VFXParticle();
        }
    }



    public void update(float dt, float worldX, float worldY, float worldZ)
    {
        if (!this.enabled)
        {
            return;
        }

        this.elapsedTime += dt;

        if (this.elapsedTime < this.startDelay)
        {
            return;
        }

        float activeElapsed = this.elapsedTime - this.startDelay;

        if (!this.burstFired && this.burstCount > 0)
        {
            this.burstFired = true;
            for (int i = 0; i < this.burstCount; i++)
            {
                spawnParticle(worldX, worldY, worldZ);
            }
        }

        boolean canSpawn = this.duration < 0.0F || activeElapsed < this.duration;
        if (canSpawn && this.spawnRate > 0.0F)
        {
            this.emitTimer += dt;
            float interval = 1.0F / this.spawnRate;
            while (this.emitTimer >= interval)
            {
                spawnParticle(worldX, worldY, worldZ);
                this.emitTimer -= interval;
            }
        }

        for (int i = 0; i < this.activeCount; i++)
        {
            VFXParticle p = this.pool[i];

            p.life -= dt;
            if (p.life <= 0.0F)
            {
                this.pool[i] = this.pool[this.activeCount - 1];
                this.pool[this.activeCount - 1] = p;
                p.alive = false;
                this.activeCount--;
                i--;
                continue;
            }

            p.normalizedLife = 1.0F - (p.life / p.maxLife);

            p.vy += p.ay * dt;
            p.vx += p.ax * dt;
            p.vz += p.az * dt;

            p.vx *= (1.0F - this.drag);
            p.vy *= (1.0F - this.drag);
            p.vz *= (1.0F - this.drag);

            float speedMult = this.speedOverLifetime.evaluate(p.normalizedLife);
            p.vx *= speedMult;
            p.vy *= speedMult;
            p.vz *= speedMult;

            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.z += p.vz * dt;

            if (this.collideFloor && p.y < this.floorY)
            {
                p.y = this.floorY;
                p.vy = -p.vy * this.bounciness;
                p.vx *= 0.8F;
                p.vz *= 0.8F;
            }

            if (this.turbulenceEnabled)
            {
                float t = this.elapsedTime;
                p.vx += summonTheNoise(p.x * this.turbulenceFreq, p.y * this.turbulenceFreq, p.z * this.turbulenceFreq, t, 0.0F) * this.turbulenceStrength * dt;
                p.vy += summonTheNoise(p.x * this.turbulenceFreq, p.y * this.turbulenceFreq, p.z * this.turbulenceFreq, t, 100.0F) * this.turbulenceStrength * dt;
                p.vz += summonTheNoise(p.x * this.turbulenceFreq, p.y * this.turbulenceFreq, p.z * this.turbulenceFreq, t, 200.0F) * this.turbulenceStrength * dt;
            }

            for (VFXForce f : this.forces)
            {
                f.apply(p, this.elapsedTime, dt);
            }

            p.rotation += p.rotSpeed * dt;
            if (this.alignToVelocity)
            {
                p.rotation = (float) Math.atan2(p.vy, p.vx);
            }

            float sizeMult = this.sizeOverLifetime.evaluate(p.normalizedLife);
            p.scaleX = p.baseScaleX * sizeMult;
            p.scaleY = p.baseScaleY * sizeMult;

            float[] c = this.colorOverLifetime.evaluate(p.normalizedLife);
            p.r = c[0] * this.startColor[0];
            p.g = c[1] * this.startColor[1];
            p.b = c[2] * this.startColor[2];
            p.a = c[3] * this.startColor[3];

            p.speed = (float) Math.sqrt(p.vx * p.vx + p.vy * p.vy + p.vz * p.vz);

            updateUV(p, dt);

            if (this.renderType == RenderType.RIBBON)
            {
                saveThat(p);
            }
        }
    }

    /**
     * Resets this emitter back to its initial state for re-use.
     */
    public void reset()
    {
        this.emitTimer = 0.0F;
        this.elapsedTime = 0.0F;
        this.burstFired = false;
        this.activeCount = 0;
        for (VFXParticle p : this.pool)
        {
            p.alive = false;
        }
    }


    private void spawnParticle(float wx, float wy, float wz)
    {
        if (this.activeCount >= this.pool.length)
        {
            return;
        }

        VFXParticle p = this.pool[this.activeCount++];
        p.alive = true;

        float[] spawnOffset = sampleShape();
        p.x = wx + spawnOffset[0] + this.offsetX.sample();
        p.y = wy + spawnOffset[1] + this.offsetY.sample();
        p.z = wz + spawnOffset[2] + this.offsetZ.sample();

        float speed = this.initialSpeed.sample();
        float[] vel = spVelocity(this.dirX, this.dirY, this.dirZ, this.spreadAngle, speed);
        p.vx = vel[0];
        p.vy = vel[1];
        p.vz = vel[2];

        p.ax = 0.0F;
        p.ay = -this.gravity;
        p.az = 0.0F;

        p.maxLife = this.lifetime.sample();
        p.life = p.maxLife;
        p.normalizedLife = 0.0F;

        float sz = this.initialSize.sample();
        p.baseScaleX = sz;
        p.baseScaleY = sz;
        p.scaleX = sz;
        p.scaleY = sz;

        p.rotation = (float) Math.toRadians(this.initialRotation.sample());
        p.rotSpeed = (float) Math.toRadians(this.rotationSpeed.sample());

        float[] c = this.colorOverLifetime.evaluate(0.0F);
        p.r = c[0] * this.startColor[0];
        p.g = c[1] * this.startColor[1];
        p.b = c[2] * this.startColor[2];
        p.a = c[3] * this.startColor[3];

        UV(p);

        if (this.renderType == RenderType.RIBBON)
        {
            p.histX = new float[this.ribbonHistory];
            p.histY = new float[this.ribbonHistory];
            p.histZ = new float[this.ribbonHistory];
            p.histHead = 0;
            p.histLen = 0;

            if (this.shape == EmitterShape.LIGHTNING_BOLT)
            {
                p.histLen = this.ribbonHistory;
                float seed = this.elapsedTime * 100.0f;
                for (int i = 0; i < this.ribbonHistory; i++)
                {
                    float progress = (float) i / (this.ribbonHistory - 1);
                    float baseY = this.shapeHeight * (1.0f - progress);

                    float jaggedX = (float) Math.sin(baseY * 2.0f + seed) * this.shapeWidth + (float) Math.cos(baseY * 5.3f - seed) * (this.shapeWidth * 0.5f);
                    float jaggedZ = (float) Math.cos(baseY * 2.1f + seed) * this.shapeWidth + (float) Math.sin(baseY * 4.7f - seed) * (this.shapeWidth * 0.5f);

                    p.histX[i] = p.x + jaggedX;
                    p.histY[i] = p.y + baseY - this.shapeHeight;
                    p.histZ[i] = p.z + jaggedZ;
                }
            }
        }
    }

    /* Shape sampling */

    private float[] sampleShape()
    {
        switch (this.shape)
        {
            case SPHERE:
            {
                float theta = (float) (this.rand.nextFloat() * Math.PI * 2.0D);
                float phi = (float) Math.acos(2.0F * this.rand.nextFloat() - 1.0F);
                float r = this.shapeRadius * (float) Math.cbrt(this.rand.nextFloat());
                float sinPhi = (float) Math.sin(phi);
                return new float[]
                        {
                                r * sinPhi * (float) Math.cos(theta),
                                r * (float) Math.cos(phi),
                                r * sinPhi * (float) Math.sin(theta)
                        };
            }
            case BOX:
            {
                return new float[]
                        {
                                (this.rand.nextFloat() * 2.0F - 1.0F) * this.shapeWidth,
                                (this.rand.nextFloat() * 2.0F - 1.0F) * this.shapeHeight,
                                (this.rand.nextFloat() * 2.0F - 1.0F) * this.shapeDepth
                        };
            }
            case CIRCLE:
            {
                float angle = this.rand.nextFloat() * (float) (Math.PI * 2.0D);
                float r = this.shapeRadius * (float) Math.sqrt(this.rand.nextFloat());
                return new float[]
                        {
                                r * (float) Math.cos(angle),
                                0.0F,
                                r * (float) Math.sin(angle)
                        };
            }
            case CYLINDER:
            {
                float angle = this.rand.nextFloat() * (float) (Math.PI * 2.0D);
                float r = this.shapeRadius * (float) Math.sqrt(this.rand.nextFloat());
                return new float[]
                        {
                                r * (float) Math.cos(angle),
                                (this.rand.nextFloat() * 2.0F - 1.0F) * this.shapeHeight,
                                r * (float) Math.sin(angle)
                        };
            }
            case LIGHTNING_BOLT:
            {
                int index = Math.max(1, this.activeCount);
                float progress = (float) index / Math.max(1, this.maxParticles);

                float baseY = this.shapeHeight * (1.0f - progress);

                float seed = this.elapsedTime * 100.0f; // Changes each time the emitter fires
                float jaggedX = (float) Math.sin(baseY * 2.0f + seed) * this.shapeWidth + (float) Math.cos(baseY * 5.3f - seed) * (this.shapeWidth * 0.5f);
                float jaggedZ = (float) Math.cos(baseY * 2.1f + seed) * this.shapeWidth + (float) Math.sin(baseY * 4.7f - seed) * (this.shapeWidth * 0.5f);

                return new float[]{jaggedX, baseY, jaggedZ};
            }
            default:
            {
                return new float[]{0.0F, 0.0F, 0.0F};
            }
        }
    }


    private float[] spVelocity(float dx, float dy, float dz, float angleDeg, float speed)
    {
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1e-6F)
        {
            dx = 0.0F;
            dy = 1.0F;
            dz = 0.0F;
        } else
        {
            dx /= len;
            dy /= len;
            dz /= len;
        }

        float halfAngle = (float) Math.toRadians(angleDeg * 0.5F);
        float cosMax = (float) Math.cos(halfAngle);
        float cosTheta = cosMax + this.rand.nextFloat() * (1.0F - cosMax);
        float sinTheta = (float) Math.sqrt(1.0F - cosTheta * cosTheta);
        float phi = this.rand.nextFloat() * (float) (Math.PI * 2.0D);

        float perpX, perpY, perpZ;
        if (Math.abs(dx) < 0.9F)
        {
            perpX = 0.0F;
            perpY = dz;
            perpZ = -dy;
        } else
        {
            perpX = -dz;
            perpY = 0.0F;
            perpZ = dx;
        }

        float pLen = (float) Math.sqrt(perpX * perpX + perpY * perpY + perpZ * perpZ);
        perpX /= pLen;
        perpY /= pLen;
        perpZ /= pLen;

        float bx = dy * perpZ - dz * perpY;
        float by = dz * perpX - dx * perpZ;
        float bz = dx * perpY - dy * perpX;

        float cosPhi = (float) Math.cos(phi);
        float sinPhi = (float) Math.sin(phi);

        float nx = sinTheta * cosPhi * perpX + sinTheta * sinPhi * bx + cosTheta * dx;
        float ny = sinTheta * cosPhi * perpY + sinTheta * sinPhi * by + cosTheta * dy;
        float nz = sinTheta * cosPhi * perpZ + sinTheta * sinPhi * bz + cosTheta * dz;

        return new float[]{nx * speed, ny * speed, nz * speed};
    }


    private void UV(VFXParticle horror)
    {
        float cellW = 1.0F / this.uvCols;
        float cellH = 1.0F / this.uvRows;

        if (this.uvMode == UVMode.RANDOM)
        {
            int col = this.rand.nextInt(this.uvCols);
            int row = this.rand.nextInt(this.uvRows);
            horror.currentFrame = row * this.uvCols + col;
            horror.u0 = col * cellW;
            horror.v0 = row * cellH;
            horror.u1 = horror.u0 + cellW;
            horror.v1 = horror.v0 + cellH;
        } else
        {
            horror.currentFrame = 0;
            horror.u0 = 0.0F;
            horror.v0 = 0.0F;
            horror.u1 = cellW;
            horror.v1 = cellH;
        }
    }

    private void updateUV(VFXParticle p, float dt)
    {
        int totalFrames = this.uvCols * this.uvRows;
        float cellW = 1.0F / this.uvCols;
        float cellH = 1.0F / this.uvRows;

        if (this.uvMode == UVMode.FLIPBOOK && totalFrames > 1)
        {
            int frame = (int) (p.normalizedLife * totalFrames * (p.maxLife * this.flipbookFPS / totalFrames));
            frame = Math.min(frame % totalFrames, totalFrames - 1);
            if (frame != p.currentFrame)
            {
                p.currentFrame = frame;
                int col = frame % this.uvCols;
                int row = frame / this.uvCols;
                p.u0 = col * cellW + this.uvScrollX * this.elapsedTime;
                p.v0 = row * cellH + this.uvScrollY * this.elapsedTime;
                p.u1 = p.u0 + cellW;
                p.v1 = p.v0 + cellH;
            }
        } else if (this.uvScrollX != 0.0F || this.uvScrollY != 0.0F)
        {
            p.u0 += this.uvScrollX * dt;
            p.v0 += this.uvScrollY * dt;
            p.u1 += this.uvScrollX * dt;
            p.v1 += this.uvScrollY * dt;
        }
    }


    private void saveThat(VFXParticle p)
    {
        if (p.histX == null)
        {
            return;
        }

        p.histX[p.histHead] = p.x;
        p.histY[p.histHead] = p.y;
        p.histZ[p.histHead] = p.z;
        p.histHead = (p.histHead + 1) % p.histX.length;
        if (p.histLen < p.histX.length)
        {
            p.histLen++;
        }
    }


    private float summonTheNoise(float x, float y, float z, float t, float seed)
    {
        int xi = (int) Math.floor(x + seed);
        int yi = (int) Math.floor(y + seed);
        int zi = (int) Math.floor(z + seed);
        int ti = (int) Math.floor(t + seed);

        float xf = x + seed - xi;
        float yf = y + seed - yi;
        float zf = z + seed - zi;
        float tf = t + seed - ti;

        float u = smoothTheHeckOut(xf);
        float v = smoothTheHeckOut(yf);
        float s = smoothTheHeckOut(zf);

        float c000 = grad4(hash4(xi, yi, zi, ti), xf, yf, zf, tf);
        float c100 = grad4(hash4(xi + 1, yi, zi, ti), xf - 1.0F, yf, zf, tf);
        float c010 = grad4(hash4(xi, yi + 1, zi, ti), xf, yf - 1.0F, zf, tf);
        float c110 = grad4(hash4(xi + 1, yi + 1, zi, ti), xf - 1.0F, yf - 1.0F, zf, tf);
        float c001 = grad4(hash4(xi, yi, zi + 1, ti), xf, yf, zf - 1.0F, tf);
        float c101 = grad4(hash4(xi + 1, yi, zi + 1, ti), xf - 1.0F, yf, zf - 1.0F, tf);
        float c011 = grad4(hash4(xi, yi + 1, zi + 1, ti), xf, yf - 1.0F, zf - 1.0F, tf);
        float c111 = grad4(hash4(xi + 1, yi + 1, zi + 1, ti), xf - 1.0F, yf - 1.0F, zf - 1.0F, tf);

        return middleMan(middleMan(middleMan(c000, c100, u), middleMan(c010, c110, u), v), middleMan(middleMan(c001, c101, u), middleMan(c011, c111, u), v), s);
    }

    private static float smoothTheHeckOut(float t)
    {
        return t * t * t * (t * (t * 6.0F - 15.0F) + 10.0F);
    }

    private static float middleMan(float a, float b, float t)
    {
        return a + t * (b - a);
    }

    private static int hash4(int x, int y, int z, int w)
    {
        int h = x * 1619 ^ y * 31337 ^ z * 6971 ^ w * 1013;
        h = h ^ (h >> 8);
        return h & 0xFF;
    }

    private static float grad4(int hash, float x, float y, float z, float w)
    {
        int h = hash & 7;
        float u = (h < 4) ? x : y;
        float v = (h < 4) ? y : z;
        return ((h & 1) != 0 ? -u : u) + ((h & 2) != 0 ? -v : v);
    }
}
