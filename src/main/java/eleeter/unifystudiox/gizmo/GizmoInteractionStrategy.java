package eleeter.unifystudiox.gizmo;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import eleeter.unifystudiox.scene.entity.Positionable;
import eleeter.unifystudiox.scene.entity.gizmo.GizmoAxis;

public interface GizmoInteractionStrategy
{
    void apply(Ray startRay, Ray currentRay, Vector3f initialPosition, Quaternionf initialRotation, Vector3f initialScale, GizmoAxis axis, Vector3f camForward, Positionable target);
}
