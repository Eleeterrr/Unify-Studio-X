package eleeter.unifystudiox.scene.entity.gizmo;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import eleeter.unifystudiox.scene.entity.BaseSceneEntity;
import eleeter.unifystudiox.scene.entity.Positionable;
import eleeter.unifystudiox.scene.entity.SceneEntity;

public class GizmoEntity extends BaseSceneEntity
{

    private static final String ID = "sys_gizmo";


    private GizmoMode mode = GizmoMode.TRANSLATE;
    private GizmoAxis hoveredAxis = GizmoAxis.NONE;
    private GizmoAxis activeAxis = GizmoAxis.NONE;

    private final Vector3f position = new Vector3f();
    private final Quaternionf rotation = new Quaternionf();
    private SceneEntity targetEntity;

    private final Matrix4f modelMatrix = new Matrix4f();


    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public void update(double deltaTime)
    {
        if (this.targetEntity != null)
        {
            if (this.targetEntity instanceof Positionable p)
            {
                this.position.set(p.getPosition());
                this.rotation.set(p.getRotation());
            } else
            {
                this.targetEntity.getModelMatrix().getTranslation(this.position);
                this.targetEntity.getModelMatrix().getNormalizedRotation(this.rotation);
            }
        }
    }


    @Override
    public Matrix4f getModelMatrix()
    {
        return this.modelMatrix.translationRotateScale(this.position, this.rotation, new Vector3f(1.0f, 1.0f, 1.0f));
    }

    public Quaternionf getRotation()
    {
        return this.rotation;
    }

    @Override
    public boolean isVisible()
    {
        return this.targetEntity != null;
    }

    @Override
    public void cleanup()
    {
    }


    public GizmoMode getMode()
    {
        return this.mode;
    }

    public void setMode(GizmoMode m)
    {
        this.mode = m;
    }

    public GizmoAxis getHoveredAxis()
    {
        return this.hoveredAxis;
    }

    public void setHoveredAxis(GizmoAxis a)
    {
        this.hoveredAxis = a;
    }

    public GizmoAxis getActiveAxis()
    {
        return this.activeAxis;
    }

    public void setActiveAxis(GizmoAxis a)
    {
        this.activeAxis = a;
    }

    public SceneEntity getTargetEntity()
    {
        return this.targetEntity;
    }

    public void setTargetEntity(SceneEntity e)
    {
        this.targetEntity = e;
    }

    public boolean isHovered()
    {
        return this.hoveredAxis != GizmoAxis.NONE;
    }
}
