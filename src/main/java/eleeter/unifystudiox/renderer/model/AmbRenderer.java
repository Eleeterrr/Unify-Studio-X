package eleeter.unifystudiox.renderer.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import eleeter.unifystudiox.amb.AmbGLMesh;
import eleeter.unifystudiox.amb.AmbMesh;
import eleeter.unifystudiox.amb.AmbModel;
import eleeter.unifystudiox.amb.AmbModelInstance;
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

public class AmbRenderer implements EntityRenderer<AmbModelInstance>
{

    private IShaderProgram shader;
    private IShaderProgram outlinePassShader;
    private IShaderProgram shadowDepthShader;
    private boolean initialized = false;
    private boolean hasTexture;
    private AmbModelInstance instance;
    private RenderContext context;

    private final Map<AmbModel, List<AmbGLMesh>> meshCache = new HashMap<>();

    @Override
    public Class<AmbModelInstance> getSupportedType()
    {
        return AmbModelInstance.class;
    }

    private void initGpuResources(RenderContext context)
    {
        this.shader = context.backend().createShaderProgram("/shaders/amb_entity.vert", "/shaders/amb_entity.frag", null);
        this.outlinePassShader = context.backend().createShaderProgram("/shaders/weight_outline.vert", "/shaders/weight_outline.frag", null);
        this.shadowDepthShader = context.backend().createShaderProgram("/shaders/cubic_depth.vert", "/shaders/cubic_depth.frag", null);
        this.initialized = true;
    }

    @Override
    public void submitCommands(AmbModelInstance instance, RenderContext context)
    {
        this.instance = instance;
        this.context = context;

        if (!this.initialized)
        {
            initGpuResources(context);
        }

        List<AmbGLMesh> glMeshes = this.meshCache.computeIfAbsent(this.instance.sourceModel, model ->
        {
            List<AmbGLMesh> list = new ArrayList<>();
            for (AmbMesh mesh : model.meshes)
            {
                list.add(new AmbGLMesh(mesh));
            }
            return list;
        });

        if (context.pass() == RenderPass.SHADOW_DEPTH)
        {
            for (AmbGLMesh glMesh : glMeshes)
            {
                RenderCommand cmd = context.bucketManager().allocateCommand();
                cmd.sortKey = ((long) this.shadowDepthShader.hashCode() << 32) | glMesh.getVao().getHandle();
                cmd.shader = this.shadowDepthShader;
                cmd.texture = null;
                cmd.vao = glMesh.getVao();
                cmd.count = glMesh.getIndexCount();
                cmd.indexed = true;
                cmd.state = PipelineState.SHADOW;
                cmd.renderer = this;
                cmd.entity = instance;
                cmd.customId = 0;
                context.bucketManager().submit(RenderBucket.SOLID_3D, cmd);
            }
            return;
        }

        this.hasTexture = instance.getTexture() != null;

        for (AmbGLMesh glMesh : glMeshes)
        {
            RenderCommand cmd1 = context.bucketManager().allocateCommand();
            cmd1.sortKey = ((long) this.shader.hashCode() << 32) | (this.hasTexture ? instance.getTexture().hashCode() : 0);
            cmd1.shader = this.shader;
            cmd1.texture = instance.getTexture();
            cmd1.vao = glMesh.getVao();
            cmd1.count = glMesh.getIndexCount();
            cmd1.indexed = true;
            cmd1.state = PipelineState.OPAQUE;
            cmd1.renderer = this;
            cmd1.entity = instance;
            cmd1.customId = 1;
            context.bucketManager().submit(RenderBucket.SOLID_3D, cmd1);
        }

        if (instance.getHoveredBoneIndex() >= 0 || instance.getSelectedBoneIndex() >= 0)
        {
            for (AmbGLMesh glMesh : glMeshes)
            {
                RenderCommand cmd2 = context.bucketManager().allocateCommand();
                cmd2.sortKey = ((long) this.outlinePassShader.hashCode() << 32);
                cmd2.shader = this.outlinePassShader;
                cmd2.texture = null;
                cmd2.vao = glMesh.getVao();
                cmd2.count = glMesh.getIndexCount();
                cmd2.indexed = true;
                cmd2.state = PipelineState.WIREFRAME_OVERLAY;
                cmd2.renderer = this;
                cmd2.entity = instance;
                cmd2.customId = 2;
                context.bucketManager().submit(RenderBucket.SOLID_3D, cmd2);
            }
        }
    }

    @Override
    public void setupUniforms(IShaderProgram shader, AmbModelInstance instance, int customId, RenderContext context)
    {
        FloatBuffer fb = context.matrixBuffer();

        if (customId == 0)
        {
            shader.setUniform("uProjection", context.projectionMatrix(), fb);
            shader.setUniform("uView", context.viewMatrix(), fb);
            shader.setUniform("uModel", instance.getWorldMatrix(), fb);
            shader.setUniformMatrix4fv("uBones", instance.boneMatrices);
        } else if (customId == 1)
        {
            shader.setUniformMatrix4f("uProjection", context.projectionMatrix().get(fb));
            shader.setUniformMatrix4f("uView", context.viewMatrix().get(fb));
            shader.setUniformMatrix4f("uModel", instance.getWorldMatrix().get(fb));

            shader.setUniformMatrix4f("uLightSpaceMatrix", context.lightSpaceMatrix().get(fb));
            shader.setUniform("uShadowMap", 1);
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
                shader.setUniformMatrix4f("uSpotLightSpaceMatrix", shadowSpot.lightSpaceMatrix.get(fb));
                shader.setUniform("uHasSpotShadow", true);
            } else
            {
                shader.setUniform("uHasSpotShadow", false);
            }

            Environment env = context.scene().getEnvironment();
            shader.setUniform("uSunDirection", env.getSunDirection().x, env.getSunDirection().y, env.getSunDirection().z);
            shader.setUniform("uSunColor", env.getSunColor().x * env.getSunIntensity(),
                    env.getSunColor().y * env.getSunIntensity(), env.getSunColor().z * env.getSunIntensity());
            shader.setUniform("uAmbientColor", env.getAmbientColor().x * env.getAmbientIntensity(),
                    env.getAmbientColor().y * env.getAmbientIntensity(),
                    env.getAmbientColor().z * env.getAmbientIntensity());

            Vector3f camPos = context.scene().getCamera().getPosition();
            shader.setUniform("uCameraPos", camPos.x, camPos.y, camPos.z);

            SpotlightUploader.upload(shader, context.scene());
            PointLightUploader.upload(shader, context.scene());
            ParticleLightUploader.upload(shader, context.scene(), context.matrixBuffer());
            shader.setUniformMatrix4fv("uBones", instance.boneMatrices);
            shader.setUniform("uHoveredBone", instance.getHoveredBoneIndex());
        } else if (customId == 2)
        {
            shader.setUniform("uProjection", context.projectionMatrix(), fb);
            shader.setUniform("uView", context.viewMatrix(), fb);
            shader.setUniform("uModel", instance.getWorldMatrix(), fb);
            shader.setUniformMatrix4fv("uBones", instance.boneMatrices);
            shader.setUniform("uHoveredBone", instance.getHoveredBoneIndex());
            shader.setUniform("uSelectedBone", instance.getSelectedBoneIndex());
            shader.setUniform("uOutlineColor", 1.0F, 1.0F, 0.0F);
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
            if (this.shadowDepthShader != null)
            {
                this.shadowDepthShader.cleanup();
            }
        }
        for (List<AmbGLMesh> meshList : this.meshCache.values())
        {
            for (AmbGLMesh glMesh : meshList)
            {
                glMesh.cleanup();
            }
        }
        this.meshCache.clear();
    }
}
