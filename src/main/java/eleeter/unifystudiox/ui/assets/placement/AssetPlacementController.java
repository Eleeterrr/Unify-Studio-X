package eleeter.unifystudiox.ui.assets.placement;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

import org.joml.Vector3f;

import eleeter.unifystudiox.assets.IModelAsset;
import eleeter.unifystudiox.assets.ModelPreviewSpec;
import eleeter.unifystudiox.gizmo.DragProjector;
import eleeter.unifystudiox.gizmo.Ray;
import eleeter.unifystudiox.gizmo.RayCaster;
import eleeter.unifystudiox.scene.Scene;
import eleeter.unifystudiox.scene.entity.Positionable;
import eleeter.unifystudiox.scene.entity.SceneEntity;

public class AssetPlacementController
{
    private static final float PLACEMENT_PLANE_Y = 0.0f;
    private static final float GRID_SNAP_SIZE = 1.0f;

    private final Scene scene;
    private final WorldRaycaster raycaster;
    private final RayCaster gizmoRayCaster = new RayCaster();
    private final DragProjector dragProjector = new DragProjector();

    private PlacementMode mode = PlacementMode.FREE;
    private boolean active = false;
    private SceneEntity ghostEntity = null;
    private String ghostEntityId = null;
    private ModelPreviewSpec activeSpec = null;
    private Vector3f ghostPosition = new Vector3f();

    public AssetPlacementController(Scene scene, WorldRaycaster raycaster)
    {
        this.scene = scene;
        this.raycaster = raycaster;
    }

    public void beginPlacement(IModelAsset asset)
    {
        cancelPlacement();
        ModelPreviewSpec spec = asset.getPreviewSpec();
        if (spec == null)
        {
            return;
        }
        SceneEntity entity = spec.createPreviewEntity();
        if (entity == null)
        {
            return;
        }
        this.activeSpec = spec;
        this.ghostEntity = entity;
        this.ghostEntityId = entity.getId();
        this.ghostPosition.set(0.0f, 0.0f, 0.0f);
        applyWorldScale(entity, spec);
        this.scene.addEntity(entity);
        this.active = true;
    }

    public void update(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        if (!this.active)
        {
            return;
        }
        boolean cancelRequested = context.isKeyPressed(GLFW_KEY_ESCAPE) || context.isRightMousePressed();

        if (cancelRequested)
        {
            cancelPlacement();
            return;
        }
        updateGhostPosition(context);
        if (context.isMousePressed() && !context.isAnyUIHovered())
        {
            confirmPlacement();
        }
    }

    public void cancelPlacement()
    {
        if (!this.active)
        {
            return;
        }
        this.scene.removeEntity(this.ghostEntityId);
        this.ghostEntity = null;
        this.ghostEntityId = null;
        this.activeSpec = null;
        this.active = false;
    }

    public boolean isActive()
    {
        return this.active;
    }

    public void setMode(PlacementMode mode)
    {
        this.mode = mode;
    }

    public PlacementMode getMode()
    {
        return this.mode;
    }

    private void confirmPlacement()
    {
        this.ghostEntity = null;
        this.ghostEntityId = null;
        this.activeSpec = null;
        this.active = false;
    }

    private void updateGhostPosition(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        Ray ray = this.gizmoRayCaster.cast(context.getMouseX(), context.getMouseY(), this.raycaster.getLogicalWidth(), this.raycaster.getLogicalHeight(), this.raycaster.getProjectionMatrix(), this.raycaster.getViewMatrix());

        Vector3f planeOrigin = new Vector3f(0.0f, PLACEMENT_PLANE_Y, 0.0f);
        Vector3f planeNormal = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f hitPoint = this.dragProjector.getPlanePoint(ray, planeOrigin, planeNormal);
        if (hitPoint == null)
        {
            return;
        }
        Vector3f snapped = applySnapMode(hitPoint);
        this.ghostPosition.set(snapped);
        if (this.ghostEntity instanceof Positionable positionable)
        {
            positionable.setPosition(new Vector3f(snapped));
        }
    }

    private Vector3f applySnapMode(Vector3f rawHit)
    {
        if (this.mode == PlacementMode.GRID_SNAP)
        {
            float x = Math.round(rawHit.x / GRID_SNAP_SIZE) * GRID_SNAP_SIZE;
            float z = Math.round(rawHit.z / GRID_SNAP_SIZE) * GRID_SNAP_SIZE;
            return new Vector3f(x, PLACEMENT_PLANE_Y, z);
        }
        return new Vector3f(rawHit.x, PLACEMENT_PLANE_Y, rawHit.z);
    }

    private void applyWorldScale(SceneEntity entity, ModelPreviewSpec spec)
    {
        if (!(entity instanceof Positionable positionable))
        {
            return;
        }
        ModelPreviewSpec.Bounds bounds = spec.getBounds();
        if (bounds == null || !bounds.isValid())
        {
            return;
        }
        float baseRenderScale = spec.getBaseRenderScale();
        float halfExtent = Math.max(bounds.getSizeX(), Math.max(bounds.getSizeY(), bounds.getSizeZ())) * 0.5f;
        if (halfExtent <= 0.0001f)
        {
            return;
        }
        float uniformScale = baseRenderScale;
        if (uniformScale < 0.05f)
        {
            uniformScale = 0.05f;
        }
        positionable.setScale(new Vector3f(uniformScale, uniformScale, uniformScale));
    }
}
