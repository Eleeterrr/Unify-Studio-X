package eleeter.unifystudiox.ui;

import eleeter.unifystudiox.graphics.TextureGL;

public interface UIRenderer
{
    void init();

    void beginFrame(float logicalW, float logicalH, float physicalW, float physicalH);

    void drawRect(float x, float y, float w, float h, float r, float g, float b, float a);

    void drawTexture(float x, float y, float w, float h, TextureGL texture, float r, float g, float b, float a);

    void drawFramebufferTexture(float x, float y, float w, float h, int glTextureHandle, float r, float g, float b, float a);

    void pushClip(float x, float y, float w, float h);

    void popClip();

    void endFrame();

    void cleanup();
}
