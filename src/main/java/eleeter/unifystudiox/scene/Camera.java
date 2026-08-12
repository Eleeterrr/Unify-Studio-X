package eleeter.unifystudiox.scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera
{

    private final Vector3f position = new Vector3f(0f, 2f, 8f);
    private final Vector3f front = new Vector3f(0f, 0f, -1f);
    private final Vector3f up = new Vector3f(0f, 1f, 0f);
    private final Vector3f right = new Vector3f(1f, 0f, 0f);

    private int lastWidth = -1, lastHeight = -1;

    private float yaw = -90f;
    private float pitch = 0f;

    private float fovDegrees = 100f;
    private float nearPlane = 0.1f;
    private float farPlane = 1000f;

    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projectionMatrix = new Matrix4f();
    private boolean viewDirty = true;
    private boolean projDirty = true;


    public Matrix4f getViewMatrix()
    {
        if (this.viewDirty)
        {
            Vector3f target = new Vector3f(this.position).add(this.front);
            this.viewMatrix.setLookAt(this.position, target, this.up);
            this.viewDirty = false;
        }
        return this.viewMatrix;
    }


    public Matrix4f getProjectionMatrix(int viewportWidth, int viewportHeight)
    {
        if (this.projDirty || viewportWidth != this.lastWidth || viewportHeight != this.lastHeight)
        {
            float aspect = (float) viewportWidth / viewportHeight;
            this.projectionMatrix.setPerspective((float) Math.toRadians(this.fovDegrees), aspect, this.nearPlane, this.farPlane);
            this.lastWidth = viewportWidth;
            this.lastHeight = viewportHeight;
            this.projDirty = false;
        }
        return this.projectionMatrix;
    }


    public void moveForward(float amount)
    {
        this.position.add(new Vector3f(this.front).mul(amount));
        this.viewDirty = true;
    }

    public void moveBackward(float amount)
    {
        this.position.sub(new Vector3f(this.front).mul(amount));
        this.viewDirty = true;
    }

    public void strafeLeft(float amount)
    {
        this.position.sub(new Vector3f(this.right).mul(amount));
        this.viewDirty = true;
    }

    public void strafeRight(float amount)
    {
        this.position.add(new Vector3f(this.right).mul(amount));
        this.viewDirty = true;
    }

    public void moveUp(float amount)
    {
        this.position.add(new Vector3f(this.up).mul(amount));
        this.viewDirty = true;
    }

    public void moveDown(float amount)
    {
        this.position.sub(new Vector3f(this.up).mul(amount));
        this.viewDirty = true;
    }

    public void rotate(float deltaYawDeg, float deltaPitchDeg, float sensitivity)
    {
        this.yaw += deltaYawDeg * sensitivity;
        this.pitch -= deltaPitchDeg * sensitivity;
        this.pitch = Math.max(-89f, Math.min(89f, this.pitch));
        recalculateFront();
    }

    public void setFov(float degrees)
    {
        this.fovDegrees = degrees;
        this.projDirty = true;
    }

    public void setNearPlane(float n)
    {
        this.nearPlane = n;
        this.projDirty = true;
    }

    public void setFarPlane(float f)
    {
        this.farPlane = f;
        this.projDirty = true;
    }


    public Vector3f getPosition()
    {
        return this.position;
    }

    public Vector3f getFront()
    {
        return this.front;
    }

    public float getFov()
    {
        return this.fovDegrees;
    }


    private void recalculateFront()
    {
        float yawRad = (float) Math.toRadians(this.yaw);
        float pitchRad = (float) Math.toRadians(this.pitch);
        this.front.set((float) (Math.cos(pitchRad) * Math.cos(yawRad)), (float) (Math.sin(pitchRad)), (float) (Math.cos(pitchRad) * Math.sin(yawRad))).normalize();
        this.right.set(this.front).cross(this.up).normalize();
        this.viewDirty = true;
    }
}
