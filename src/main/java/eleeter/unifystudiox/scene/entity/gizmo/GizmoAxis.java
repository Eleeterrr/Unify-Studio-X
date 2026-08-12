package eleeter.unifystudiox.scene.entity.gizmo;

import org.joml.Vector3f;

public enum GizmoAxis
{
    NONE(0, 0, 0),
    X(1, 0, 0),
    Y(0, 1, 0),
    Z(0, 0, 1),
    NEG_X(-1, 0, 0),
    NEG_Y(0, -1, 0),
    NEG_Z(0, 0, -1),
    CENTER(1.0F, 1.0F, 1.0F);

    private final float dx, dy, dz;

    GizmoAxis(float dx, float dy, float dz)
    {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public Vector3f direction()
    {
        return new Vector3f(this.dx, this.dy, this.dz);
    }
}
