package eleeter.unifystudiox.gizmo;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import eleeter.unifystudiox.scene.entity.Positionable;
import eleeter.unifystudiox.scene.entity.gizmo.GizmoAxis;

public class TranslationStrategy implements GizmoInteractionStrategy
{
    @Override
    public void apply(Ray startRay, Ray currentRay, Vector3f initialPosition, Quaternionf initialRotation,
            Vector3f initialScale, GizmoAxis axis, Vector3f camForward, Positionable target)
            {
        Vector3f axisDir = axis.direction();

        float tStart = closestPointOnAxis(startRay, initialPosition, axisDir);
        float tCurrent = closestPointOnAxis(currentRay, initialPosition, axisDir);

        float delta = tCurrent - tStart;

        Vector3f newPosition = new Vector3f(initialPosition).add(new Vector3f(axisDir).mul(delta));
        target.setPosition(newPosition);
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
