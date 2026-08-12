package eleeter.unifystudiox.gizmo;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import eleeter.unifystudiox.scene.entity.Positionable;
import eleeter.unifystudiox.scene.entity.gizmo.GizmoAxis;

public class ScalingStrategy implements GizmoInteractionStrategy
{
    private static final float SCALE_SENSITIVITY = 1.0f;

    @Override
    public void apply(Ray startRay, Ray currentRay, Vector3f initialPosition, Quaternionf initialRotation, Vector3f initialScale, GizmoAxis axis, Vector3f camForward, Positionable target)
    {
        Vector3f localAxisDir = axis.direction();
        Vector3f axisDir = new Vector3f(localAxisDir).rotate(initialRotation);
        
        float tStart = closestPointOnAxis(startRay, initialPosition, axisDir);
        float tCurrent = closestPointOnAxis(currentRay, initialPosition, axisDir);
        
        float delta = tCurrent - tStart;
        float multiplier = 1.0f + delta * SCALE_SENSITIVITY;

        float nx = (localAxisDir.x > 0.5f) ? initialScale.x * multiplier : initialScale.x;
        float ny = (localAxisDir.y > 0.5f) ? initialScale.y * multiplier : initialScale.y;
        float nz = (localAxisDir.z > 0.5f) ? initialScale.z * multiplier : initialScale.z;

        target.setScale(new Vector3f(Math.max(nx, 1e-4f), Math.max(ny, 1e-4f), Math.max(nz, 1e-4f)));
    }

    private float closestPointOnAxis(Ray ray, Vector3f pivot, Vector3f axisDir)
    {
        Vector3f w0 = new Vector3f(pivot).sub(ray.origin());
        float a = axisDir.dot(axisDir); 
        float b = axisDir.dot(ray.direction());
        float c = ray.direction().dot(ray.direction()); 
        float d = axisDir.dot(w0);
        float e = ray.direction().dot(w0);
        
        float denom = a * c - b * b;
        if (Math.abs(denom) < 1e-6f)
        {
            return 0.0f;
        }
        
        return (b * e - c * d) / denom;
    }
}
