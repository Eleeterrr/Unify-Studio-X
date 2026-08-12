package eleeter.unifystudiox.editor.animation;

import static org.lwjgl.glfw.GLFW.glfwGetCurrentContext;
import static org.lwjgl.opengl.GL45C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL45C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL45C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL45C.GL_FLOAT;
import static org.lwjgl.opengl.GL45C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL45C.GL_LINES;
import static org.lwjgl.opengl.GL45C.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL45C.glBindFramebuffer;
import static org.lwjgl.opengl.GL45C.glBindVertexArray;
import static org.lwjgl.opengl.GL45C.glClear;
import static org.lwjgl.opengl.GL45C.glClearColor;
import static org.lwjgl.opengl.GL45C.glCreateBuffers;
import static org.lwjgl.opengl.GL45C.glCreateVertexArrays;
import static org.lwjgl.opengl.GL45C.glDeleteBuffers;
import static org.lwjgl.opengl.GL45C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL45C.glDisable;
import static org.lwjgl.opengl.GL45C.glDrawArrays;
import static org.lwjgl.opengl.GL45C.glEnable;
import static org.lwjgl.opengl.GL45C.glEnableVertexArrayAttrib;
import static org.lwjgl.opengl.GL45C.glLineWidth;
import static org.lwjgl.opengl.GL45C.glNamedBufferData;
import static org.lwjgl.opengl.GL45C.glVertexArrayAttribBinding;
import static org.lwjgl.opengl.GL45C.glVertexArrayAttribFormat;
import static org.lwjgl.opengl.GL45C.glVertexArrayVertexBuffer;
import static org.lwjgl.opengl.GL45C.glViewport;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import eleeter.unifystudiox.assets.IModelAsset;
import eleeter.unifystudiox.assets.ModelPreviewSpec;
import eleeter.unifystudiox.graphics.Framebuffer;
import eleeter.unifystudiox.graphics.TextureFormatBit;
import eleeter.unifystudiox.graphics.api.BlitFilter;
import eleeter.unifystudiox.graphics.api.BlitMask;
import eleeter.unifystudiox.graphics.gl.GLGraphicsBackend;
import eleeter.unifystudiox.renderer.Renderer;
import eleeter.unifystudiox.renderer.core.RenderContext;
import eleeter.unifystudiox.renderer.core.RenderPass;
import eleeter.unifystudiox.renderer.core.RenderSettings;
import eleeter.unifystudiox.renderer.shading.ShaderProgram;
import eleeter.unifystudiox.scene.OrbitCamera;
import eleeter.unifystudiox.scene.Scene;
import eleeter.unifystudiox.scene.SelectionManager;
import eleeter.unifystudiox.scene.entity.HierarchicalEntity;
import eleeter.unifystudiox.scene.entity.Positionable;
import eleeter.unifystudiox.scene.entity.RiggedEntity;
import eleeter.unifystudiox.scene.entity.SceneEntity;
import eleeter.unifystudiox.scene.entity.gizmo.GizmoEntity;

public class ModelEditViewport
{
    /* Grid parameters */
    private static final int GRID_HALF_LINES = 16;
    private static final float GRID_CELL = 1.0F;

    /* Axis-line colours */
    private static final float GRID_R = 0.22F, GRID_G = 0.24F, GRID_B = 0.30F;
    private static final float AXIS_X_R = 0.78F, AXIS_X_G = 0.25F, AXIS_X_B = 0.25F;
    private static final float AXIS_Z_R = 0.25F, AXIS_Z_G = 0.45F, AXIS_Z_B = 0.90F;

    /* GL resources */
    private ShaderProgram gridShader;
    private int vaoGrid, vboGrid, gridVertexCount;
    private int vaoAxes, vboAxes;
    private FloatBuffer matrixBuf;
    private Framebuffer framebuffer;
    private Framebuffer resolveFramebuffer;
    private GLGraphicsBackend graphicsBackend;

    /* Cached per-frame matrices */
    private final Matrix4f cachedProj = new Matrix4f();
    private final Matrix4f cachedView = new Matrix4f();
    private final Matrix4f cachedModel = new Matrix4f();

    /* Reused input handler */
    private EditorInputHandler editorInput;

    /* State */
    private final OrbitCamera camera;
    private boolean initialized = false;
    private int lastWidth = 0;
    private int lastHeight = 0;

    private BoneOverlayRenderer boneOverlay;
    private Scene scene;
    private Renderer renderer;
    private IModelAsset currentModel;
    private SceneEntity currentEntity;
    private String currentEntityId;

    public ModelEditViewport()
    {
        this.scene = new Scene();
        this.camera = this.scene.getCamera();
    }


    public int render(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, float contentX, float contentY, int width, int height, double deltaTime)
    {
        this.ensureInitialized();
        if (!this.initialized || width < 1 || height < 1)
        {
            return 0;
        }

        this.ensureFramebuffer(width, height);
        this.camera.update(deltaTime);

        this.editorInput.update(context, contentX, contentY);

        if (this.currentEntity != null)
        {
            this.updateViewportGizmo(this.editorInput, width, height, deltaTime);
        }

        this.graphicsBackend.resetState();

        this.framebuffer.bind();
        glViewport(0, 0, width, height);
        glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glDisable(GL_DEPTH_TEST);

        Matrix4f proj = this.buildProj(width, height);
        Matrix4f view = this.camera.getViewMatrix();
        Matrix4f model = this.cachedModel.identity();

        this.gridShader.bind();
        this.gridShader.setUniform("uProjection", proj, this.matrixBuf);
        this.gridShader.setUniform("uView", view, this.matrixBuf);
        this.gridShader.setUniform("uModel", model, this.matrixBuf);

        glLineWidth(1.0F);
        glBindVertexArray(this.vaoGrid);
        this.gridShader.setUniform("uColor", GRID_R, GRID_G, GRID_B);
        glDrawArrays(GL_LINES, 0, this.gridVertexCount);

        glLineWidth(1.8F);
        glBindVertexArray(this.vaoAxes);
        this.gridShader.setUniform("uColor", AXIS_X_R, AXIS_X_G, AXIS_X_B);
        glDrawArrays(GL_LINES, 0, 2);
        this.gridShader.setUniform("uColor", AXIS_Z_R, AXIS_Z_G, AXIS_Z_B);
        glDrawArrays(GL_LINES, 2, 2);

        glBindVertexArray(0);
        this.gridShader.unbind();

        glEnable(GL_DEPTH_TEST);

        if (this.currentEntity != null)
        {
            RenderContext renderCtx = new RenderContext(RenderPass.MAIN, this.renderer.getEntityShader(), this.camera.getViewMatrix(), this.buildProj(width, height), this.cachedModel.identity(), this.renderer.getMatrixBuffer(), this.scene, this.renderer.getBucketManager(), this.graphicsBackend);

            boolean prevNight = RenderSettings.NIGHT_MODE;
            boolean prevShadows = RenderSettings.SHADOWS_ENABLED;
            boolean prevFog = RenderSettings.FOG_ENABLED;
            
            RenderSettings.NIGHT_MODE = false;
            RenderSettings.SHADOWS_ENABLED = false;
            RenderSettings.FOG_ENABLED = false;

            try
            {
                this.renderer.renderScene(renderCtx, null);
                this.renderer.renderEditorPass(renderCtx);
            } finally
            {
                RenderSettings.NIGHT_MODE = prevNight;
                RenderSettings.SHADOWS_ENABLED = prevShadows;
                RenderSettings.FOG_ENABLED = prevFog;
            }

            if (this.currentEntity instanceof RiggedEntity riggedEntity)
            {
                this.boneOverlay.draw(
                        riggedEntity,
                        this.currentEntity.getModelMatrix(),
                        this.camera.getViewMatrix(),
                        this.buildProj(width, height));
            }
        }

        this.framebuffer.blitTo(this.resolveFramebuffer,
                BlitMask.COLOR_BUFFER,
                BlitFilter.LINEAR);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return this.resolveFramebuffer.getColorTexture(0).getHandle();
    }

    /**
     * Builds the projection matrix into the cached field.
     */
    private Matrix4f buildProj(int w, int h)
    {
        float aspect = (float) w / Math.max(1, h);
        return this.cachedProj.setPerspective(
                (float) Math.toRadians(this.camera.getFov()), aspect,
                this.camera.getNear(), this.camera.getFar());
    }

    private void updateViewportGizmo(EditorInputHandler input, int vpW, int vpH, double deltaTime)
    {
        Matrix4f proj = this.buildProj(vpW, vpH);
        Matrix4f view = this.camera.getViewMatrix();

        GizmoEntity sceneGizmo = this.scene.getGizmoEntity();
        SelectionManager selMgr = this.scene.getSelectionManager();

        sceneGizmo.update(deltaTime);
        sceneGizmo.getRotation().identity();

        this.scene.getGizmoController().update(input, sceneGizmo, vpW, vpH, proj, view);

        boolean outsideViewport = !input.isMouseInsideViewport(vpW, vpH);
        SceneEntity newSelection = selMgr.updateSelection(
                input, vpW, vpH, proj, view,
                this.scene.getEntities(), this.scene.getSelectedEntity(),
                sceneGizmo.isHovered() || outsideViewport);

        selMgr.updateHover(
                input, vpW, vpH, proj, view,
                this.scene.getEntities(), sceneGizmo.isHovered());

        if (newSelection != this.scene.getSelectedEntity())
        {
            this.scene.selectEntity(newSelection);
        }

        SceneEntity target = this.scene.getSelectedEntity();
        if (target instanceof HierarchicalEntity hierarchical)
        {
            int subIndex = selMgr.getSelectedSubIndex();
            if (subIndex >= 0)
            {
                SceneEntity subEntity = hierarchical.getSubEntity(subIndex);
                if (subEntity != null)
                {
                    target = subEntity;
                }
            }
        }

        this.scene.getGizmoEntity().setTargetEntity(target);

        this.currentEntity.update(deltaTime);
        sceneGizmo.update(deltaTime);
        sceneGizmo.getRotation().identity();
    }

    public boolean isGizmoHovered()
    {
        return this.scene != null && this.scene.getGizmoEntity().isHovered();
    }

    public boolean isGizmoDragging()
    {
        return this.scene != null && this.scene.getGizmoController().isDragging();
    }

    public void setModel(IModelAsset model)
    {
        this.ensureInitialized();
        if (!this.initialized)
        {
            return;
        }

        if (this.currentEntityId != null)
        {
            this.scene.removeEntity(this.currentEntityId);
            this.currentEntityId = null;
            this.currentEntity = null;
        }

        this.scene.selectEntity(null);

        this.currentModel = model;
        if (model == null)
        {
            return;
        }

        ModelPreviewSpec previewSpec = model.getPreviewSpec();
        if (previewSpec == null)
        {
            return;
        }

        this.currentEntity = previewSpec.createPreviewEntity();
        if (this.currentEntity == null)
        {
            return;
        }

        this.currentEntityId = this.currentEntity.getId();
        this.scene.addEntity(this.currentEntity);
        this.scene.selectEntity(this.currentEntity);

        if (this.currentEntity instanceof Positionable positionable)
        {
            ModelPreviewSpec.Bounds bounds = previewSpec.getBounds();
            if (bounds != null && bounds.isValid())
            {
                float baseRenderScale = previewSpec.getBaseRenderScale();
                float halfExtent = Math.max(bounds.getSizeX(),
                        Math.max(bounds.getSizeY(), bounds.getSizeZ())) * 0.5F;
                if (halfExtent > 0.0001F)
                {
                    float targetHalfExtent = 2.10F;
                    float uniformScale = targetHalfExtent
                            / Math.max(0.0001F, halfExtent * baseRenderScale);
                    uniformScale = Math.max(0.05F, Math.min(8.0F, uniformScale));

                    Vector3f centeredPosition = new Vector3f(
                            -bounds.getCenterX() * uniformScale * baseRenderScale,
                            -bounds.getCenterY() * uniformScale * baseRenderScale,
                            -bounds.getCenterZ() * uniformScale * baseRenderScale);

                    positionable.setScale(new Vector3f(uniformScale, uniformScale, uniformScale));
                    positionable.setPosition(centeredPosition);
                }
            }
        }

        this.camera.snapTo(-30.0F, 25.0F);
        this.camera.getTarget().set(0.0F, 0.0F, 0.0F);
    }

    public OrbitCamera getCamera()
    {
        return this.camera;
    }

    public String getCurrentEntityId()
    {
        return this.currentEntityId;
    }

    public Scene getScene()
    {
        return this.scene;
    }

    public Positionable getActiveTransformTarget()
    {
        if (this.scene == null)
        {
            return null;
        }

        SceneEntity target = this.scene.getGizmoEntity().getTargetEntity();
        return target instanceof Positionable positionable ? positionable : null;
    }


    public void cleanup()
    {
        if (this.initialized)
        {
            if (this.currentEntityId != null)
            {
                this.scene.removeEntity(this.currentEntityId);
                this.currentEntityId = null;
                this.currentEntity = null;
            }
            if (this.renderer != null)
            {
                this.renderer.cleanup();
                this.renderer = null;
            }
            if (this.scene != null)
            {
                this.scene.cleanup();
                this.scene = null;
            }
            if (this.boneOverlay != null)
            {
                this.boneOverlay.cleanup();
                this.boneOverlay = null;
            }

            glDeleteVertexArrays(this.vaoGrid);
            glDeleteBuffers(this.vboGrid);
            glDeleteVertexArrays(this.vaoAxes);
            glDeleteBuffers(this.vboAxes);
            this.gridShader.cleanup();

            if (this.matrixBuf != null)
            {
                MemoryUtil.memFree(this.matrixBuf);
                this.matrixBuf = null;
            }
            if (this.framebuffer != null)
            {
                this.framebuffer.destroy();
                this.framebuffer = null;
            }
            if (this.resolveFramebuffer != null)
            {
                this.resolveFramebuffer.destroy();
                this.resolveFramebuffer = null;
            }
            this.initialized = false;
        }
    }


    private void ensureInitialized()
    {
        if (this.initialized) return;
        if (!hasGLContext()) return;

        this.scene.getEnvironment().setFogEnabled(false);
        this.scene.getEnvironment().setAmbientColor(0.4F, 0.4F, 0.4F);
        this.scene.getEnvironment().setSunIntensity(1.0F);
        this.scene.getEnvironment().setSunColor(1.0F, 0.95F, 0.85F);
        this.scene.getEnvironment().setSunDirection(0.5F, -1.0F, 0.3F);

        this.graphicsBackend = new GLGraphicsBackend();
        this.renderer = new Renderer(this.graphicsBackend);
        this.renderer.init(this.scene);

        this.camera.setFov(60.0F);
        this.camera.snapTo(-30.0F, 25.0F);
        this.camera.update(1.0D);

        /* Allocate the reusable input handler once */
        this.editorInput = new EditorInputHandler(null, 0, 0);

        this.matrixBuf = MemoryUtil.memAllocFloat(16);
        this.gridShader = ShaderProgram.builder()
                .vertex("/shaders/gizmo_simple.vert")
                .fragment("/shaders/gizmo_simple.frag")
                .build();

        this.boneOverlay = new BoneOverlayRenderer();
        this.boneOverlay.init();

        this.buildGridVao();
        this.buildAxesVao();
        this.initialized = true;
    }

    private void buildGridVao()
    {
        int lineCount = GRID_HALF_LINES * 4;
        float extent = GRID_HALF_LINES * GRID_CELL;
        float[] verts = new float[lineCount * 2 * 3];
        int idx = 0;

        for (int i = -GRID_HALF_LINES; i <= GRID_HALF_LINES; i++)
        {
            if (i == 0) continue;
            float p = i * GRID_CELL;
            verts[idx++] = -extent;
            verts[idx++] = 0.0F;
            verts[idx++] = p;
            verts[idx++] = extent;
            verts[idx++] = 0.0F;
            verts[idx++] = p;
            verts[idx++] = p;
            verts[idx++] = 0.0F;
            verts[idx++] = -extent;
            verts[idx++] = p;
            verts[idx++] = 0.0F;
            verts[idx++] = extent;
        }

        this.gridVertexCount = idx / 3;
        this.vaoGrid = glCreateVertexArrays();
        this.vboGrid = glCreateBuffers();

        FloatBuffer buf = MemoryUtil.memAllocFloat(idx);
        buf.put(verts, 0, idx).flip();
        glNamedBufferData(this.vboGrid, buf, GL_STATIC_DRAW);
        MemoryUtil.memFree(buf);

        glEnableVertexArrayAttrib(this.vaoGrid, 0);
        glVertexArrayAttribFormat(this.vaoGrid, 0, 3, GL_FLOAT, false, 0);
        glVertexArrayAttribBinding(this.vaoGrid, 0, 0);
        glVertexArrayVertexBuffer(this.vaoGrid, 0, this.vboGrid, 0, 3 * Float.BYTES);
    }

    private void buildAxesVao()
    {
        float extent = GRID_HALF_LINES * GRID_CELL;
        float[] verts =
        {
                -extent, 0.0F, 0.0F, extent, 0.0F, 0.0F,
                0.0F, 0.0F, -extent, 0.0F, 0.0F, extent
        };

        this.vaoAxes = glCreateVertexArrays();
        this.vboAxes = glCreateBuffers();

        FloatBuffer buf = MemoryUtil.memAllocFloat(verts.length);
        buf.put(verts).flip();
        glNamedBufferData(this.vboAxes, buf, GL_STATIC_DRAW);
        MemoryUtil.memFree(buf);

        glEnableVertexArrayAttrib(this.vaoAxes, 0);
        glVertexArrayAttribFormat(this.vaoAxes, 0, 3, GL_FLOAT, false, 0);
        glVertexArrayAttribBinding(this.vaoAxes, 0, 0);
        glVertexArrayVertexBuffer(this.vaoAxes, 0, this.vboAxes, 0, 3 * Float.BYTES);
    }

    private void ensureFramebuffer(int width, int height)
    {
        if (this.framebuffer != null && this.lastWidth == width && this.lastHeight == height)
        {
            return;
        }
        if (this.framebuffer != null) this.framebuffer.destroy();
        if (this.resolveFramebuffer != null) this.resolveFramebuffer.destroy();

        this.framebuffer = Framebuffer.builder(width, height)
                .withSamples(RenderSettings.MSAA_SAMPLES)
                .addColorAttachment(TextureFormatBit.RGBA8)
                .addDepthAttachment(TextureFormatBit.DEPTH24)
                .build();
        this.resolveFramebuffer = Framebuffer.builder(width, height)
                .withSamples(1)
                .addColorAttachment(TextureFormatBit.RGBA8)
                .build();
        this.lastWidth = width;
        this.lastHeight = height;
    }

    private static boolean hasGLContext()
    {
        if (glfwGetCurrentContext() == 0L) return false;
        try
        {
            return GL.getCapabilities() != null;
        } catch (IllegalStateException ignored)
        {
            return false;
        }
    }
}
