package eleeter.unifystudiox.gizmo;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import eleeter.unifystudiox.scene.entity.Positionable;
import eleeter.unifystudiox.scene.entity.gizmo.GizmoAxis;

public class RotationStrategy implements GizmoInteractionStrategy
{
    private final DragProjector projector = new DragProjector();

    @Override
    public void apply(Ray startRay, Ray currentRay, Vector3f initialPosition, Quaternionf initialRotation, Vector3f initialScale, GizmoAxis axis, Vector3f camForward, Positionable target)
    {
        if (axis == GizmoAxis.CENTER)
        {
            /* Trackball-style Free Rotation */
            Vector3f right = new Vector3f(camForward).cross(0.0F, 1.0F, 0.0F).normalize();
            if (right.lengthSquared() < 0.001F)
            {
                right.set(1.0F, 0.0F, 0.0F);
            }
            Vector3f up = new Vector3f(right).cross(camForward).normalize();

            Vector3f d1 = new Vector3f(startRay.direction()).normalize();
            Vector3f d2 = new Vector3f(currentRay.direction()).normalize();

            float dx = d2.dot(right) - d1.dot(right);
            float dy = d2.dot(up) - d1.dot(up);

            float sensitivity = 6.0F;
            Quaternionf qx = new Quaternionf().fromAxisAngleRad(up, -dx * sensitivity);
            Quaternionf qy = new Quaternionf().fromAxisAngleRad(right, dy * sensitivity);

            target.setRotation(qx.mul(qy).mul(initialRotation, new Quaternionf()));
            return;
        }

        Vector3f axisDir = axis.direction().rotate(initialRotation);
        
        Vector3f startPt = this.projector.getPlanePoint(startRay, initialPosition, axisDir);
        Vector3f currentPt = this.projector.getPlanePoint(currentRay, initialPosition, axisDir);

        if (startPt == null || currentPt == null)
        {
            return;
        }

        Vector3f v1 = new Vector3f(startPt).sub(initialPosition).normalize();
        Vector3f v2 = new Vector3f(currentPt).sub(initialPosition).normalize();

        float dot = Math.max(-1.0f, Math.min(1.0f, v1.dot(v2)));
        float angle = (float) Math.acos(dot);

        Vector3f cross = new Vector3f(v1).cross(v2);
        if (cross.dot(axisDir) < 0)
        {
            angle = -angle;
        }

        if (Float.isNaN(angle) || Math.abs(angle) < 1e-6)
        {
            return;
        }

        Quaternionf rotationDelta = new Quaternionf().fromAxisAngleRad(axisDir, angle);
        target.setRotation(rotationDelta.mul(initialRotation, new Quaternionf()));
    }
}
