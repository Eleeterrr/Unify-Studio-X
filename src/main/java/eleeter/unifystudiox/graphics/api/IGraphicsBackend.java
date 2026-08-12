package eleeter.unifystudiox.graphics.api;

import eleeter.unifystudiox.graphics.TextureFormatBit;
import eleeter.unifystudiox.graphics.gfx.PipelineState;

public interface IGraphicsBackend
{

    void init();

    void clearColor(float r, float g, float b, float a);

    IFramebuffer createFramebuffer(int width, int height, int samples, TextureFormatBit[] colorFormats, TextureFormatBit depthFormat);

    IShaderProgram createShaderProgram(String vertexPath, String fragmentPath, String geometryPath);

    void clear(BlitMask mask);

    void clearDepth();

    void clearFrame();

    void setViewport(int width, int height);

    void setScissor(int x, int y, int width, int height);

    void setScissorEnabled(boolean enabled);

    void bindSampler(int unit, int samplerId);

    void applyState(PipelineState state);

    void resetState();

    void saveState();

    void restoreState();

    void bindTextureId(int unit, int textureId);

    void drawElements(PrimitiveType primitiveType, int count);

    void drawArrays(PrimitiveType primitiveType, int first, int count);

}
