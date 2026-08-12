package eleeter.unifystudiox.renderer.core;

import java.util.List;

import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.graphics.Vao;
import eleeter.unifystudiox.graphics.api.IGraphicsBackend;
import eleeter.unifystudiox.graphics.api.IShaderProgram;
import eleeter.unifystudiox.graphics.gfx.PipelineState;
import eleeter.unifystudiox.renderer.entities.EntityRenderer;
import eleeter.unifystudiox.scene.entity.SceneEntity;

public class RenderBatch
{
    private final IGraphicsBackend backend;
    private IShaderProgram currentShader;
    private TextureGL currentTexture;
    private Vao currentVao;

    public RenderBatch(IGraphicsBackend backend)
    {
        this.backend = backend;
    }

    public void flush(List<RenderCommand> commands, RenderContext context)
    {
        for (RenderCommand cmd : commands)
        {

            boolean shaderChanged = false;
            if (this.currentShader != cmd.shader)
            {
                if (cmd.shader != null)
                {
                    cmd.shader.bind();
                    cmd.shader.setUniformMatrix4f("uView", context.viewMatrix().get(context.matrixBuffer()));
                    cmd.shader.setUniformMatrix4f("uProjection",
                            context.projectionMatrix().get(context.matrixBuffer()));
                }
                this.currentShader = cmd.shader;
                shaderChanged = true;
            }

            if (this.currentTexture != cmd.texture || shaderChanged)
            {
                if (cmd.texture != null)
                {
                    cmd.texture.bind(0);
                    if (this.currentShader != null)
                    {
                        this.currentShader.setUniform("uHasTexture", true);
                        this.currentShader.setUniform("uTexture", 0);
                    }
                } else
                {
                    if (this.currentShader != null)
                    {
                        this.currentShader.setUniform("uHasTexture", false);
                    }
                }
                this.currentTexture = cmd.texture;
            }

            if (this.currentVao != cmd.vao)
            {
                if (cmd.vao != null)
                {
                    cmd.vao.bind();
                }
                this.currentVao = cmd.vao;
            }

            if (cmd.renderer != null && cmd.entity != null)
            {
                @SuppressWarnings("unchecked")
                EntityRenderer<SceneEntity> r = (EntityRenderer<SceneEntity>) cmd.renderer;
                r.setupUniforms(this.currentShader, cmd.entity, cmd.customId, context);
            } else if (this.currentShader != null)
            {
                this.currentShader.setUniformMatrix4f("uModel", cmd.modelMatrix.get(context.matrixBuffer()));
                this.currentShader.setUniform("uBaseColor", cmd.color.x, cmd.color.y, cmd.color.z);
            }

            this.backend.applyState(cmd.state);

            if (cmd.indexed)
            {
                this.backend.drawElements(cmd.primitiveType, cmd.count);
            } else
            {
                this.backend.drawArrays(cmd.primitiveType, 0, cmd.count);
            }
        }

        if (this.currentVao != null)
        {
            this.currentVao.unbind();
        }

        this.currentVao = null;
        this.currentShader = null;
        this.currentTexture = null;
    }


    public void configureGlobal()
    {
        this.backend.init();
    }

    public void clearFrame()
    {
        this.backend.clearFrame();
    }

    public void clearDepth()
    {
        this.backend.clearDepth();
    }

    public void setViewport(int width, int height)
    {
        this.backend.setViewport(width, height);
    }

    public void bindSampler(int unit, int samplerId)
    {
        this.backend.bindSampler(unit, samplerId);
    }

    public void flushShadow(List<RenderCommand> commands, RenderContext context)
    {
        this.backend.applyState(PipelineState.SHADOW);
        flush(commands, context);
        this.backend.resetState();
    }
}
