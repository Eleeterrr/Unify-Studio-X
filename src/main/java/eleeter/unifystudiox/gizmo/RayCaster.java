package eleeter.unifystudiox.gizmo;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class RayCaster
{


    public Ray cast(double mouseX, double mouseY, int logicalW, int logicalH, Matrix4f projection, Matrix4f view)
    {
        float ndcX = (float) (2.0 * mouseX / logicalW) - 1.0f;
        float ndcY = 1.0f - (float) (2.0 * mouseY / logicalH);

        Matrix4f invProj = projection.invert(new Matrix4f());
        Vector4f eyeDir  = invProj.transform(new Vector4f(ndcX, ndcY, -1.0f, 1.0f));
        eyeDir.z = -1.0f;
        eyeDir.w =  0.0f;

        Matrix4f invView  = view.invert(new Matrix4f());
        Vector4f worldDir = invView.transform(eyeDir);
        Vector3f direction = new Vector3f(worldDir.x, worldDir.y, worldDir.z).normalize();

        Vector3f origin = invView.getTranslation(new Vector3f());

        return new Ray(origin, direction);
    }
}
