package eleeter.unifystudiox.scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.joml.Vector3f;

import eleeter.unifystudiox.anchor.AnchorManager;
import eleeter.unifystudiox.animation.api.AnimationSystem;
import eleeter.unifystudiox.ecs.EntityWorld;
import eleeter.unifystudiox.ecs.systems.TransformSystem;
import eleeter.unifystudiox.editor.animation.ViewportSelectionListener;
import eleeter.unifystudiox.gizmo.GizmoController;
import eleeter.unifystudiox.gizmo.ViewportGizmoController;
import eleeter.unifystudiox.graphics.Window;
import eleeter.unifystudiox.input.InputHandler;
import eleeter.unifystudiox.resource.AssetRegistry;
import eleeter.unifystudiox.resource.IAssetBinder;
import eleeter.unifystudiox.scene.entity.HierarchicalEntity;
import eleeter.unifystudiox.scene.entity.Positionable;
import eleeter.unifystudiox.scene.entity.SceneEntity;
import eleeter.unifystudiox.scene.entity.gizmo.GizmoEntity;
import eleeter.unifystudiox.scene.entity.gizmo.ViewportGizmoEntity;
import eleeter.unifystudiox.ui.UIKey;
import eleeter.unifystudiox.ui.framework.render.context.UISystem;
import eleeter.unifystudiox.ui.framework.render.gl.GLUIRenderer;
import eleeter.unifystudiox.ui.overlay.UICameraZoom;

public class Scene
{

    private final OrbitCamera camera = new OrbitCamera();
    private final Environment environment = new Environment();
    private final UISystem ui = new UISystem(new GLUIRenderer());
    private final AnchorManager anchorManager = new AnchorManager();
    private final EntityWorld ecsWorld = new EntityWorld();

    private SceneEntity newSelection;

    private float dx;
    private float dy;
    private float scroll;
    private boolean shift;
    private boolean mmb;

    private final Map<String, SceneEntity> entityMap = new LinkedHashMap<>();

    private final GizmoEntity gizmo = new GizmoEntity();
    private final GizmoController gizmoController = new GizmoController();
    private final SelectionManager selectionManager = new SelectionManager();

    private final ViewportGizmoEntity viewportGizmo = new ViewportGizmoEntity();
    private ViewportGizmoController viewportGizmoController;

    private SceneEntity selectedEntity;
    private boolean paused = false;
    private AnimationSystem animationSystem;
    private ViewportSelectionListener selectionListener;
    private IAssetBinder assetBinder;
    private CameraZoomSpeedController zoomController;
    private UICameraZoom zoomIndicator;

    private static final float MOUSE_SENSITIVITY = 0.1f;

    public Scene()
    {
        this.ecsWorld.addSystem(new TransformSystem());
    }


    public Scene addEntity(SceneEntity entity)
    {
        if (this.assetBinder != null)
        {
            this.assetBinder.bind(entity);
        }

        this.entityMap.put(entity.getId(), entity);
        entity.initEcs(this.ecsWorld);

        this.entityMap.remove(this.gizmo.getId());
        this.entityMap.put(this.gizmo.getId(), this.gizmo);

        this.entityMap.remove(this.viewportGizmo.getId());
        this.entityMap.put(this.viewportGizmo.getId(), this.viewportGizmo);

        return this;
    }

    private Runnable frameHook;

    public void setFrameHook(Runnable hook)
    {
        this.frameHook = hook;
    }

    public SceneEntity getGizmoTarget()
    {
        return this.gizmo.getTargetEntity();
    }

    public Optional<SceneEntity> findEntity(String id)
    {
        return Optional.ofNullable(this.entityMap.get(id));
    }

    public boolean removeEntity(String id)
    {
        SceneEntity removed = this.entityMap.remove(id);
        if (removed != null)
        {
            removed.cleanup();
        }
        return removed != null;
    }


    public Collection<SceneEntity> getEntities()
    {
        return Collections.unmodifiableCollection(this.entityMap.values());
    }


    public void update(double deltaTime, InputHandler input, Window window)
    {
        AssetRegistry.update();

        if (this.animationSystem != null)
        {
            this.animationSystem.update((float) deltaTime);
        }

        this.ui.processInput(input, window.getLogicalWidth(), window.getLogicalHeight());
        this.ui.update(deltaTime, window.getLogicalWidth(), window.getLogicalHeight());

        this.paused = this.ui.isGamePaused();

        this.ecsWorld.update((float) deltaTime);

        this.updateCamera(deltaTime, input);
        this.updateGizmo(input, window);
        this.updateViewportGizmo(input, window);

        if (this.frameHook != null)
        {
            this.frameHook.run();
        }

        for (SceneEntity entity : this.entityMap.values())
        {
            if (entity instanceof GizmoEntity)
            {
                continue;
            }

            double frameDelta = this.paused ? 0.0 : deltaTime;
            entity.update(frameDelta);
        }

        this.anchorManager.updateAll();
        if (!this.paused)
        {
            this.anchorManager.syncOffsetsFromPayloads();
            this.anchorManager.updateAll();
            this.gizmo.update(deltaTime);
        }



    }

    public boolean isPaused()
    {
        return this.paused;
    }

    public void setViewportSelectionListener(ViewportSelectionListener listener)
    {
        this.selectionListener = listener;
    }

    public Collection<SceneEntity> getPositionableEntities()
    {
        List<SceneEntity> result = new ArrayList<>();
        for (SceneEntity entity : this.entityMap.values())
        {
            if (entity instanceof Positionable)
            {
                result.add(entity);
            }
        }
        return result;
    }

    public void selectEntity(SceneEntity entity)
    {
        this.selectedEntity = entity;
        this.gizmo.setTargetEntity(entity);
    }

    public SceneEntity getSelectedEntity()
    {
        return this.selectedEntity;
    }

    public SceneEntity getHoveredEntity()
    {
        return this.selectionManager.getHoveredEntity();
    }

    public OrbitCamera getCamera()
    {
        return this.camera;
    }

    public GizmoEntity getGizmoEntity()
    {
        return this.gizmo;
    }

    public GizmoController getGizmoController()
    {
        return this.gizmoController;
    }

    public SelectionManager getSelectionManager()
    {
        return this.selectionManager;
    }

    public Environment getEnvironment()
    {
        return this.environment;
    }

    public UISystem getUi()
    {
        return this.ui;
    }


    public void cleanup()
    {
        this.entityMap.values().forEach(SceneEntity::cleanup);
        this.entityMap.clear();
        this.anchorManager.clear();
        this.ui.cleanup();
        this.ecsWorld.cleanup();
    }

    public AnchorManager getAnchorManager()
    {
        return this.anchorManager;
    }

    public void setAnimationSystem(AnimationSystem animationSystem)
    {
        this.animationSystem = animationSystem;
    }

    public AnimationSystem getAnimationSystem()
    {
        return this.animationSystem;
    }

    public void setAssetBinder(IAssetBinder assetBinder)
    {
        this.assetBinder = assetBinder;
    }


    public void setZoomController(CameraZoomSpeedController controller, UICameraZoom indicator)
    {
        this.zoomController = controller;
        this.zoomIndicator = indicator;
    }

    private void updateGizmo(InputHandler input, Window window)
    {
        boolean alreadyDragging = this.gizmoController.isDragging();
        if (this.ui.getContext().isUIBlockingInput() && !alreadyDragging)
        {
            return;
        }

        int logicalW = window.getLogicalWidth();
        int logicalH = window.getLogicalHeight();

        this.newSelection = this.selectionManager.updateSelection(input, logicalW, logicalH,
                this.camera.getProjectionMatrix(window.getWidth(), window.getHeight()), this.camera.getViewMatrix(),
                this.entityMap.values(), this.selectedEntity, this.gizmo.isHovered());

        this.selectionManager.updateHover(input, logicalW, logicalH,
                this.camera.getProjectionMatrix(window.getWidth(), window.getHeight()), this.camera.getViewMatrix(),
                this.entityMap.values(), this.gizmo.isHovered());

        SceneEntity prevTarget = this.gizmo.getTargetEntity();

        if (this.newSelection != this.selectedEntity)
        {
            selectEntity(this.newSelection);
        }

        SceneEntity target = this.selectedEntity;
        if (target instanceof HierarchicalEntity hierarchical)
        {
            int subIndex = this.selectionManager.getSelectedSubIndex();
            if (subIndex >= 0)
            {
                SceneEntity subEntity = hierarchical.getSubEntity(subIndex);
                if (subEntity != null)
                {
                    target = subEntity;
                }
            }
        }

        if (target != prevTarget)
        {
            /* If the user clicked a new bone or object, fire the selection event */
            if (this.selectionListener != null && target != null)
            {
                String fullId = target.getId();
                this.selectionListener.onViewportBoneSelected(fullId);
            }
        }

        this.gizmo.setTargetEntity(target);

        this.gizmoController.update(input, this.gizmo, logicalW, logicalH,
                this.camera.getProjectionMatrix(window.getWidth(), window.getHeight()), this.camera.getViewMatrix());

        if (this.gizmoController.isDragging() && this.selectionListener != null && target instanceof Positionable pos)
        {
            String fullId = target.getId();

            Vector3f p = pos.getLocalPosition();
            Vector3f s = pos.getLocalScale();
            Vector3f r = new Vector3f();
            pos.getLocalRotation().getEulerAnglesXYZ(r);

            this.selectionListener.onGizmoTransformChanged(fullId, p.x, p.y, p.z, r.x, r.y, r.z, s.x, s.y, s.z);
        }
    }

    private void updateViewportGizmo(InputHandler input, Window window)
    {
        if (this.viewportGizmoController == null)
        {
            this.viewportGizmoController = new ViewportGizmoController(this.viewportGizmo, this.camera, input);
        }
        this.viewportGizmoController.update(window.getLogicalWidth(), window.getLogicalHeight());
    }


    private void updateCamera(double deltaTime, InputHandler input)
    {
        this.dx = (float) input.getMouseDeltaX();
        this.dy = (float) input.getMouseDeltaY();
        this.scroll = (float) input.getScrollDelta();
        this.shift = input.isKeyHeld(340);
        this.mmb = input.isButtonHeld(2);


        if (this.zoomController != null)
        {
            this.zoomController.update(input, this.zoomIndicator);
        }

        if (!this.ui.getContext().isUIBlockingInput())
        {
            if (this.mmb && !this.shift)
            {
                this.camera.orbit(this.dx, this.dy, MOUSE_SENSITIVITY);
            }

            if (this.mmb && this.shift)
            {
                this.camera.pan(this.dx, this.dy, MOUSE_SENSITIVITY * 0.1f);
            }


            boolean isCtrlHeld = input.isKeyHeld(UIKey.LEFT_CONTROL)
                    || input.isKeyHeld(UIKey.RIGHT_CONTROL);

            if (this.scroll != 0 && !isCtrlHeld)
            {
                float multiplier = this.zoomController != null
                        ? this.zoomController.getMultiplier() : 1.0F;
                this.camera.zoom(this.scroll, multiplier);
            }
        }

        this.camera.update(deltaTime);
    }
}
