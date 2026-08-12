package eleeter.unifystudiox.renderer.model;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3f;

import eleeter.unifystudiox.cubic.CubicModelInstance;
import eleeter.unifystudiox.cubic.render.CubeGLMesh;
import eleeter.unifystudiox.cubic.render.CubeRuntimeModel;
import eleeter.unifystudiox.graphics.Vao;
import eleeter.unifystudiox.graphics.api.IShaderProgram;
import eleeter.unifystudiox.graphics.gfx.PipelineState;
import eleeter.unifystudiox.renderer.core.RenderBucket;
import eleeter.unifystudiox.renderer.core.RenderCommand;
import eleeter.unifystudiox.renderer.core.RenderContext;
import eleeter.unifystudiox.renderer.core.RenderPass;
import eleeter.unifystudiox.renderer.core.RenderSettings;
import eleeter.unifystudiox.renderer.entities.EntityRenderer;
import eleeter.unifystudiox.renderer.lighting.ParticleLightUploader;
import eleeter.unifystudiox.renderer.lighting.PointLightUploader;
import eleeter.unifystudiox.renderer.lighting.SpotlightUploader;
import eleeter.unifystudiox.scene.Environment;
import eleeter.unifystudiox.scene.SpotlightData;
import eleeter.unifystudiox.scene.entity.SceneEntity;
import eleeter.unifystudiox.scene.entity.SpotlightEntity;

public class CubeRenderer implements EntityRenderer<CubicModelInstance>
{
    private IShaderProgram shader;
    private IShaderProgram outlinePassShader;
    private IShaderProgram shadowDepthShader;

    private boolean hasTexture;
    private boolean initialized = false;

    private CubeRuntimeModel model;
    private CubeGLMesh glMesh;

    private FloatBuffer fb;
    private Environment env;

    private final Map<CubeRuntimeModel, CubeGLMesh> meshCache = new HashMap<>();

    @Override
    public Class<CubicModelInstance> getSupportedType()
    {
        return CubicModelInstance.class;
    }

    private void init(RenderContext context)
    {
        this.shader = context.backend().createShaderProgram("/shaders/cubic.vert", "/shaders/cubic.frag", null);
        this.outlinePassShader = context.backend().createShaderProgram("/shaders/weight_outline.vert", "/shaders/weight_outline.frag", null);
        this.shadowDepthShader = context.backend().createShaderProgram("/shaders/cubic_depth.vert", "/shaders/cubic_depth.frag", null);
        this.initialized = true;
    }

    @Override
    public void submitCommands(CubicModelInstance instance, RenderContext context)
    {
        if (!this.initialized)
        {
            init(context);
        }

        this.model = instance.getModel();
        this.glMesh = this.meshCache.computeIfAbsent(this.model, runtimeModel ->
        {
            if (runtimeModel.vertexData != null && runtimeModel.indexData != null)
            {
                return new CubeGLMesh(runtimeModel.vertexData, runtimeModel.indexData);
            }
            return null;
        });

        if (this.glMesh == null) return;

        if (context.pass() == RenderPass.SHADOW_DEPTH)
        {
            RenderCommand cmd = context.bucketManager().allocateCommand();
            cmd.sortKey = ((long) this.shadowDepthShader.hashCode() << 32) | this.glMesh.getVaoId();
            cmd.shader = this.shadowDepthShader;
            cmd.texture = null;
            cmd.vao = new Vao(this.glMesh.getVaoId());
            cmd.count = this.glMesh.getIndexCount();
            cmd.indexed = true;
            cmd.state = PipelineState.SHADOW;
            cmd.renderer = this;
            cmd.entity = instance;
            cmd.customId = 0;
            context.bucketManager().submit(RenderBucket.SOLID_3D, cmd);
            return;
        }

        RenderCommand cmd1 = context.bucketManager().allocateCommand();
        this.hasTexture = instance.getTexture() != null;
        cmd1.sortKey = ((long) this.shader.hashCode() << 32) | (this.hasTexture ? instance.getTexture().hashCode() : 0);
        cmd1.shader = this.shader;
        cmd1.texture = instance.getTexture();
        cmd1.vao = new Vao(this.glMesh.getVaoId());
        cmd1.count = this.glMesh.getIndexCount();
        cmd1.indexed = true;
        cmd1.state = PipelineState.OPAQUE_NO_CULL;
        cmd1.renderer = this;
        cmd1.entity = instance;
        cmd1.customId = 1;
        context.bucketManager().submit(RenderBucket.SOLID_3D, cmd1);

        if (instance.getHoveredBoneIndex() >= 0 || instance.getSelectedBoneIndex() >= 0)
        {
            RenderCommand cmd2 = context.bucketManager().allocateCommand();
            cmd2.sortKey = ((long) this.outlinePassShader.hashCode() << 32);
            cmd2.shader = this.outlinePassShader;
            cmd2.texture = null;
            cmd2.vao = new Vao(this.glMesh.getVaoId());
            cmd2.count = this.glMesh.getIndexCount();
            cmd2.indexed = true;
            cmd2.state = PipelineState.WIREFRAME_OVERLAY;
            cmd2.renderer = this;
            cmd2.entity = instance;
            cmd2.customId = 2;
            context.bucketManager().submit(RenderBucket.SOLID_3D, cmd2);
        }
    }

    @Override
    public void setupUniforms(IShaderProgram shader, CubicModelInstance instance, int customId, RenderContext context)
    {
        this.fb = context.matrixBuffer();
        
        if (customId == 0)
        {
            shader.setUniform("uModel", instance.getWorldMatrix(), this.fb);
            shader.setUniformMatrix4fv("uBones", instance.boneMatrices);
        } 
        else if (customId == 1)
        {
            this.env = context.scene().getEnvironment();

            shader.setUniform("uModel", instance.getWorldMatrix(), this.fb);
            shader.setUniformMatrix4f("uLightSpaceMatrix", context.lightSpaceMatrix().get(this.fb));
            shader.setUniform("uShadowMap", 1);

            shader.setUniform("uSunDirection", this.env.getSunDirection().x, this.env.getSunDirection().y, this.env.getSunDirection().z);
            shader.setUniform("uSunColor", this.env.getSunColor().x * this.env.getSunIntensity(),
                    this.env.getSunColor().y * this.env.getSunIntensity(),
                    this.env.getSunColor().z * this.env.getSunIntensity());
            shader.setUniform("uAmbientColor", this.env.getAmbientColor().x * this.env.getAmbientIntensity(),
                    this.env.getAmbientColor().y * this.env.getAmbientIntensity(),
                    this.env.getAmbientColor().z * this.env.getAmbientIntensity());

            Vector3f camPos = context.scene().getCamera().getPosition();
            shader.setUniform("uCameraPos", camPos.x, camPos.y, camPos.z);
            shader.setUniform("uNightMode", RenderSettings.NIGHT_MODE);

            SpotlightData shadowSpot = null;
            for (SceneEntity e : context.scene().getEntities())
            {
                if (e instanceof SpotlightEntity se)
                {
                    if (se.getData().enabled && se.getData().castShadow)
                    {
                        shadowSpot = se.getData();
                        break;
                    }
                }
            }

            shader.setUniform("uSpotShadowMap", 2);
            if (shadowSpot != null)
            {
                shader.setUniformMatrix4f("uSpotLightSpaceMatrix", shadowSpot.lightSpaceMatrix.get(this.fb));
                shader.setUniform("uHasSpotShadow", true);
            } else
            {
                shader.setUniform("uHasSpotShadow", false);
            }

            shader.setUniformMatrix4fv("uBones", instance.boneMatrices);
            shader.setUniform("uHoveredBone", instance.getHoveredBoneIndex());

            SpotlightUploader.upload(shader, context.scene());
            PointLightUploader.upload(shader, context.scene());
            ParticleLightUploader.upload(shader, context.scene(), context.matrixBuffer());
        }
        else if (customId == 2)
        {
            shader.setUniform("uModel", instance.getWorldMatrix(), this.fb);
            shader.setUniformMatrix4fv("uBones", instance.boneMatrices);
            shader.setUniform("uHoveredBone", instance.getHoveredBoneIndex());
            shader.setUniform("uSelectedBone", instance.getSelectedBoneIndex());
            shader.setUniform("uOutlineColor", 1.0f, 1.0f, 0.0f);
        }
    }


    @Override
    public void cleanup()
    {
        if (this.initialized)
        {
            this.shader.cleanup();
            if (this.outlinePassShader != null)
            {
                this.outlinePassShader.cleanup();
            }
        }

        for (CubeGLMesh glMesh : this.meshCache.values())
        {
            glMesh.cleanup();
        }
        this.meshCache.clear();
    }
}
