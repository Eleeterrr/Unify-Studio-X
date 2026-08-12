package eleeter.unifystudiox.scene;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

import java.util.Collection;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import eleeter.unifystudiox.gizmo.Ray;
import eleeter.unifystudiox.gizmo.RayCaster;
import eleeter.unifystudiox.input.InputHandler;
import eleeter.unifystudiox.scene.entity.HierarchicalEntity;
import eleeter.unifystudiox.scene.entity.Pickable;
import eleeter.unifystudiox.scene.entity.Positionable;
import eleeter.unifystudiox.scene.entity.RiggedEntity;
import eleeter.unifystudiox.scene.entity.SceneEntity;

public class SelectionManager
{
    private final RayCaster rayCaster = new RayCaster();

    private float closestDist;

    private Vector3f closestPointOnRay;
    private Vector3f pos;
    private Vector3f w;
    private float t;

    private float distSq;
    private Ray ray;
    private SceneEntity closest;
    private int selectedSubIndex = -1;

    private static final float SELECTION_RADIUS_SQ = 0.1f * 0.1f;


    private SceneEntity selectedParent = null;
    private SceneEntity hoveredEntity;

    public SceneEntity updateSelection(InputHandler input, int logicalW, int logicalH, Matrix4f projection, Matrix4f view, Collection<SceneEntity> entities, SceneEntity currentSelection, boolean isGizmoHovered)
    {
        if (!input.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT) || isGizmoHovered)
        {
            return currentSelection;
        }

        this.ray = this.rayCaster.cast(input.getMouseX(), input.getMouseY(), logicalW, logicalH, projection, view);

        this.closest = null;
        this.selectedParent = null;
        this.closestDist = Float.MAX_VALUE;
        this.selectedSubIndex = -1;

        for (SceneEntity entity : entities)
        {
            if (!entity.isVisible()) continue;

            if (entity instanceof Pickable pickable)
            {
                SelectionResult result = pickable.pick(this.ray);
                if (result.hasHit() && result.distance() < this.closestDist)
                {
                    this.closestDist = result.distance();
                    this.closest = result.entity();
                    this.selectedParent = this.closest;
                    this.selectedSubIndex = result.subIndex();
                    
                    if (this.selectedSubIndex != -1 && this.closest instanceof HierarchicalEntity hierarchical)
                    {
                        SceneEntity subEntity = hierarchical.getSubEntity(this.selectedSubIndex);
                        if (subEntity != null)
                        {
                            this.closest = subEntity;
                        }
                    }
                }
                continue;
            }

            if (entity instanceof Positionable p)
            {
                this.pos = p.getPosition();
                
                this.w = new Vector3f(this.pos).sub(this.ray.origin());
                this.t = this.w.dot(this.ray.direction());
                
                if (this.t < 0) continue;

                this.closestPointOnRay = new Vector3f(this.ray.direction()).mul(this.t).add(this.ray.origin());
                this.distSq = this.closestPointOnRay.distanceSquared(this.pos);
                
                if (distSq <= SELECTION_RADIUS_SQ && this.t < closestDist)
                {
                    this.closestDist = this.t;
                    this.closest = entity;
                    this.selectedParent = this.closest;
                    this.selectedSubIndex = -1;
                }
            }
        }

        for (SceneEntity entity : entities)
        {
            if (entity instanceof RiggedEntity rigged)
            {
                if (entity == this.selectedParent)
                {
                    rigged.setSelectedBoneIndex(this.selectedSubIndex);
                }
                else
                {
                    rigged.setSelectedBoneIndex(-1);
                }
            }
        }

        return closest;
    }

    public int getSelectedSubIndex()
    {
        return selectedSubIndex;
    }

    public void updateHover(InputHandler input, int logicalW, int logicalH, Matrix4f projection, Matrix4f view, Collection<SceneEntity> entities, boolean isGizmoHovered)
    {
        if (isGizmoHovered)
        {
            clearHover(entities);
            return;
        }

        this.ray = this.rayCaster.cast(input.getMouseX(), input.getMouseY(), logicalW, logicalH, projection, view);

        float bestHoverDist = Float.MAX_VALUE;
        this.hoveredEntity = null;
        int hoveredSubIndex = -1;

        for (SceneEntity entity : entities)
        {
            if (!entity.isVisible()) continue;

            if (entity instanceof Pickable pickable)
            {
                SelectionResult result = pickable.pick(this.ray);
                if (result.hasHit() && result.distance() < bestHoverDist)
                {
                    bestHoverDist = result.distance();
                    this.hoveredEntity = result.entity();
                    hoveredSubIndex = result.subIndex();
                }
            }
        }

        for (SceneEntity entity : entities)
        {
            if (entity instanceof RiggedEntity rigged)
            {
                if (entity == this.hoveredEntity)
                {
                    rigged.setHoveredBoneIndex(hoveredSubIndex);
                }
                else
                {
                    rigged.setHoveredBoneIndex(-1);
                }
            }
        }
    }

    public SceneEntity getHoveredEntity()
    {
        return this.hoveredEntity;
    }

    private void clearHover(Collection<SceneEntity> entities)
    {
        this.hoveredEntity = null;
        for (SceneEntity entity : entities)
        {
            if (entity instanceof RiggedEntity rigged)
            {
                rigged.setHoveredBoneIndex(-1);
            }
        }
    }
}
