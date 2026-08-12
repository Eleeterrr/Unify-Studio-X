package eleeter.unifystudiox.gizmo;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import eleeter.unifystudiox.input.InputHandler;
import eleeter.unifystudiox.scene.OrbitCamera;
import eleeter.unifystudiox.scene.entity.gizmo.GizmoAxis;
import eleeter.unifystudiox.scene.entity.gizmo.ViewportGizmoEntity;


public class ViewportGizmoController
{
    private final ViewportGizmoEntity entity;
    private final OrbitCamera camera;
    private final InputHandler input;

    private float aspect = 1.0F;

    private boolean isDragging = false;
    private GizmoAxis dragAxis = GizmoAxis.NONE;
    private float totalDragX = 0.0F;
    private float totalDragY = 0.0F;

    public ViewportGizmoController(ViewportGizmoEntity entity, OrbitCamera camera, InputHandler input)
    {
        this.entity = entity;
        this.camera = camera;
        this.input = input;
    }

    public void update(int screenW, int screenH)
    {
        this.aspect = (float) screenW / screenH;
        float mouseX = (float) this.input.getMouseX();
        float mouseY = (float) this.input.getMouseY();

        float nx = (mouseX / screenW * 2.0F - 1.0F) * (this.aspect * 10.0F);
        float ny = (1.0F - mouseY / screenH * 2.0F) * 10.0F;

        float cx = this.aspect * 8.5F;
        float cy = 8.5F;

        float dx = nx - cx;
        float dy = ny - cy;

        GizmoAxis best = GizmoAxis.NONE;

        if (Math.sqrt(dx * dx + dy * dy) < 4.0F)
        {
            best = pickPip(dx, dy);
        }

        this.entity.setHoveredAxis(best);

        if (this.input.isButtonPressed(0))
        {
            if (Math.sqrt(dx * dx + dy * dy) < 4.0F)
            {
                this.isDragging = true;
                this.dragAxis = best;
                this.totalDragX = 0.0F;
                this.totalDragY = 0.0F;
            }
        }
        else if (this.input.isButtonHeld(0) && this.isDragging)
        {
            float mdx = (float) this.input.getMouseDeltaX();
            float mdy = (float) this.input.getMouseDeltaY();
            this.totalDragX += Math.abs(mdx);
            this.totalDragY += Math.abs(mdy);
            
            this.camera.orbit(mdx, mdy, 0.40F);
        }
        else if (this.input.isButtonReleased(0) && this.isDragging)
        {
            this.isDragging = false;
            if (this.totalDragX < 4.0F && this.totalDragY < 4.0F && this.dragAxis != GizmoAxis.NONE)
            {
                handleSnap(this.dragAxis);
            }
        }
    }

    private GizmoAxis pickPip(float localX, float localY)
    {
        Matrix4f rotation = new Matrix4f(this.camera.getViewMatrix());

        /* IT TOOK ME 2 FREAKING HOURS TO UNDERSTAND WHAT IS WRONG HERE
        * I JUST ADDED rotation.setTranslation(0, -0.4F, 0);  THIS >>> y-0.4F
        * */
        rotation.setTranslation(0, -0.4F, 0);


        GizmoAxis[] axes =
        { GizmoAxis.X, GizmoAxis.Y, GizmoAxis.Z,
                GizmoAxis.NEG_X, GizmoAxis.NEG_Y, GizmoAxis.NEG_Z };
        GizmoAxis best = GizmoAxis.NONE;
        float minDist = 0.6F;

        for (GizmoAxis axis : axes)
        {
            Vector3f dir = axis.direction();
            Vector4f proj = new Vector4f(dir.x, dir.y, dir.z, 1.0F).mul(rotation);


            float d = (float) Math.sqrt(Math.pow(localX - proj.x * 1.5F, 2) + Math.pow(localY - proj.y * 1.5F, 2));

            if (d < minDist)
            {
                minDist = d;
                best = axis;
            }
        }
        return best;
    }

    private void handleSnap(GizmoAxis axis)
    {
        switch (axis)
        {
            case Z     -> this.camera.snapTo(0.0F, 0.0F);    // Front
            case NEG_Z -> this.camera.snapTo(180.0F, 0.0F);  // Back
            case X     -> this.camera.snapTo(-90.0F, 0.0F);  // Right
            case NEG_X -> this.camera.snapTo(90.0F, 0.0F);   // Left
            case Y     -> this.camera.snapTo(this.input.isKeyHeld(340) ? 0.0F : 0.0F, 89.9F); // Top
            case NEG_Y -> this.camera.snapTo(0.0F, -89.9F);  // Bottom
        }
    }
}
