package eleeter.unifystudiox.gizmo;

import eleeter.unifystudiox.scene.entity.gizmo.GizmoAxis;
import org.joml.Vector3f;

public class DragProjector
{

    public float project(Ray startRay, Ray currentRay, Vector3f pivot, GizmoAxis axis, Vector3f camForward)
    {
        Vector3f axisDir = axis.direction();
        Vector3f planeNormal = buildPlaneNormal(axisDir, camForward);

        float tStart = rayPlaneIntersect(startRay, pivot, planeNormal);
        float tCurrent = rayPlaneIntersect(currentRay, pivot, planeNormal);

        if (Float.isNaN(tStart) || Float.isNaN(tCurrent))
        {
            return 0.0f;
        }

        Vector3f startPt = new Vector3f(startRay.direction()).mul(tStart).add(startRay.origin());
        Vector3f currentPt = new Vector3f(currentRay.direction()).mul(tCurrent).add(currentRay.origin());

        return new Vector3f(currentPt).sub(startPt).dot(axisDir);
    }


    public Vector3f getPlanePoint(Ray ray, Vector3f planeOrigin, Vector3f planeNormal)
    {
        float denom = ray.direction().dot(planeNormal);
        if (Math.abs(denom) < 1e-6f)
        {
            return null;
        }

        float t = new Vector3f(planeOrigin).sub(ray.origin()).dot(planeNormal) / denom;
        if (t < 0)
        {
            return null;
        }

        return new Vector3f(ray.direction()).mul(t).add(ray.origin());
    }

    private float rayPlaneIntersect(Ray ray, Vector3f planeOrigin, Vector3f planeNormal)
    {
        float denom = ray.direction().dot(planeNormal);
        if (Math.abs(denom) < 1e-6f)
        {
            return Float.NaN;
        }
        float t = new Vector3f(planeOrigin).sub(ray.origin()).dot(planeNormal) / denom;
        return t > 0 ? t : Float.NaN;
    }

    private Vector3f buildPlaneNormal(Vector3f axisDir, Vector3f camForward)
    {
        float dot = camForward.dot(axisDir);
        Vector3f n = new Vector3f(camForward).sub(new Vector3f(axisDir).mul(dot));

        if (n.lengthSquared() < 1e-6f)
        {
            n = (Math.abs(axisDir.x) < 0.9f) ? new Vector3f(0, 1, 0).cross(axisDir, new Vector3f()) : new Vector3f(1, 0, 0).cross(axisDir, new Vector3f());
        }

        return n.normalize();
    }
}
