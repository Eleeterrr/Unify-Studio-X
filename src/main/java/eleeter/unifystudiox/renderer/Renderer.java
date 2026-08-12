package eleeter.unifystudiox.renderer;

import eleeter.unifystudiox.graphics.Framebuffer;
import eleeter.unifystudiox.graphics.TextureFormatBit;
import eleeter.unifystudiox.graphics.api.BlitFilter;
import eleeter.unifystudiox.graphics.api.BlitMask;
import eleeter.unifystudiox.graphics.api.IGraphicsBackend;
import eleeter.unifystudiox.graphics.api.IShaderProgram;
import eleeter.unifystudiox.particle.EmitterLightSnapshot;
import eleeter.unifystudiox.renderer.core.BucketManager;
import eleeter.unifystudiox.renderer.core.RenderBatch;
import eleeter.unifystudiox.renderer.core.RenderBucket;
import eleeter.unifystudiox.renderer.core.RenderContext;
import eleeter.unifystudiox.renderer.core.RenderDispatcher;
import eleeter.unifystudiox.renderer.core.RenderPass;
import eleeter.unifystudiox.renderer.core.RenderSettings;
import eleeter.unifystudiox.renderer.entities.EntityRenderer;
import eleeter.unifystudiox.renderer.environment.BaseplateRenderer;
import eleeter.unifystudiox.renderer.environment.CloudRenderer;
import eleeter.unifystudiox.renderer.environment.SkyRenderer;
import eleeter.unifystudiox.renderer.environment.SunRenderer;
import eleeter.unifystudiox.renderer.geometry.MeshRenderer;
import eleeter.unifystudiox.renderer.lighting.ParticleLightUploader;
import eleeter.unifystudiox.renderer.lighting.PointLightRenderer;
import eleeter.unifystudiox.renderer.lighting.PointLightUploader;
import eleeter.unifystudiox.renderer.lighting.SpotlightRenderer;
import eleeter.unifystudiox.renderer.lighting.SpotlightUploader;
import eleeter.unifystudiox.renderer.model.AmbRenderer;
import eleeter.unifystudiox.renderer.model.CubeRenderer;
import eleeter.unifystudiox.renderer.model.ObjRenderer;
import eleeter.unifystudiox.renderer.shading.Sampler;
import eleeter.unifystudiox.renderer.tool.GizmoRenderer;
import eleeter.unifystudiox.renderer.tool.ViewportGizmoRenderer;
import eleeter.unifystudiox.scene.Environment;
import eleeter.unifystudiox.scene.OrbitCamera;
import eleeter.unifystudiox.scene.Scene;
import eleeter.unifystudiox.scene.SpotlightData;
import eleeter.unifystudiox.scene.entity.SceneEntity;
import eleeter.unifystudiox.scene.entity.SpotlightEntity;
import java.nio.FloatBuffer;
import java.util.Collection;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryUtil;

public class Renderer
{
    private final IGraphicsBackend backend;

    public Renderer(IGraphicsBackend backend)
    {
        this.backend = backend;
    }

    private IShaderProgram entityShader;
    private RenderContext activeContext;
    private RenderDispatcher dispatcher;
    private FloatBuffer matrixBuffer;
    private RenderBatch batch;
    private Runnable postSceneHook;

    public interface PostProcessor
    {
        void process(Framebuffer source, int targetFbo, int targetW, int targetH);
    }

    private PostProcessor postProcessor;
    private BucketManager bucketManager;
    private SpotlightData shadowSpot;

    private Framebuffer frame;
    private Framebuffer shadowMapFb;
    private Framebuffer spotShadowFb;
    private Framebuffer particleLightShadowFb;
    private EmitterLightSnapshot particleLightShadowCaster;
    private final Matrix4f lightProjection = new Matrix4f();
    private final Matrix4f lightView = new Matrix4f();
    private final Matrix4f lightSpaceMatrix = new Matrix4f();

    public Framebuffer getFrame()
    {
        return frame;
    }

    public SpotlightData getShadowSpot()
    {
        return shadowSpot;
    }

    public void init(Scene scene)
    {
        this.matrixBuffer = MemoryUtil.memAllocFloat(16);

        this.entityShader = this.backend.createShaderProgram("/shaders/entity.vert", "/shaders/entity.frag", null);

        this.batch = new RenderBatch(this.backend);
        this.bucketManager = new BucketManager();
        this.dispatcher = new RenderDispatcher();

        this.dispatcher.register(new MeshRenderer());
        this.dispatcher.register(new BaseplateRenderer());
        this.dispatcher.register(new SunRenderer());
        this.dispatcher.register(new SkyRenderer());
        this.dispatcher.register(new CloudRenderer());
        this.dispatcher.register(new GizmoRenderer());
        this.dispatcher.register(new AmbRenderer());
        this.dispatcher.register(new ViewportGizmoRenderer());
        this.dispatcher.register(new CubeRenderer());
        this.dispatcher.register(new SpotlightRenderer());
        this.dispatcher.register(new PointLightRenderer());
        this.dispatcher.register(new LabelRenderer());
        this.dispatcher.register(new ObjRenderer());
        // this.dispatcher.register(new ParticleRenderer());

        if (RenderSettings.SHADOW_RESOLUTION > 0)
        {
            this.shadowMapFb = Framebuffer.builder(RenderSettings.SHADOW_RESOLUTION, RenderSettings.SHADOW_RESOLUTION)
                    .addDepthAttachment(TextureFormatBit.DEPTH24).build();
            this.spotShadowFb = Framebuffer.builder(RenderSettings.SHADOW_RESOLUTION, RenderSettings.SHADOW_RESOLUTION)
                    .addDepthAttachment(TextureFormatBit.DEPTH24).build();
            this.particleLightShadowFb = Framebuffer.builder(RenderSettings.SHADOW_RESOLUTION,
                    RenderSettings.SHADOW_RESOLUTION).addDepthAttachment(TextureFormatBit.DEPTH24).build();
        }

        Sampler.init();
        this.batch.configureGlobal();
    }

    public void resize(int width, int height)
    {
        if (width < 1 || height < 1)
        {
            return;
        }

        if (this.frame == null)
        {
            this.frame = Framebuffer.builder(width, height).withSamples(RenderSettings.MSAA_SAMPLES)
                    .addColorAttachment(TextureFormatBit.RGBA8)
                    .addDepthAttachment(TextureFormatBit.DEPTH24).build();
        } else
        {

            if (this.frame.getSamples() != RenderSettings.MSAA_SAMPLES)
            {
                this.frame.destroy();
                this.frame = Framebuffer.builder(width, height).withSamples(RenderSettings.MSAA_SAMPLES)
                        .addColorAttachment(TextureFormatBit.RGBA8)
                        .addDepthAttachment(TextureFormatBit.DEPTH24).build();
            } else
            {
                this.frame.resize(width, height);
            }
        }
    }

    public SpotlightData updateShadows(Scene scene)
    {
        if (!RenderSettings.SHADOWS_ENABLED || RenderSettings.SHADOW_RESOLUTION == 0)
        {
            return null;
        }

        this.shadowMapFb.bind();
        this.backend.setViewport(RenderSettings.SHADOW_RESOLUTION, RenderSettings.SHADOW_RESOLUTION);
        this.backend.clearDepth();

        Environment env = scene.getEnvironment();


        if (env.getSunIntensity() <= 0.0F)
        {
            this.shadowMapFb.bind();
            this.backend.clearDepth();
            this.shadowMapFb.unbind();
        } else
        {
            this.shadowMapFb.bind();
            this.backend.setViewport(RenderSettings.SHADOW_RESOLUTION, RenderSettings.SHADOW_RESOLUTION);
            this.backend.clearDepth();

            Vector3f sunDir = env.getSunDirection();

            float b = 25.0F;
            this.lightProjection.identity().ortho(-b, b, -b, b, -30.0F, 50.0F);

            Vector3f center = new Vector3f(0.0F, 0.0F, 0.0F);
            Vector3f eye = new Vector3f(sunDir).mul(-25.0F);
            this.lightView.identity().lookAt(eye, center, new Vector3f(0.0F, 1.0F, 0.0F));
            this.lightProjection.mul(this.lightView, this.lightSpaceMatrix);

            this.bucketManager.clear();
            RenderContext shadowCtx = new RenderContext(RenderPass.SHADOW_DEPTH, null, this.lightView, this.lightProjection, this.lightSpaceMatrix, this.matrixBuffer, scene, this.bucketManager, this.backend);

            submitAll(scene.getEntities(), shadowCtx);
            this.batch.flushShadow(this.bucketManager.getSortedBucket(RenderBucket.SOLID_3D), shadowCtx);

            this.shadowMapFb.unbind();
        }

        this.shadowSpot = findShadowSpot(scene);

        if (this.shadowSpot != null)
        {
            this.spotShadowFb.bind();
            this.backend.setViewport(RenderSettings.SHADOW_RESOLUTION, RenderSettings.SHADOW_RESOLUTION);
            this.backend.clearDepth();

            this.bucketManager.clear();
            RenderContext spotCtx = new RenderContext(RenderPass.SHADOW_DEPTH, null, this.shadowSpot.lightView,
                    this.shadowSpot.lightProjection, this.shadowSpot.lightSpaceMatrix, this.matrixBuffer, scene,
                    this.bucketManager, this.backend);

            submitAll(scene.getEntities(), spotCtx);
            this.batch.flushShadow(this.bucketManager.getSortedBucket(RenderBucket.SOLID_3D), spotCtx);

            this.spotShadowFb.unbind();
        }


        return this.shadowSpot;
    }

    /*
    private void updateParticleLightShadow(Scene scene)
    {
        this.particleLightShadowCaster = null;

        if (!RenderSettings.SHADOWS_ENABLED || !RenderSettings.PARTICLE_LIGHT_SHADOWS_ENABLED
                || this.particleLightShadowFb == null)
        {
            return;
        }

        //List<EmitterLightSnapshot> lights = ParticleLightUploader.collect(scene);
       // int uploadedCount = Math.min(lights.size(), RenderSettings.MAX_PARTICLE_LIGHTS);
       // EmitterLightSnapshot caster = ParticleLightUploader.resolveShadowCaster(scene);

        if (caster == null || !caster.active)
        {
            return;
        }

        this.particleLightShadowCaster = caster;

        this.particleLightShadowFb.bind();
        this.backend.setViewport(RenderSettings.SHADOW_RESOLUTION, RenderSettings.SHADOW_RESOLUTION);
        this.backend.clearDepth();

        this.bucketManager.clear();
        RenderContext particleShadowCtx = new RenderContext(RenderPass.SHADOW_DEPTH, null, caster.lightView,
                caster.lightProjection, caster.lightSpaceMatrix, this.matrixBuffer, scene,
                this.bucketManager, this.backend);

        submitAll(scene.getEntities(), particleShadowCtx);
        this.batch.flushShadow(this.bucketManager.getSortedBucket(RenderBucket.SOLID_3D), particleShadowCtx);

        this.particleLightShadowFb.unbind();
    }

     */

    public void renderToFramebuffer(Scene scene, Framebuffer target, int width, int height)
    {
        renderToFramebuffer(scene, target, width, height, true);
    }

    public void renderToFramebuffer(Scene scene, Framebuffer target, int width, int height, boolean updateShadows)
    {
        SpotlightData shadowSpot = null;
        if (updateShadows)
        {
            shadowSpot = updateShadows(scene);
        }

        target.bind();
        this.backend.setViewport(width, height);
        this.backend.clearFrame();

        OrbitCamera camera = scene.getCamera();
        RenderContext context = new RenderContext(RenderPass.MAIN, this.entityShader, camera.getViewMatrix(),
                camera.getProjectionMatrix(width, height), this.lightSpaceMatrix, this.matrixBuffer, scene,
                this.bucketManager, this.backend);
        this.activeContext = context;

        if (this.shadowMapFb != null)
        {
            this.shadowMapFb.getDepthTexture().bind(1);
        }
        this.backend.bindSampler(1, 0);

        if (shadowSpot != null && this.spotShadowFb != null)
        {
            this.spotShadowFb.getDepthTexture().bind(2);
        }

        this.backend.bindSampler(2, 0);

        if (this.particleLightShadowCaster != null && this.particleLightShadowFb != null)
        {
            this.particleLightShadowFb.getDepthTexture().bind(RenderSettings.PARTICLE_LIGHT_SHADOW_TEXTURE_UNIT);
        }

        this.backend.bindSampler(RenderSettings.PARTICLE_LIGHT_SHADOW_TEXTURE_UNIT, 0);

        renderScene(context, shadowSpot);
        target.unbind();
    }


    public Framebuffer renderForScreenshot(Scene scene, int captureW, int captureH)
    {
        Framebuffer msaaFb = Framebuffer.builder(captureW, captureH)
                .withSamples(RenderSettings.MSAA_SAMPLES)
                .addColorAttachment(TextureFormatBit.RGBA8)
                .addDepthAttachment(TextureFormatBit.DEPTH24)
                .build();

        Framebuffer resolveFb = Framebuffer.builder(captureW, captureH)
                .withSamples(1)
                .addColorAttachment(TextureFormatBit.RGBA8)
                .build();

        /* 1. Render full 3D scene to MSAA FBO */
        renderToFramebuffer(scene, msaaFb, captureW, captureH);

        if (this.postProcessor != null)
        {
            this.postProcessor.process(msaaFb, resolveFb.getHandle(), captureW, captureH);
        } else
        {
            msaaFb.blitTo(resolveFb, BlitMask.COLOR_BUFFER, BlitFilter.LINEAR);
        }
        msaaFb.destroy();

        resolveFb.bind();
        this.backend.setViewport(captureW, captureH);
        scene.getUi().render(captureW, captureH, captureW, captureH);
        this.backend.resetState();
        resolveFb.unbind();

        return resolveFb;
    }

    public void render(Scene scene, int viewportWidth, int viewportHeight, int logicalWidth, int logicalHeight)
    {
        if (viewportWidth < 1 || viewportHeight < 1)
        {
            return;
        }

        if (RenderSettings.PENDING_QUALITY_UPDATE)
        {
            applyQualityUpdates(viewportWidth, viewportHeight);
            RenderSettings.PENDING_QUALITY_UPDATE = false;
        }

        boolean frameSizeChanged = this.frame == null
                || this.frame.getWidth() != viewportWidth
                || this.frame.getHeight() != viewportHeight;

        if (frameSizeChanged)
        {
            resize(viewportWidth, viewportHeight);
        }

        /* TODO: REMOVE THIS GARBAGE */
        boolean shouldRenderScene = !scene.isPaused() || frameSizeChanged || scene.getAnimationSystem() != null;

        boolean skip3DForUIBatchTesting = false;

        if (shouldRenderScene && !skip3DForUIBatchTesting)
        {
            renderToFramebuffer(scene, this.frame, viewportWidth, viewportHeight, true);
        } else
        {
            this.frame.bind();
            this.backend.setViewport(viewportWidth, viewportHeight);
            this.backend.clearFrame();
            this.frame.unbind();
        }

        this.backend.setViewport(viewportWidth, viewportHeight);

        if (this.postProcessor != null)
        {
            this.postProcessor.process(this.frame, 0, viewportWidth, viewportHeight);
        } else
        {
            this.frame.blitTo(null, BlitMask.COLOR_BUFFER, BlitFilter.LINEAR);
        }

        GL45C.glBindFramebuffer(GL45C.GL_FRAMEBUFFER, 0);

        if (this.activeContext != null && scene.getUi().isVisible())
        {
            this.batch.flush(this.bucketManager.getSortedBucket(RenderBucket.EDITOR), this.activeContext);
        }


        scene.getUi().render(logicalWidth, logicalHeight, viewportWidth, viewportHeight);

        this.backend.resetState();
    }

    private void applyQualityUpdates(int currentWidth, int currentHeight)
    {
        if (this.shadowMapFb != null)
        {
            this.shadowMapFb.destroy();
            this.shadowMapFb = null;
        }
        if (this.spotShadowFb != null)
        {
            this.spotShadowFb.destroy();
            this.spotShadowFb = null;
        }
        if (this.particleLightShadowFb != null)
        {
            this.particleLightShadowFb.destroy();
            this.particleLightShadowFb = null;
        }

        if (RenderSettings.SHADOW_RESOLUTION > 0 && RenderSettings.SHADOWS_ENABLED)
        {
            this.shadowMapFb = Framebuffer.builder(RenderSettings.SHADOW_RESOLUTION, RenderSettings.SHADOW_RESOLUTION).addDepthAttachment(TextureFormatBit.DEPTH24).build();
            this.spotShadowFb = Framebuffer.builder(RenderSettings.SHADOW_RESOLUTION, RenderSettings.SHADOW_RESOLUTION).addDepthAttachment(TextureFormatBit.DEPTH24).build();
            this.particleLightShadowFb = Framebuffer.builder(RenderSettings.SHADOW_RESOLUTION, RenderSettings.SHADOW_RESOLUTION).addDepthAttachment(TextureFormatBit.DEPTH24).build();
        }

        resize(currentWidth, currentHeight);
    }

    public void renderScene(RenderContext context, SpotlightData shadowSpot)
    {
        Scene scene = context.scene();
        Environment env = scene.getEnvironment();

        this.entityShader.bind();

        this.entityShader.setUniform("uSpotShadowMap", 2);
        if (shadowSpot != null)
        {
            this.entityShader.setUniformMatrix4f("uSpotLightSpaceMatrix",
                    shadowSpot.lightSpaceMatrix.get(this.matrixBuffer));
            this.entityShader.setUniform("uHasSpotShadow", true);
        } else
        {
            this.entityShader.setUniform("uHasSpotShadow", false);
        }

        OrbitCamera camera = scene.getCamera();
        this.entityShader.setUniformMatrix4f("uView", context.viewMatrix().get(matrixBuffer));
        this.entityShader.setUniformMatrix4f("uProjection", context.projectionMatrix().get(matrixBuffer));
        this.entityShader.setUniform("uCameraPos", camera.getPosition().x, camera.getPosition().y,
                camera.getPosition().z);

        this.entityShader.setUniform("uSunDirection", env.getSunDirection().x, env.getSunDirection().y, env.getSunDirection().z);
        this.entityShader.setUniform("uSunColor", env.getSunColor().x * env.getSunIntensity(),
                env.getSunColor().y * env.getSunIntensity(), env.getSunColor().z * env.getSunIntensity());
        this.entityShader.setUniform("uAmbientColor", env.getAmbientColor().x * env.getAmbientIntensity(), env.getAmbientColor().y * env.getAmbientIntensity(), env.getAmbientColor().z * env.getAmbientIntensity());
        this.entityShader.setUniformMatrix4f("uLightSpaceMatrix", this.lightSpaceMatrix.get(this.matrixBuffer));
        this.entityShader.setUniform("uShadowMap", 1);
        this.entityShader.setUniform("uHasSunShadow", RenderSettings.SHADOWS_ENABLED && this.shadowMapFb != null);

        this.entityShader.setUniform("uFogEnabled", env.isFogEnabled() && RenderSettings.FOG_ENABLED);
        this.entityShader.setUniform("uFogColor", env.getFogColor().x, env.getFogColor().y, env.getFogColor().z);
        this.entityShader.setUniform("uFogDensity", env.getFogDensity());
        this.entityShader.setUniform("uFogStart", env.getFogStart());
        this.entityShader.setUniform("uFogEnd", env.getFogEnd());
        this.entityShader.setUniform("uNightMode", RenderSettings.NIGHT_MODE);

        SpotlightUploader.upload(this.entityShader, scene);
        PointLightUploader.upload(this.entityShader, scene);
        ParticleLightUploader.upload(this.entityShader, scene, this.matrixBuffer);

        this.entityShader.unbind();

        this.bucketManager.clear();
        submitAll(scene.getEntities(), context);

        this.batch.flush(this.bucketManager.getSortedBucket(RenderBucket.BACKGROUND), context);
        this.batch.flush(this.bucketManager.getSortedBucket(RenderBucket.SOLID_3D), context);
        this.batch.flush(this.bucketManager.getSortedBucket(RenderBucket.WORLD_2D), context);

        if (this.postSceneHook != null)
        {
            this.postSceneHook.run();
            this.backend.resetState();
        }
    }

    public void renderEditorPass(RenderContext context)
    {
        this.batch.flush(this.bucketManager.getSortedBucket(RenderBucket.EDITOR), context);
    }

    public void setPostSceneHook(Runnable hook)
    {
        this.postSceneHook = hook;
    }

    public void setPostProcessor(PostProcessor hook)
    {
        this.postProcessor = hook;
    }

    public void resetState()
    {
        this.backend.resetState();
    }

    public void cleanup()
    {
        this.dispatcher.cleanup();
        Sampler.cleanup();

        if (this.frame != null)
        {
            this.frame.destroy();
        }

        if (this.entityShader != null)
        {
            this.entityShader.cleanup();
        }

        if (this.matrixBuffer != null)
        {
            MemoryUtil.memFree(this.matrixBuffer);
        }
    }

    @SuppressWarnings("unchecked")
    private void submitAll(Collection<SceneEntity> entities, RenderContext context)
    {
        for (SceneEntity entity : entities)
        {
            EntityRenderer renderer = this.dispatcher.getRendererFor(entity);
            if (renderer != null)
            {
                renderer.submitCommands(entity, context);
            }
        }
    }

    private SpotlightData findShadowSpot(Scene scene)
    {
        for (SceneEntity e : scene.getEntities())
        {
            if (e instanceof SpotlightEntity se)
            {
                if (se.getData().enabled && se.getData().castShadow)
                {
                    return se.getData();
                }
            }
        }
        return null;
    }

    public IShaderProgram getEntityShader()
    {
        return this.entityShader;
    }

    public FloatBuffer getMatrixBuffer()
    {
        return this.matrixBuffer;
    }

    public RenderDispatcher getDispatcher()
    {
        return this.dispatcher;
    }

    public BucketManager getBucketManager()
    {
        return this.bucketManager;
    }
}
