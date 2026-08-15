package eleeter.unifystudiox.ui.framework.render;

import java.util.List;

import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.graphics.math.TransformStack;
import eleeter.unifystudiox.graphics.Vao;
import eleeter.unifystudiox.graphics.text.render.MeshData;

public interface UIRenderer
{
    void init();

    void beginFrame(float logicalW, float logicalH, float physicalW, float physicalH);

    void drawRect(float x, float y, float w, float h, float r, float g, float b, float a);

    void drawRoundedRect(float x, float y, float w, float h, float r, float g, float b, float a, float radius);

    void drawTexture(float x, float y, float w, float h, TextureGL texture, float r, float g, float b, float a);

    void pushClip(float x, float y, float w, float h);

    void popClip();

    void drawText(MeshData data, TextureGL atlas, float x, float y, float scale, float r, float g, float b, float a);

    void drawFramebufferTexture(float x, float y, float w, float h, int glTextureHandle, float r, float g, float b, float a);

    void drawGeometry(Vao vao, int indexCount, TextureGL texture);

    /** Renders raw ShapeDraw vertex/index lists as a solid colored mesh */
    void drawShapeGeometry(List<Float> vertices, List<Integer> indices, float r, float g, float b, float a);

    /** Exposes the active MatrixStack for ShapeDraw transform support */
    TransformStack getMatrixStack();

    void endFrame();

    void cleanup();
}
