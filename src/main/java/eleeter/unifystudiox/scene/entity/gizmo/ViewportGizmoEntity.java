package eleeter.unifystudiox.scene.entity.gizmo;

import org.joml.Matrix4f;

import eleeter.unifystudiox.scene.entity.BaseSceneEntity;

public class ViewportGizmoEntity extends BaseSceneEntity
{
    private static final String ID = "sys_viewport_gizmo";

    private GizmoAxis hoveredAxis = GizmoAxis.NONE;
    private final Matrix4f modelMatrix = new Matrix4f();

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public void update(double deltaTime)
    {
    }

    @Override
    public Matrix4f getModelMatrix()
    {
        return this.modelMatrix;
    }

    @Override
    public void cleanup()
    {
    }

    public GizmoAxis getHoveredAxis()
    {
        return this.hoveredAxis;
    }

    public void setHoveredAxis(GizmoAxis axis)
    {
        this.hoveredAxis = axis;
    }
}
