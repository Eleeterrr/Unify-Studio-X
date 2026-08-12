package eleeter.unifystudiox.gizmo;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_T;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import eleeter.unifystudiox.input.InputHandler;
import eleeter.unifystudiox.scene.entity.Positionable;
import eleeter.unifystudiox.scene.entity.SceneEntity;
import eleeter.unifystudiox.scene.entity.gizmo.GizmoAxis;
import eleeter.unifystudiox.scene.entity.gizmo.GizmoEntity;
import eleeter.unifystudiox.scene.entity.gizmo.GizmoMode;

public class GizmoController
{

    private final RayCaster rayCaster = new RayCaster();
    private final AxisPicker axisPicker = new AxisPicker();
    private final TranslationStrategy translationStrategy = new TranslationStrategy();
    private final RotationStrategy rotationStrategy = new RotationStrategy();
    private final ScalingStrategy scalingStrategy = new ScalingStrategy();


    private boolean dragging = false;
    private Ray dragStartRay = null;
    private Vector3f initialPosition = null;
    private Quaternionf initialRotation = null;
    private Vector3f initialScale = null;

    public boolean isDragging()
    {
        return this.dragging;
    }


    public void update(InputHandler input, GizmoEntity gizmo, int logicalW, int logicalH, Matrix4f projection, Matrix4f view)
    {
        handleModeSwitch(input, gizmo);

        SceneEntity target = gizmo.getTargetEntity();
        if (target == null)
        {
            resetGizmoState(gizmo);
            return;
        }

        Matrix4f invView = view.invert(new Matrix4f());
        Vector3f cameraPos  = invView.getTranslation(new Vector3f());
        Vector3f camForward = new Vector3f(-invView.m02(), -invView.m12(), -invView.m22()).normalize();

        Ray currentRay = this.rayCaster.cast(input.getMouseX(), input.getMouseY(), logicalW, logicalH, projection, view);

        Vector3f gizmoOrigin = gizmo.getModelMatrix().getTranslation(new Vector3f());
        float distToCam = cameraPos.distance(gizmoOrigin);

        if (!this.dragging)
        {
            updateHover(currentRay, gizmo, gizmoOrigin, distToCam);
            beginDrag(input, gizmo, currentRay, gizmoOrigin);
        }
        else
        {
            if (input.wasWrappedThisFrame())
            {
                this.dragStartRay = currentRay;
                if (target instanceof Positionable positionable)
                {
                    this.initialPosition = new Vector3f(positionable.getPosition());
                    this.initialRotation = new Quaternionf(positionable.getRotation());
                    this.initialScale = new Vector3f(positionable.getScale());
                }
            }
            else if (target instanceof Positionable positionable)
            {
                updateDrag(currentRay, gizmo, positionable, camForward);
            }
            tryEndDrag(input, gizmo);
        }
    }


    private void handleModeSwitch(InputHandler input, GizmoEntity gizmo)
    {
        if (input.isKeyPressed(GLFW_KEY_T))
        {
            gizmo.setMode(GizmoMode.TRANSLATE);
        }

        if (input.isKeyPressed(GLFW_KEY_R))
        {
            gizmo.setMode(GizmoMode.ROTATE);
        }

        if (input.isKeyPressed(GLFW_KEY_S))
        {
            gizmo.setMode(GizmoMode.SCALE);
        }
    }

    private void updateHover(Ray ray, GizmoEntity gizmo, Vector3f origin, float distToCam)
    {
        GizmoAxis hovered = this.axisPicker.pick(ray, origin, gizmo.getRotation(), distToCam, gizmo.getMode());
        gizmo.setHoveredAxis(hovered);
    }

    private void beginDrag(InputHandler input, GizmoEntity gizmo, Ray currentRay, Vector3f origin)
    {
        if (input.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT) && gizmo.getHoveredAxis() != GizmoAxis.NONE)
        {
            this.dragging = true;
            this.dragStartRay = currentRay;
            
            SceneEntity target = gizmo.getTargetEntity();
            if (target instanceof Positionable positionable)
            {
                this.initialPosition = new Vector3f(positionable.getPosition());
                this.initialRotation = new Quaternionf(positionable.getRotation());
                this.initialScale = new Vector3f(positionable.getScale());
            }
            else
            {
                this.initialPosition = new Vector3f(origin);
                this.initialRotation = new Quaternionf();
                this.initialScale = new Vector3f(1.0f, 1.0f, 1.0f);
            }
            
            gizmo.setActiveAxis(gizmo.getHoveredAxis());
        }
    }


    private void updateDrag(Ray currentRay, GizmoEntity gizmo, Positionable target, Vector3f camForward)
    {
        GizmoAxis axis = gizmo.getActiveAxis();
        GizmoInteractionStrategy strategy = switch (gizmo.getMode())
        {
            case TRANSLATE -> translationStrategy;
            case ROTATE -> rotationStrategy;
            case SCALE -> scalingStrategy;
        };

        strategy.apply(this.dragStartRay, currentRay, this.initialPosition, this.initialRotation, this.initialScale, axis, camForward, target);
    }

    private void tryEndDrag(InputHandler input, GizmoEntity gizmo)
    {
        if (input.isButtonReleased(GLFW_MOUSE_BUTTON_LEFT))
        {
            this.dragging = false;
            this.dragStartRay = null;
            this.initialPosition = null;
            this.initialRotation = null;
            this.initialScale = null;
            gizmo.setActiveAxis(GizmoAxis.NONE);
        }
    }

    private void resetGizmoState(GizmoEntity gizmo)
    {
        gizmo.setHoveredAxis(GizmoAxis.NONE);
        gizmo.setActiveAxis(GizmoAxis.NONE);
        this.dragging = false;
        this.dragStartRay = null;
        this.initialPosition = null;
        this.initialRotation = null;
        this.initialScale = null;
    }
}
