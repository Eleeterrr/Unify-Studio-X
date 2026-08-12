package eleeter.unifystudiox.gizmo;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import eleeter.unifystudiox.scene.entity.Positionable;
import eleeter.unifystudiox.scene.entity.gizmo.GizmoAxis;
import eleeter.unifystudiox.scene.entity.gizmo.GizmoMode;

public class TransformApplicator
{


    public void translate(Positionable target, GizmoAxis axis, float delta)
    {
        Vector3f move = axis.direction().mul(delta);
        target.setPosition(new Vector3f(target.getPosition()).add(move));
    }


    public void rotate(Positionable target, GizmoAxis axis, float angleRad)
    {
        Quaternionf delta = new Quaternionf().fromAxisAngleRad(axis.direction(), angleRad);
        Quaternionf current = target.getRotation();
        target.setRotation(delta.mul(current, new Quaternionf()));
    }


    public void scale(Positionable target, GizmoAxis axis, float factor)
    {
        Vector3f s = target.getScale();
        Vector3f dir = axis.direction();

        float nx = (dir.x > 0.5f) ? s.x * factor : s.x;
        float ny = (dir.y > 0.5f) ? s.y * factor : s.y;
        float nz = (dir.z > 0.5f) ? s.z * factor : s.z;

        target.setScale(new Vector3f(Math.max(nx, 1e-4f), Math.max(ny, 1e-4f), Math.max(nz, 1e-4f)));
    }


    public void apply(Positionable target, GizmoMode mode, GizmoAxis axis, float delta)
    {
        switch (mode)
        {
            case TRANSLATE -> translate(target, axis, delta);
            case ROTATE -> rotate(target, axis, delta * 0.8f);
            case SCALE -> scale(target,  axis, 1.0f + delta * 0.5f);
        }
    }
}
