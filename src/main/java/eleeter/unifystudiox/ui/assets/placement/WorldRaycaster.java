package eleeter.unifystudiox.ui.assets.placement;

import org.joml.Matrix4f;

public interface WorldRaycaster
{
    Matrix4f getProjectionMatrix();

    Matrix4f getViewMatrix();

    int getLogicalWidth();

    int getLogicalHeight();
}
