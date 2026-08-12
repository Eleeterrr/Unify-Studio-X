package eleeter.unifystudiox.particle.emitter.shape;

import java.util.Random;

import org.joml.Vector3f;

import eleeter.unifystudiox.particle.api.EmitterShape;

public class MeshSurfaceEmitterShape implements EmitterShape
{
    private final float[] positions;
    private final float[] cdf;
    private final float[] faceNormalX;
    private final float[] faceNormalY;
    private final float[] faceNormalZ;

    private static final int VERTS_PER_TRI = 3;
    private static final int FLOATS_PER_VERT = 3;
    private static final int FLOATS_PER_TRI = VERTS_PER_TRI * FLOATS_PER_VERT;


    public MeshSurfaceEmitterShape(float[] positions)
    {
        this.positions = positions;
        int triCount = positions.length / FLOATS_PER_TRI;

        this.cdf = new float[triCount];
        this.faceNormalX = new float[triCount];
        this.faceNormalY = new float[triCount];
        this.faceNormalZ = new float[triCount];

        float total = 0.0F;

        for (int i = 0; i < triCount; i++)
        {
            int base = i * FLOATS_PER_TRI;
            float ax = positions[base], ay = positions[base + 1], az = positions[base + 2];
            float bx = positions[base + 3], by = positions[base + 4], bz = positions[base + 5];
            float cx = positions[base + 6], cy = positions[base + 7], cz = positions[base + 8];

            float abX = bx - ax, abY = by - ay, abZ = bz - az;
            float acX = cx - ax, acY = cy - ay, acZ = cz - az;

            float crossX = abY * acZ - abZ * acY;
            float crossY = abZ * acX - abX * acZ;
            float crossZ = abX * acY - abY * acX;

            float area = (float) Math.sqrt(crossX * crossX + crossY * crossY + crossZ * crossZ) * 0.5F;
            total += area;
            this.cdf[i] = total;

            float invLen = area > 0.0F ? 1.0F / (area * 2.0F) : 0.0F;
            this.faceNormalX[i] = crossX * invLen;
            this.faceNormalY[i] = crossY * invLen;
            this.faceNormalZ[i] = crossZ * invLen;
        }

        for (int i = 0; i < triCount; i++)
        {
            this.cdf[i] /= total;
        }
    }

    @Override
    public void spawn(Vector3f outPosition, Vector3f outDirection, Random rng)
    {
        int tri = lowerBound(rng.nextFloat());
        int base = tri * FLOATS_PER_TRI;

        float ax = this.positions[base], ay = this.positions[base + 1], az = this.positions[base + 2];
        float bx = this.positions[base + 3], by = this.positions[base + 4], bz = this.positions[base + 5];
        float cx = this.positions[base + 6], cy = this.positions[base + 7], cz = this.positions[base + 8];

        float r1 = rng.nextFloat();
        float r2 = rng.nextFloat();

        if (r1 + r2 > 1.0F)
        {
            r1 = 1.0F - r1;
            r2 = 1.0F - r2;
        }

        float r3 = 1.0F - r1 - r2;

        outPosition.set(
                r3 * ax + r1 * bx + r2 * cx,
                r3 * ay + r1 * by + r2 * cy,
                r3 * az + r1 * bz + r2 * cz
        );

        outDirection.set(this.faceNormalX[tri], this.faceNormalY[tri], this.faceNormalZ[tri]);
    }

    private int lowerBound(float sample)
    {
        int lo = 0;
        int hi = this.cdf.length - 1;

        while (lo < hi)
        {
            int mid = (lo + hi) >>> 1;

            if (this.cdf[mid] < sample)
            {
                lo = mid + 1;
            } else
            {
                hi = mid;
            }
        }

        return lo;
    }
}
