package eleeter.unifystudiox.renderer.core;

import java.nio.FloatBuffer;
import java.util.concurrent.atomic.AtomicReference;

import org.joml.Matrix4f;

import eleeter.unifystudiox.graphics.api.IGraphicsBackend;
import eleeter.unifystudiox.graphics.api.IShaderProgram;
import eleeter.unifystudiox.graphics.api.ITexture;
import eleeter.unifystudiox.scene.Scene;

public record RenderContext(
    RenderPass pass,
    IShaderProgram shader,
    Matrix4f viewMatrix,
    Matrix4f projectionMatrix,
    Matrix4f lightSpaceMatrix,
    FloatBuffer matrixBuffer,
    Scene scene,
    BucketManager bucketManager,
    IGraphicsBackend backend,
    AtomicReference<ITexture> sceneDepthRef
)
{
    public RenderContext(
            RenderPass pass,
            IShaderProgram shader,
            Matrix4f viewMatrix,
            Matrix4f projectionMatrix,
            Matrix4f lightSpaceMatrix,
            FloatBuffer matrixBuffer,
            Scene scene,
            BucketManager bucketManager,
            IGraphicsBackend backend)
    {
        this(pass, shader, viewMatrix, projectionMatrix, lightSpaceMatrix, matrixBuffer, scene, bucketManager, backend,
                new AtomicReference<>());
    }

    public ITexture sceneDepthTexture()
    {
        return this.sceneDepthRef.get();
    }
}
