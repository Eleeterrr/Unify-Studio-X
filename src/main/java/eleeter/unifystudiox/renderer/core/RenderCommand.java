package eleeter.unifystudiox.renderer.core;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.graphics.Vao;
import eleeter.unifystudiox.graphics.api.IShaderProgram;
import eleeter.unifystudiox.graphics.api.PrimitiveType;
import eleeter.unifystudiox.graphics.gfx.PipelineState;
import eleeter.unifystudiox.renderer.entities.EntityRenderer;
import eleeter.unifystudiox.scene.entity.SceneEntity;

public class RenderCommand implements Comparable<RenderCommand>
{
    public long sortKey;
    
    public Vao vao;
    public int count;
    public boolean indexed;
    public PrimitiveType primitiveType = PrimitiveType.TRIANGLES;
    
    public IShaderProgram shader;
    public TextureGL texture;
    public PipelineState state = PipelineState.OPAQUE;
    
    public final Matrix4f modelMatrix = new Matrix4f();
    public final Vector4f color = new Vector4f();
    
    public EntityRenderer<?> renderer;
    public SceneEntity entity;
    public int customId;
    
    public void reset()
    {
        this.sortKey = 0;
        this.vao = null;
        this.count = 0;
        this.indexed = false;
        this.primitiveType = PrimitiveType.TRIANGLES;
        this.shader = null;
        this.texture = null;
        this.state = PipelineState.OPAQUE;
        this.modelMatrix.identity();
        this.color.set(0, 0, 0, 0);
        this.renderer = null;
        this.entity = null;
        this.customId = 0;
    }

    @Override
    public int compareTo(RenderCommand o)
    {
        return Long.compare(this.sortKey, o.sortKey);
    }
}
