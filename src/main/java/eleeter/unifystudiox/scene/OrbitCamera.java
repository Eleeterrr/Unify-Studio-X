package eleeter.unifystudiox.scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import eleeter.unifystudiox.scene.io.SerializeProperty;

public class OrbitCamera
{

    private final Vector3f target = new Vector3f(0, 0, 0);
    private float yaw = -45.0f;

    private float pitch = 25.0f;

    private float distance = 10.0f;

    @SerializeProperty
    private final Vector3f targetGoal = new Vector3f(0, 0, 0);

    @SerializeProperty
    private float yawGoal = -45.0f;

    @SerializeProperty
    private float pitchGoal = 25.0f;

    @SerializeProperty
    private float distanceGoal = 10.0f;

    @SerializeProperty
    private float smoothing = 15.0f;

    @SerializeProperty
    private float fov = 90;

    @SerializeProperty
    private float near = 0.1f;

    @SerializeProperty
    private float far = 1000.0f;

    private final Matrix4f viewMatrix = new Matrix4f();

    private final Matrix4f projectionMatrix = new Matrix4f();

    public void update(double deltaTime)
    {
        float alpha = (float) (1.0 - Math.exp(-this.smoothing * deltaTime));

        this.yaw = lerp(this.yaw, this.yawGoal, alpha);
        this.pitch = lerp(this.pitch, this.pitchGoal, alpha);
        this.distance = lerp(this.distance, this.distanceGoal, alpha);
        this.target.lerp(this.targetGoal, alpha);

        recalculateMatrices();
    }

    private void recalculateMatrices()
    {
        this.viewMatrix.identity().translate(0, 0, -this.distance).rotateX((float) Math.toRadians(this.pitch))
                .rotateY((float) Math.toRadians(this.yaw)).translate(-this.target.x, -this.target.y, -this.target.z);
    }

    public Matrix4f getViewMatrix()
    {
        return this.viewMatrix;
    }

    public Matrix4f getProjectionMatrix(int width, int height)
    {
        float aspect = (float) width / Math.max(1, height);
        return new Matrix4f().setPerspective(
                (float) Math.toRadians(this.fov), aspect, this.near, this.far);
    }

    public void orbit(float dx, float dy, float sensitivity)
    {
        this.yawGoal += dx * sensitivity;
        this.pitchGoal += dy * sensitivity;

        this.pitchGoal = Math.max(-89.0f, Math.min(89.0f, this.pitchGoal));
    }

    public void zoom(float delta, float sensitivity)
    {
        this.distanceGoal -= delta * sensitivity * (this.distanceGoal * 0.1f);
        this.distanceGoal = Math.max(0.1f, this.distanceGoal);
    }

    public void snapTo(float yaw, float pitch)
    {
        this.yawGoal = yaw;
        this.pitchGoal = pitch;
    }

    public void pan(float dx, float dy, float sensitivity)
    {
        Matrix4f invView = this.viewMatrix.invert(new Matrix4f());
        Vector3f right = new Vector3f();
        Vector3f up = new Vector3f();
        invView.transformDirection(new Vector3f(1, 0, 0), right);
        invView.transformDirection(new Vector3f(0, 1, 0), up);

        float panSpeed = this.distanceGoal * sensitivity;
        this.targetGoal.add(right.mul(-dx * panSpeed));
        this.targetGoal.add(up.mul(dy * panSpeed));
    }

    public Vector3f getTarget()
    {
        return this.targetGoal;
    }

    public Vector3f getPosition()
    {
        Matrix4f invView = viewMatrix.invert(new Matrix4f());
        return invView.getTranslation(new Vector3f());
    }

    private float lerp(float a, float b, float f)
    {
        return a + f * (b - a);
    }

    public void setFov(float fov)
    {
        this.fov = fov;
    }

    public void setNear(float near)
    {
        this.near = near;
    }

    public void setFar(float far)
    {
        this.far = far;
    }

    public float getFov()
    {
        return this.fov;
    }

    public float getNear()
    {
        return this.near;
    }

    public float getFar()
    {
        return this.far;
    }
}
