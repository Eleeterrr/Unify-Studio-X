package eleeter.unifystudiox.gizmo;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import eleeter.unifystudiox.scene.entity.gizmo.GizmoAxis;
import eleeter.unifystudiox.scene.entity.gizmo.GizmoMode;

public class AxisPicker
{
    private float armLength;
    private float bestDist;
    private GizmoAxis best;

    public GizmoAxis pick(Ray ray, Vector3f gizmoOrigin, Quaternionf rotation, float distToCamera, GizmoMode mode)
    {
        this.armLength = distToCamera * 0.2f;
        
        float shaftRadius = armLength * 0.08f;
        float headRadius = armLength * 0.10f;
        float torusRadius = armLength * 0.08f;
        float ringRadius = armLength * 1.0f;

        this.best = GizmoAxis.NONE;
        this.bestDist = Float.MAX_VALUE;

        if (mode == GizmoMode.ROTATE)
        {
            /* Check center sphere for free rotation */
            checkCenter(ray, gizmoOrigin, armLength * 0.15F);

            checkRotationRing(ray, gizmoOrigin, rotation, GizmoAxis.X, ringRadius, torusRadius);
            checkRotationRing(ray, gizmoOrigin, rotation, GizmoAxis.Y, ringRadius, torusRadius);
            checkRotationRing(ray, gizmoOrigin, rotation, GizmoAxis.Z, ringRadius, torusRadius);
        }
        else
        {
            checkAxis(ray, gizmoOrigin, rotation, GizmoAxis.X, shaftRadius, headRadius);
            checkAxis(ray, gizmoOrigin, rotation, GizmoAxis.Y, shaftRadius, headRadius);
            checkAxis(ray, gizmoOrigin, rotation, GizmoAxis.Z, shaftRadius, headRadius);
        }

        return this.best;
    }

    private void checkCenter(Ray ray, Vector3f origin, float radius)
    {
        Vector3f oc = new Vector3f(ray.origin()).sub(origin);
        float b = oc.dot(ray.direction());
        float c = oc.dot(oc) - radius * radius;
        float h = b * b - c;

        if (h >= 0.0F)
        {
            float t = -b - (float) Math.sqrt(h);
            if (t > 0.0F && t < this.bestDist)
            {
                this.bestDist = t;
                this.best = GizmoAxis.CENTER;
            }
        }
    }

    private void checkAxis(Ray ray, Vector3f origin, Quaternionf rotation, GizmoAxis axis, float shaftRadius, float headRadius)
    {
        Vector3f dir = axis.direction().rotate(rotation);
        Vector3f shaftEnd = new Vector3f(dir).mul(armLength * 0.85f).add(origin);
        Vector3f headStart = new Vector3f(shaftEnd);
        Vector3f tip = new Vector3f(dir).mul(armLength).add(origin);

        float dShaft = distToSegment(ray, origin, shaftEnd);
        if (dShaft < shaftRadius)
        {
            updateBest(ray, origin, shaftEnd, axis);
        }

        float dHead = distToSegment(ray, headStart, tip);
        if (dHead < headRadius)
        {
            updateBest(ray, headStart, tip, axis);
        }
    }

    private void checkRotationRing(Ray ray, Vector3f origin, Quaternionf rotation, GizmoAxis axis, float radius, float tubeRadius)
    {
        int segments = 48;
        Vector3f normal = axis.direction().rotate(rotation);
        Vector3f perp1 = new Vector3f();
        if (Math.abs(normal.x) < 0.9f)
        {
            perp1.set(1, 0, 0);
        }
        else
        {
            perp1.set(0, 1, 0);
        }
        Vector3f u = new Vector3f(normal).cross(perp1).normalize().mul(radius);
        Vector3f v = new Vector3f(normal).cross(u).normalize().mul(radius);

        for (int i = 0; i < segments; i++)
        {
            float a1 = (float) (i * 2.0 * Math.PI / segments);
            float a2 = (float) ((i + 1) * 2.0 * Math.PI / segments);
            
            Vector3f p1 = new Vector3f(u).mul((float)Math.cos(a1)).add(new Vector3f(v).mul((float)Math.sin(a1))).add(origin);
            Vector3f p2 = new Vector3f(u).mul((float)Math.cos(a2)).add(new Vector3f(v).mul((float)Math.sin(a2))).add(origin);

            float d = distToSegment(ray, p1, p2);
            if (d < tubeRadius)
            {
                float t = closestTOnRay(ray, p1, p2);
                if (t > 0 && t < bestDist)
                {
                    bestDist = t;
                    best = axis;
                }
            }
        }
    }

    private void updateBest(Ray ray, Vector3f p1, Vector3f p2, GizmoAxis axis)
    {
        float t = closestTOnRay(ray, p1, p2);
        if (t > 0 && t < bestDist)
        {
            bestDist = t;
            best = axis;
        }
    }

    private float distToSegment(Ray ray, Vector3f p1, Vector3f p2)
    {
        Vector3f da = new Vector3f(ray.direction());
        Vector3f db = new Vector3f(p2).sub(p1);
        Vector3f r = new Vector3f(ray.origin()).sub(p1);

        float a = da.lengthSquared();
        float e = db.lengthSquared();
        float f = db.dot(r);
        float s = da.dot(db);

        float denom = a * e - s * s;
        float sc, tc;

        if (denom != 0.0f)
        {
            sc = (s * f - e * da.dot(r)) / denom;
        }
        else
        {
            sc = 0.0f;
        }

        tc = (s * sc + f) / e;
        tc = Math.max(0, Math.min(1, tc));

        Vector3f onRay = new Vector3f(ray.direction()).mul(sc).add(ray.origin());
        Vector3f onSegment = new Vector3f(db).mul(tc).add(p1);
        return onRay.distance(onSegment);
    }

    private float closestTOnRay(Ray ray, Vector3f p1, Vector3f p2)
    {
        Vector3f da = new Vector3f(ray.direction());
        Vector3f db = new Vector3f(p2).sub(p1);
        Vector3f r = new Vector3f(ray.origin()).sub(p1);
        float a = da.lengthSquared();
        float e = db.lengthSquared();
        float s = da.dot(db);
        float denom = a * e - s * s;
        if (denom != 0.0f)
        {
            return (s * db.dot(r) - e * da.dot(r)) / denom;
        }
        return -1;
    }
}
