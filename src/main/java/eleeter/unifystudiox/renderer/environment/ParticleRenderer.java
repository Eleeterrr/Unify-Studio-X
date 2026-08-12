package eleeter.unifystudiox.renderer.environment;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import eleeter.unifystudiox.graphics.Shaders;
import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.graphics.Vao;
import eleeter.unifystudiox.graphics.VertexBuffer;
import eleeter.unifystudiox.graphics.api.IShaderProgram;
import eleeter.unifystudiox.graphics.buffer.GpuBufferUsage;
import eleeter.unifystudiox.graphics.gfx.PipelineState;
import eleeter.unifystudiox.graphics.gl.GlConstants;
import eleeter.unifystudiox.graphics.gl.GlDraw;
import eleeter.unifystudiox.graphics.layout.AttributeType;
import eleeter.unifystudiox.graphics.layout.BufferLayout;
import eleeter.unifystudiox.particle.ParticleEffectEntity;
import eleeter.unifystudiox.particle.ParticleEmitter;
import eleeter.unifystudiox.renderer.core.RenderBucket;
import eleeter.unifystudiox.renderer.core.RenderCommand;
import eleeter.unifystudiox.renderer.core.RenderContext;
import eleeter.unifystudiox.renderer.core.RenderPass;
import eleeter.unifystudiox.renderer.entities.EntityRenderer;
import eleeter.unifystudiox.scene.OrbitCamera;

public class ParticleRenderer implements EntityRenderer<ParticleEffectEntity>
{
    private static final int FLOATS_PER_INSTANCE = ParticleEmitter.FLOATS_PER_INSTANCE;
    private static final float LOD_CULL_DISTANCE = 200.0F;
    private static final int FRUSTUM_PLANE_COUNT = 6;

    private static final BufferLayout QUAD_LAYOUT = BufferLayout.builder()
            .add(0, 2, AttributeType.FLOAT)
            .build();

    private static final BufferLayout INSTANCE_LAYOUT = BufferLayout.builder()
            .addInstanced(1, 3, AttributeType.FLOAT, 1)
            .addInstanced(2, 1, AttributeType.FLOAT, 1)
            .addInstanced(3, 4, AttributeType.FLOAT, 1)
            .addInstanced(4, 1, AttributeType.FLOAT, 1)
            .addInstanced(5, 4, AttributeType.FLOAT, 1)
            .addInstanced(6, 1, AttributeType.FLOAT, 1)
            .build();

    private static final float[] QUAD_VERTICES =
            {
                    -0.5F, -0.5F,
                    0.5F, -0.5F,
                    0.5F, 0.5F,
                    0.5F, 0.5F,
                    -0.5F, 0.5F,
                    -0.5F, -0.5F
            };


    private static final class EmitterGpu
    {
        final VertexBuffer instanceVbo;
        final Vao vao;
        final FloatBuffer stagingBuffer;
        int lastLiveCount;

        EmitterGpu(VertexBuffer quadVbo, int maxParticles)
        {
            this.instanceVbo = new VertexBuffer(GpuBufferUsage.STREAMING);
            this.instanceVbo.allocate((long) maxParticles * FLOATS_PER_INSTANCE * Float.BYTES);

            this.vao = Vao.builder().bindVertexBuffer(quadVbo, QUAD_LAYOUT, 0).bindVertexBuffer(this.instanceVbo, INSTANCE_LAYOUT, 1).build();

            this.stagingBuffer = MemoryUtil.memAllocFloat(maxParticles * FLOATS_PER_INSTANCE);
            this.lastLiveCount = 0;
        }

        void destroy()
        {
            this.vao.destroy();
            this.instanceVbo.destroy();
            MemoryUtil.memFree(this.stagingBuffer);
        }
    }

    private IShaderProgram shader;
    private VertexBuffer quadVbo;
    private boolean isInitialized;

    private final Map<ParticleEmitter, EmitterGpu> gpuCache = new HashMap<>();
    private final float[] frustumPlanes = new float[FRUSTUM_PLANE_COUNT * 4];

    @Override
    public Class<ParticleEffectEntity> getSupportedType()
    {
        return ParticleEffectEntity.class;
    }

    private void initGpuResources(RenderContext context)
    {
        this.shader = Shaders.particle();
        this.quadVbo = new VertexBuffer(QUAD_VERTICES, GpuBufferUsage.STATIC);
        this.isInitialized = true;
    }

    @Override
    public void submitCommands(ParticleEffectEntity entity, RenderContext context)
    {
        if (context.pass() == RenderPass.SHADOW_DEPTH)
        {
            return;
        }

        if (!this.isInitialized)
        {
            initGpuResources(context);
        }

        OrbitCamera camera = context.scene().getCamera();
        Vector3f cameraPos = camera.getPosition();

        extractFrustumPlanes(context.projectionMatrix(), context.viewMatrix());

        List<ParticleEmitter> emitters = entity.getEmitters();

        for (int e = 0; e < emitters.size(); e++)
        {
            ParticleEmitter emitter = emitters.get(e);
            int liveCount = emitter.getLiveCount();

            if (liveCount == 0)
            {
                continue;
            }

            float ex = emitter.getWorldX();
            float ey = emitter.getWorldY();
            float ez = emitter.getWorldZ();

            float dx = ex - cameraPos.x;
            float dy = ey - cameraPos.y;
            float dz = ez - cameraPos.z;
            float distSq = dx * dx + dy * dy + dz * dz;

            if (distSq > LOD_CULL_DISTANCE * LOD_CULL_DISTANCE)
            {
                continue;
            }

            float boundRadius = emitter.getMaxParticles() * 0.5F;

            if (!sphereInFrustum(ex, ey, ez, boundRadius))
            {
                continue;
            }

            EmitterGpu gpu = this.gpuCache.computeIfAbsent(emitter, em -> new EmitterGpu(this.quadVbo, em.getMaxParticles()));

            gpu.stagingBuffer.clear();
            int written = emitter.fillInstanceBuffer(gpu.stagingBuffer);
            gpu.stagingBuffer.flip();
            gpu.lastLiveCount = written;

            if (written == 0)
            {
                continue;
            }

            gpu.instanceVbo.uploadPartial(gpu.stagingBuffer, 0L);

            TextureGL texture = TextureGL.loadCached(emitter.getTexturePath());
            PipelineState state = emitter.isAdditive() ? PipelineState.ADDITIVE : PipelineState.TRANSPARENT;

            RenderCommand cmd = context.bucketManager().allocateCommand();

            float dist = (float) Math.sqrt(distSq);
            cmd.sortKey = Float.floatToRawIntBits(10000.0F - dist);

            cmd.shader = this.shader;
            cmd.texture = texture;
            cmd.vao = gpu.vao;
            cmd.count = 0;
            cmd.indexed = false;
            cmd.state = state;
            cmd.renderer = this;
            cmd.entity = entity;
            cmd.customId = e;

            cmd.modelMatrix.identity();

            context.bucketManager().submit(RenderBucket.WORLD_2D, cmd);
        }
    }

    @Override
    public void setupUniforms(IShaderProgram shader, ParticleEffectEntity entity, int customId, RenderContext context)
    {
        FloatBuffer fb = context.matrixBuffer();

        shader.setUniformMatrix4f("uView", context.viewMatrix().get(fb));
        shader.setUniformMatrix4f("uProjection", context.projectionMatrix().get(fb));

        OrbitCamera camera = context.scene().getCamera();
        Vector3f camPos = camera.getPosition();
        shader.setUniform("uCameraRight", context.viewMatrix().m00(), context.viewMatrix().m10(), context.viewMatrix().m20());
        shader.setUniform("uCameraUp", context.viewMatrix().m01(), context.viewMatrix().m11(), context.viewMatrix().m21());
        shader.setUniform("uTexture", 0);

        List<ParticleEmitter> emitters = entity.getEmitters();
        if (customId < 0 || customId >= emitters.size())
        {
            return;
        }

        ParticleEmitter emitter = emitters.get(customId);
        EmitterGpu gpu = this.gpuCache.get(emitter);

        if (gpu == null || gpu.lastLiveCount == 0)
        {
            return;
        }

        shader.setUniform("uSpriteRows", emitter.getSpriteRows());
        shader.setUniform("uSpriteCols", emitter.getSpriteCols());

        gpu.vao.bind();

        /* 6 Quad vertex count. keep in mind. */
        int QUAD_VERTEX = 6;
        GlDraw.drawArraysInstanced(GlConstants.GL_TRIANGLES, 0, QUAD_VERTEX, gpu.lastLiveCount);

        gpu.vao.unbind();
    }

    @Override
    public void cleanup()
    {
        for (EmitterGpu gpu : this.gpuCache.values())
        {
            gpu.destroy();
        }

        this.gpuCache.clear();

        if (this.quadVbo != null)
        {
            this.quadVbo.destroy();
        }

        if (this.shader != null)
        {
            this.shader.cleanup();
        }

        this.isInitialized = false;
    }


    private void extractFrustumPlanes(Matrix4f proj, Matrix4f view)
    {
        Matrix4f vp = new Matrix4f(proj).mul(view);

        /* Left */
        this.frustumPlanes[0] = vp.m30() + vp.m00();
        this.frustumPlanes[1] = vp.m31() + vp.m01();
        this.frustumPlanes[2] = vp.m32() + vp.m02();
        this.frustumPlanes[3] = vp.m33() + vp.m03();
        /* Right */
        this.frustumPlanes[4] = vp.m30() - vp.m00();
        this.frustumPlanes[5] = vp.m31() - vp.m01();
        this.frustumPlanes[6] = vp.m32() - vp.m02();
        this.frustumPlanes[7] = vp.m33() - vp.m03();
        /* Bottom */
        this.frustumPlanes[8] = vp.m30() + vp.m10();
        this.frustumPlanes[9] = vp.m31() + vp.m11();
        this.frustumPlanes[10] = vp.m32() + vp.m12();
        this.frustumPlanes[11] = vp.m33() + vp.m13();
        /* Top */
        this.frustumPlanes[12] = vp.m30() - vp.m10();
        this.frustumPlanes[13] = vp.m31() - vp.m11();
        this.frustumPlanes[14] = vp.m32() - vp.m12();
        this.frustumPlanes[15] = vp.m33() - vp.m13();
        /* Near */
        this.frustumPlanes[16] = vp.m30() + vp.m20();
        this.frustumPlanes[17] = vp.m31() + vp.m21();
        this.frustumPlanes[18] = vp.m32() + vp.m22();
        this.frustumPlanes[19] = vp.m33() + vp.m23();
        /* Far */
        this.frustumPlanes[20] = vp.m30() - vp.m20();
        this.frustumPlanes[21] = vp.m31() - vp.m21();
        this.frustumPlanes[22] = vp.m32() - vp.m22();
        this.frustumPlanes[23] = vp.m33() - vp.m23();
    }


    private boolean sphereInFrustum(float cx, float cy, float cz, float radius)
    {
        for (int i = 0; i < FRUSTUM_PLANE_COUNT; i++)
        {
            int base = i * 4;
            float a = this.frustumPlanes[base];
            float b = this.frustumPlanes[base + 1];
            float c = this.frustumPlanes[base + 2];
            float d = this.frustumPlanes[base + 3];
            float dist = a * cx + b * cy + c * cz + d;

            if (dist < -radius)
            {
                return false;
            }
        }

        return true;
    }


    private void uploadInstanceData(EmitterGpu gpu, FloatBuffer data)
    {
        gpu.instanceVbo.uploadPartial(data, 0L);
    }
}
