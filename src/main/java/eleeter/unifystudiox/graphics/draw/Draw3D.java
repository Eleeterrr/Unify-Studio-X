package eleeter.unifystudiox.graphics.draw;

import java.util.List;

import org.joml.Vector3f;

import eleeter.unifystudiox.graphics.math.TransformStack;

public class Draw3D
{
    private static final float TWO_PI = (float) (2.0 * Math.PI);

    private Draw3D()
    {
    }


    private static void emit(List<Float> list, float x, float y, float z, float r, float g, float b)
    {
        list.add(x);
        list.add(y);
        list.add(z);
        list.add(r);
        list.add(g);
        list.add(b);
    }

    private static void emitTransformed(List<Float> list, TransformStack stack, float x, float y, float z, float r, float g, float b)
    {
        Vector3f transformed = new Vector3f(x, y, z).mulPosition(stack.peek());
        emit(list, transformed.x, transformed.y, transformed.z, r, g, b);
    }


    /**
     * Appends a single 3D line segment (2 vertices, for GL_LINES).
     */
    public static void line(List<Float> list, Vector3f start, Vector3f end, float r, float g, float b)
    {
        emit(list, start.x, start.y, start.z, r, g, b);
        emit(list, end.x, end.y, end.z, r, g, b);
    }

    /**
     * Appends a single 3D line segment transformed by the top of the given stack.
     */
    public static void line(List<Float> list, TransformStack stack, Vector3f start, Vector3f end, float r, float g, float b)
    {
        emitTransformed(list, stack, start.x, start.y, start.z, r, g, b);
        emitTransformed(list, stack, end.x, end.y, end.z, r, g, b);
    }


    /**
     * Appends three axis lines from the origin: X = red, Y = green, Z = blue.
     */
    public static void axisLines(List<Float> list, float length)
    {
        emit(list, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F);
        emit(list, length, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F);

        emit(list, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F);
        emit(list, 0.0F, length, 0.0F, 0.0F, 1.0F, 0.0F);

        emit(list, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
        emit(list, 0.0F, 0.0F, length, 0.0F, 0.0F, 1.0F);
    }


    public static void axisLines(List<Float> list, TransformStack stack, float length)
    {
        emitTransformed(list, stack, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F);
        emitTransformed(list, stack, length, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F);

        emitTransformed(list, stack, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F);
        emitTransformed(list, stack, 0.0F, length, 0.0F, 0.0F, 1.0F, 0.0F);

        emitTransformed(list, stack, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
        emitTransformed(list, stack, 0.0F, 0.0F, length, 0.0F, 0.0F, 1.0F);
    }




    public static void cylinder(List<Float> list, Vector3f start, Vector3f end, float radius, int segments, float r, float g, float b)
    {
        Vector3f dir = new Vector3f(end).sub(start).normalize();
        Vector3f u = buildPerp(dir);
        Vector3f v = new Vector3f(dir).cross(u).normalize();

        for (int i = 0; i < segments; i++)
        {
            float a1 = i * TWO_PI / segments;
            float a2 = (i + 1) * TWO_PI / segments;

            float cos1 = (float) Math.cos(a1), sin1 = (float) Math.sin(a1);
            float cos2 = (float) Math.cos(a2), sin2 = (float) Math.sin(a2);

            Vector3f p1 = new Vector3f(u).mul(cos1 * radius).add(new Vector3f(v).mul(sin1 * radius));
            Vector3f p2 = new Vector3f(u).mul(cos2 * radius).add(new Vector3f(v).mul(sin2 * radius));

            Vector3f s1 = new Vector3f(start).add(p1);
            Vector3f s2 = new Vector3f(start).add(p2);
            Vector3f e1 = new Vector3f(end).add(p1);
            Vector3f e2 = new Vector3f(end).add(p2);

            emit(list, s1.x, s1.y, s1.z, r, g, b);
            emit(list, s2.x, s2.y, s2.z, r, g, b);
            emit(list, e2.x, e2.y, e2.z, r, g, b);

            emit(list, s1.x, s1.y, s1.z, r, g, b);
            emit(list, e2.x, e2.y, e2.z, r, g, b);
            emit(list, e1.x, e1.y, e1.z, r, g, b);
        }
    }


    public static void cylinder(List<Float> list, TransformStack stack, Vector3f start, Vector3f end, float radius, int segments, float r, float g, float b)
    {
        Vector3f worldStart = new Vector3f(start).mulPosition(stack.peek());
        Vector3f worldEnd = new Vector3f(end).mulPosition(stack.peek());
        cylinder(list, worldStart, worldEnd, radius, segments, r, g, b);
    }



    public static void cone(List<Float> list, Vector3f base, Vector3f tip, float radius, int segments, float r, float g, float b)
    {
        Vector3f dir = new Vector3f(tip).sub(base).normalize();
        Vector3f u = buildPerp(dir);
        Vector3f v = new Vector3f(dir).cross(u).normalize();

        for (int i = 0; i < segments; i++)
        {
            float a1 = i * TWO_PI / segments;
            float a2 = (i + 1) * TWO_PI / segments;

            float cos1 = (float) Math.cos(a1), sin1 = (float) Math.sin(a1);
            float cos2 = (float) Math.cos(a2), sin2 = (float) Math.sin(a2);

            Vector3f p1 = new Vector3f(u).mul(cos1 * radius).add(new Vector3f(v).mul(sin1 * radius));
            Vector3f p2 = new Vector3f(u).mul(cos2 * radius).add(new Vector3f(v).mul(sin2 * radius));

            Vector3f b1 = new Vector3f(base).add(p1);
            Vector3f b2 = new Vector3f(base).add(p2);

            emit(list, b1.x, b1.y, b1.z, r, g, b);
            emit(list, b2.x, b2.y, b2.z, r, g, b);
            emit(list, tip.x, tip.y, tip.z, r, g, b);
        }
    }


    public static void cone(List<Float> list, TransformStack stack, Vector3f base, Vector3f tip, float radius, int segments, float r, float g, float b)
    {
        Vector3f worldBase = new Vector3f(base).mulPosition(stack.peek());
        Vector3f worldTip = new Vector3f(tip).mulPosition(stack.peek());
        cone(list, worldBase, worldTip, radius, segments, r, g, b);
    }


    public static void wireCone(List<Float> list, Vector3f baseCenter, Vector3f tip, float radius, int segments, float r, float g, float b)
    {
        Vector3f dir = new Vector3f(tip).sub(baseCenter).normalize();
        Vector3f u = buildPerp(dir);
        Vector3f v = new Vector3f(dir).cross(u).normalize();

        for (int i = 0; i < segments; i++)
        {
            float a1 = i * TWO_PI / segments;
            float a2 = (i + 1) * TWO_PI / segments;

            float cos1 = (float) Math.cos(a1), sin1 = (float) Math.sin(a1);
            float cos2 = (float) Math.cos(a2), sin2 = (float) Math.sin(a2);

            Vector3f p1 = new Vector3f(u).mul(cos1 * radius).add(new Vector3f(v).mul(sin1 * radius));
            Vector3f p2 = new Vector3f(u).mul(cos2 * radius).add(new Vector3f(v).mul(sin2 * radius));

            Vector3f b1 = new Vector3f(baseCenter).add(p1);
            Vector3f b2 = new Vector3f(baseCenter).add(p2);

            // Circle segment at base
            emit(list, b1.x, b1.y, b1.z, r, g, b);
            emit(list, b2.x, b2.y, b2.z, r, g, b);

            emit(list, b1.x, b1.y, b1.z, r, g, b);
            emit(list, tip.x, tip.y, tip.z, r, g, b);
        }
    }


    public static void wireCone(List<Float> list, TransformStack stack, Vector3f baseCenter, Vector3f tip, float radius, int segments, float r, float g, float b)
    {
        Vector3f worldBase = new Vector3f(baseCenter).mulPosition(stack.peek());
        Vector3f worldTip = new Vector3f(tip).mulPosition(stack.peek());
        wireCone(list, worldBase, worldTip, radius, segments, r, g, b);
    }




    public static void wireSphere(List<Float> list, Vector3f center, float radius, int segments, float r, float g, float b)
    {
        for (int i = 0; i < segments; i++)
        {
            float a1 = i * TWO_PI / segments;
            float a2 = (i + 1) * TWO_PI / segments;

            float cos1 = (float) Math.cos(a1), sin1 = (float) Math.sin(a1);
            float cos2 = (float) Math.cos(a2), sin2 = (float) Math.sin(a2);

            /* XY plane */
            emit(list, center.x + cos1 * radius, center.y + sin1 * radius, center.z, r, g, b);
            emit(list, center.x + cos2 * radius, center.y + sin2 * radius, center.z, r, g, b);

            /* XZ plane */
            emit(list, center.x + cos1 * radius, center.y, center.z + sin1 * radius, r, g, b);
            emit(list, center.x + cos2 * radius, center.y, center.z + sin2 * radius, r, g, b);

            /* YZ plane */
            emit(list, center.x, center.y + cos1 * radius, center.z + sin1 * radius, r, g, b);
            emit(list, center.x, center.y + cos2 * radius, center.z + sin2 * radius, r, g, b);
        }
    }


    public static void wireSphere(List<Float> list, TransformStack stack, Vector3f center, float radius, int segments, float r, float g, float b)
    {
        Vector3f worldCenter = new Vector3f(center).mulPosition(stack.peek());
        wireSphere(list, worldCenter, radius, segments, r, g, b);
    }


    public static void box(List<Float> list, Vector3f center, float size, float r, float g, float b)
    {
        float h = size * 0.5F;
        float cx = center.x, cy = center.y, cz = center.z;

        Vector3f[] corners =
        {
            new Vector3f(cx - h, cy - h, cz - h), new Vector3f(cx + h, cy - h, cz - h),
            new Vector3f(cx + h, cy + h, cz - h), new Vector3f(cx - h, cy + h, cz - h),
            new Vector3f(cx - h, cy - h, cz + h), new Vector3f(cx + h, cy - h, cz + h),
            new Vector3f(cx + h, cy + h, cz + h), new Vector3f(cx - h, cy + h, cz + h)
        };

        int[] indices = { 0, 1, 2, 0, 2, 3, 4, 5, 6, 4, 6, 7, 0, 1, 5, 0, 5, 4, 2, 3, 7, 2, 7, 6, 0, 3, 7, 0, 7, 4, 1, 2, 6, 1, 6, 5 };

        for (int idx : indices)
        {
            emit(list, corners[idx].x, corners[idx].y, corners[idx].z, r, g, b);
        }
    }

    public static void box(List<Float> list, TransformStack stack, Vector3f center, float size, float r, float g, float b)
    {
        float h = size * 0.5F;
        float cx = center.x, cy = center.y, cz = center.z;

        Vector3f[] corners =
        {
            new Vector3f(cx - h, cy - h, cz - h), new Vector3f(cx + h, cy - h, cz - h),
            new Vector3f(cx + h, cy + h, cz - h), new Vector3f(cx - h, cy + h, cz - h),
            new Vector3f(cx - h, cy - h, cz + h), new Vector3f(cx + h, cy - h, cz + h),
            new Vector3f(cx + h, cy + h, cz + h), new Vector3f(cx - h, cy + h, cz + h)
        };

        int[] indices = { 0, 1, 2, 0, 2, 3, 4, 5, 6, 4, 6, 7, 0, 1, 5, 0, 5, 4, 2, 3, 7, 2, 7, 6, 0, 3, 7, 0, 7, 4, 1, 2, 6, 1, 6, 5 };

        for (int idx : indices)
        {
            emitTransformed(list, stack, corners[idx].x, corners[idx].y, corners[idx].z, r, g, b);
        }
    }


    public static void arc(List<Float> list, char axis, float radius, float startAngle, float endAngle, int segments, float r, float g, float b)
    {
        float prev_x = 0.0F, prev_y = 0.0F, prev_z = 0.0F;
        boolean first = true;

        for (int i = 0; i <= segments; i++)
        {
            float angle = startAngle + (endAngle - startAngle) * i / segments;
            float cos = (float) Math.cos(angle) * radius;
            float sin = (float) Math.sin(angle) * radius;

            float px, py, pz;
            if (axis == 'X')
            {
                px = 0.0F; py = cos; pz = sin;
            }
            else if (axis == 'Y')
            {
                px = cos; py = 0.0F; pz = sin;
            }
            else
            {
                px = cos; py = sin; pz = 0.0F;
            }

            if (!first)
            {
                emit(list, prev_x, prev_y, prev_z, r, g, b);
                emit(list, px, py, pz, r, g, b);
            }

            prev_x = px;
            prev_y = py;
            prev_z = pz;
            first = false;
        }
    }

    public static void arc(List<Float> list, TransformStack stack, char axis, float radius, float startAngle, float endAngle, int segments, float r, float g, float b)
    {
        float prev_x = 0.0F, prev_y = 0.0F, prev_z = 0.0F;
        boolean first = true;

        for (int i = 0; i <= segments; i++)
        {
            float angle = startAngle + (endAngle - startAngle) * i / segments;
            float cos = (float) Math.cos(angle) * radius;
            float sin = (float) Math.sin(angle) * radius;

            float px, py, pz;
            if (axis == 'X')
            {
                px = 0.0F; py = cos; pz = sin;
            }
            else if (axis == 'Y')
            {
                px = cos; py = 0.0F; pz = sin;
            }
            else
            {
                px = cos; py = sin; pz = 0.0F;
            }

            if (!first)
            {
                emitTransformed(list, stack, prev_x, prev_y, prev_z, r, g, b);
                emitTransformed(list, stack, px, py, pz, r, g, b);
            }

            prev_x = px;
            prev_y = py;
            prev_z = pz;
            first = false;
        }
    }


    public static void ring(List<Float> list, char axis, float radius, float tubeRadius, int ringSegments, int tubeSegments, float r, float g, float b)
    {
        for (int i = 0; i < ringSegments; i++)
        {
            float a1 = i * TWO_PI / ringSegments;
            float a2 = (i + 1) * TWO_PI / ringSegments;

            for (int j = 0; j < tubeSegments; j++)
            {
                float b1 = j * TWO_PI / tubeSegments;
                float b2 = (j + 1) * TWO_PI / tubeSegments;

                Vector3f[] p = new Vector3f[4];
                float[] anglesA = { a1, a1, a2, a2 };
                float[] anglesB = { b1, b2, b2, b1 };

                for (int k = 0; k < 4; k++)
                {
                    float cosA = (float) Math.cos(anglesA[k]);
                    float sinA = (float) Math.sin(anglesA[k]);
                    float cosB = (float) Math.cos(anglesB[k]);
                    float sinB = (float) Math.sin(anglesB[k]);

                    float tr = radius + cosB * tubeRadius;
                    float ty = sinB * tubeRadius;

                    if (axis == 'X')
                    {
                        p[k] = new Vector3f(ty, cosA * tr, sinA * tr);
                    }
                    else if (axis == 'Y')
                    {
                        p[k] = new Vector3f(cosA * tr, ty, sinA * tr);
                    }
                    else
                    {
                        p[k] = new Vector3f(cosA * tr, sinA * tr, ty);
                    }
                }

                emit(list, p[0].x, p[0].y, p[0].z, r, g, b);
                emit(list, p[1].x, p[1].y, p[1].z, r, g, b);
                emit(list, p[2].x, p[2].y, p[2].z, r, g, b);

                emit(list, p[0].x, p[0].y, p[0].z, r, g, b);
                emit(list, p[2].x, p[2].y, p[2].z, r, g, b);
                emit(list, p[3].x, p[3].y, p[3].z, r, g, b);

                emit(list, p[0].x, p[0].y, p[0].z, r, g, b);
                emit(list, p[2].x, p[2].y, p[2].z, r, g, b);
                emit(list, p[1].x, p[1].y, p[1].z, r, g, b);

                emit(list, p[0].x, p[0].y, p[0].z, r, g, b);
                emit(list, p[3].x, p[3].y, p[3].z, r, g, b);
                emit(list, p[2].x, p[2].y, p[2].z, r, g, b);
            }
        }
    }


    public static void ring(List<Float> list, TransformStack stack, char axis, float radius, float tubeRadius, int ringSegments, int tubeSegments, float r, float g, float b)
    {
        stack.push();
        ring(list, axis, radius, tubeRadius, ringSegments, tubeSegments, r, g, b);
        stack.pop();
    }

    public static void sphere(List<Float> list, Vector3f center, float radius, int stacks, int slices, float r, float g, float b)
    {
        for (int i = 0; i < stacks; i++)
        {
            float phi1 = (float) Math.PI * i / stacks;
            float phi2 = (float) Math.PI * (i + 1) / stacks;

            for (int j = 0; j < slices; j++)
            {
                float theta1 = TWO_PI * j / slices;
                float theta2 = TWO_PI * (j + 1) / slices;

                Vector3f v0 = spherePoint(center, radius, phi1, theta1);
                Vector3f v1 = spherePoint(center, radius, phi1, theta2);
                Vector3f v2 = spherePoint(center, radius, phi2, theta2);
                Vector3f v3 = spherePoint(center, radius, phi2, theta1);

                emit(list, v0.x, v0.y, v0.z, r, g, b);
                emit(list, v1.x, v1.y, v1.z, r, g, b);
                emit(list, v2.x, v2.y, v2.z, r, g, b);

                emit(list, v0.x, v0.y, v0.z, r, g, b);
                emit(list, v2.x, v2.y, v2.z, r, g, b);
                emit(list, v3.x, v3.y, v3.z, r, g, b);
            }
        }
    }


    public static void sphere(List<Float> list, TransformStack stack, Vector3f center, float radius, int stacks, int slices, float r, float g, float b)
    {
        Vector3f worldCenter = new Vector3f(center).mulPosition(stack.peek());
        sphere(list, worldCenter, radius, stacks, slices, r, g, b);
    }



    public static void scaleHandle(List<Float> list, Vector3f dir, float shaftLength, float shaftRadius, float boxSize, int segments, float r, float g, float b)
    {
        Vector3f origin = new Vector3f(0.0F, 0.0F, 0.0F);
        Vector3f normalised = new Vector3f(dir).normalize();
        Vector3f shaftEnd = new Vector3f(normalised).mul(shaftLength);

        cylinder(list, origin, shaftEnd, shaftRadius, segments, r, g, b);

        Vector3f boxPos = new Vector3f(normalised).mul(shaftLength + (boxSize * 0.5F) + 0.03F); // 0.92f in original
        box(list, boxPos, boxSize, r, g, b);
    }

    public static void scaleHandle(List<Float> list, TransformStack stack, Vector3f dir, float shaftLength, float shaftRadius, float boxSize, int segments, float r, float g, float b)
    {
        Vector3f origin = new Vector3f(0.0F, 0.0F, 0.0F).mulPosition(stack.peek());
        Vector3f worldDir = new Vector3f(dir).mulDirection(stack.peek()).normalize();
        Vector3f shaftEnd = new Vector3f(origin).add(new Vector3f(worldDir).mul(shaftLength));

        cylinder(list, origin, shaftEnd, shaftRadius, segments, r, g, b);

        Vector3f boxPos = new Vector3f(origin).add(new Vector3f(worldDir).mul(shaftLength + (boxSize * 0.5F) + 0.03F));
        box(list, boxPos, boxSize, r, g, b);
    }



    public static void arrow(List<Float> list, Vector3f dir, float shaftLength, float shaftRadius, float coneRadius, int segments, float r, float g, float b)
    {
        Vector3f origin = new Vector3f(0.0F, 0.0F, 0.0F);
        Vector3f normalised = new Vector3f(dir).normalize();
        Vector3f shaftEnd = new Vector3f(normalised).mul(shaftLength);
        Vector3f tipEnd = new Vector3f(normalised);

        cylinder(list, origin, shaftEnd, shaftRadius, segments, r, g, b);
        cone(list, shaftEnd, tipEnd, coneRadius, segments, r, g, b);
    }


    public static void arrow(List<Float> list, TransformStack stack, Vector3f dir, float shaftLength, float shaftRadius, float coneRadius, int segments, float r, float g, float b)
    {
        Vector3f origin = new Vector3f(0.0F, 0.0F, 0.0F).mulPosition(stack.peek());
        Vector3f worldDir = new Vector3f(dir).mulDirection(stack.peek()).normalize();
        Vector3f shaftEnd = new Vector3f(origin).add(new Vector3f(worldDir).mul(shaftLength));
        Vector3f tipEnd = new Vector3f(origin).add(worldDir);

        cylinder(list, origin, shaftEnd, shaftRadius, segments, r, g, b);
        cone(list, shaftEnd, tipEnd, coneRadius, segments, r, g, b);
    }



    private static Vector3f buildPerp(Vector3f dir)
    {
        Vector3f reference = Math.abs(dir.x) < 0.9F ? new Vector3f(1.0F, 0.0F, 0.0F) : new Vector3f(0.0F, 1.0F, 0.0F);
        return new Vector3f(dir).cross(reference).normalize();
    }

    private static Vector3f spherePoint(Vector3f center, float radius, float phi, float theta)
    {
        float x = center.x + radius * (float) (Math.sin(phi) * Math.cos(theta));
        float y = center.y + radius * (float) Math.cos(phi);
        float z = center.z + radius * (float) (Math.sin(phi) * Math.sin(theta));
        return new Vector3f(x, y, z);
    }
}
