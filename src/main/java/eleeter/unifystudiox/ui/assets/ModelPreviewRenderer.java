package eleeter.unifystudiox.ui.assets;

import eleeter.unifystudiox.assets.IModelAsset;
import eleeter.unifystudiox.assets.ModelPreviewSpec;
import eleeter.unifystudiox.graphics.Framebuffer;
import eleeter.unifystudiox.graphics.TextureFormatBit;
import eleeter.unifystudiox.graphics.gl.GLGraphicsBackend;
import eleeter.unifystudiox.graphics.gl.GlConstants;
import eleeter.unifystudiox.graphics.gl.GlTexture;
import eleeter.unifystudiox.renderer.Renderer;
import eleeter.unifystudiox.renderer.core.RenderSettings;
import eleeter.unifystudiox.scene.Scene;
import eleeter.unifystudiox.scene.entity.Positionable;
import eleeter.unifystudiox.scene.entity.SceneEntity;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;


public class ModelPreviewRenderer
{
    private static final int THUMB_SIZE = 192;
    private static final int MAX_CACHE_SIZE = 512;

    private final Renderer renderer = new Renderer(new GLGraphicsBackend());
    private final Scene previewScene = new Scene();
    private Framebuffer previewFramebuffer;
    private GlTexture glTexture;

    private final Map<String, Integer> textureCache = new HashMap<>();
    private final Map<String, Integer> lastUsedFrame = new HashMap<>();
    private final Map<String, SceneEntity> entityCache = new HashMap<>();
    private final Map<String, IModelAsset> liveAssets = new HashMap<>();
    private final Map<String, IModelAsset> pendingAssets = new HashMap<>();
    private final ArrayDeque<String> renderQueue = new ArrayDeque<>();
    private final Set<String> queuedIds = new HashSet<>();

    private String activePreviewEntityId;
    private int frameCounter = 0;
    private boolean initialized = false;
    private float previewYawDegrees = 0.0F;
    private float previewPitchDegrees = 0.0F;
    private float animatedYawDegrees = 0.0F;
    private float animatedPitchDegrees = 0.0F;
    private float appliedCameraYawDegrees = 0.0F;
    private float appliedCameraPitchDegrees = 0.0F;


    public ModelPreviewRenderer()
    {
        this.glTexture = new GlTexture();
    }

    public void syncAssets(List<IModelAsset> assets)
        {

        this.frameCounter++;
        Set<String> liveIds = new HashSet<>();

        for (IModelAsset asset : assets)
        {
            String id = asset.getId();
            liveIds.add(id);
            this.liveAssets.put(id, asset);

            if (!this.textureCache.containsKey(id) && this.queuedIds.add(id))
            {
                this.pendingAssets.put(id, asset);
                this.renderQueue.addLast(id);
            }
        }

        List<String> stale = new ArrayList<>();
        for (String cachedId : this.textureCache.keySet())
        {
            if (!liveIds.contains(cachedId))
            {
                stale.add(cachedId);
            }
        }
        for (String staleId : stale)
        {
            Integer handle = this.textureCache.remove(staleId);
            this.lastUsedFrame.remove(staleId);
            this.liveAssets.remove(staleId);
            this.entityCache.remove(staleId);

            if (handle != null)
            {
                glTexture.deleteTextures(handle);
            }
        }

        trimCacheIfNeeded(liveIds);
    }

    public void renderPending(int budgetPerFrame)
    {
        ensureInitialized();
        if (!this.initialized)
        {
            return;
        }
        if (advancePreviewCameraAnimation())
        {
            queueAllLiveAssetsForRefresh();
        }
        int remaining = Math.max(1, budgetPerFrame);

        while (remaining > 0 && !this.renderQueue.isEmpty())
        {
            String id = this.renderQueue.removeFirst();
            this.queuedIds.remove(id);
            IModelAsset asset = this.pendingAssets.remove(id);
            if (asset != null)
            {
                renderAssetPreview(asset);
            }
            remaining--;
        }
    }

    public int getPreviewTextureHandle(String assetId)
    {
        Integer handle = this.textureCache.get(assetId);
        if (handle != null)
        {
            this.lastUsedFrame.put(assetId, this.frameCounter);
            return handle;
        }
        return 0;
    }


    public void addPreviewRotationDelta(float yawDeltaDegrees, float pitchDeltaDegrees)
    {
        float newYaw = this.previewYawDegrees + yawDeltaDegrees;
        float newPitch = clamp(this.previewPitchDegrees + pitchDeltaDegrees, -89.0F, 89.0F);

        if (Math.abs(newYaw - this.previewYawDegrees) < 0.01F
                && Math.abs(newPitch - this.previewPitchDegrees) < 0.01F)
        {
            return;
        }

        this.previewYawDegrees = newYaw;
        this.previewPitchDegrees = newPitch;
        queueAllLiveAssetsForRefresh();
    }

    public void cleanup()
    {
        boolean canUseGl = hasOpenGLContext();
        if (canUseGl)
        {
            if (this.activePreviewEntityId != null)
            {
                this.previewScene.removeEntity(this.activePreviewEntityId);
                this.activePreviewEntityId = null;
            }
            for (Integer handle : this.textureCache.values())
            {
                glTexture.deleteTextures(handle);
            }
        }
        this.textureCache.clear();
        this.lastUsedFrame.clear();
        this.liveAssets.clear();
        this.pendingAssets.clear();
        this.renderQueue.clear();
        this.queuedIds.clear();

        if (canUseGl)
        {
            if (this.previewFramebuffer != null)
            {
                this.previewFramebuffer.destroy();
                this.previewFramebuffer = null;
            }
            this.renderer.cleanup();
            this.previewScene.cleanup();
        }
        this.previewFramebuffer = null;
        this.initialized = false;
    }

    private void ensureInitialized()
    {
        if (this.initialized)
        {
            return;
        }
        if (!hasOpenGLContext())
        {
            return;
        }

        this.previewScene.getEnvironment().setFogEnabled(false);
        this.previewScene.getEnvironment().setAmbientColor(2.5f, 2.5f, 2.5f);
        this.previewScene.getEnvironment().setSunIntensity(0.0f);
        this.previewScene.getCamera().zoom(6.5F, 1.0F);
        this.previewScene.getCamera().update(1.0D);

        this.previewFramebuffer = Framebuffer.builder(THUMB_SIZE, THUMB_SIZE)
                .addColorAttachment(TextureFormatBit.RGBA8)
                .addDepthAttachment(TextureFormatBit.DEPTH24)
                .build();
        this.renderer.init(this.previewScene);
        this.animatedYawDegrees = 0.0f;
        this.animatedPitchDegrees = 0.0f;
        this.appliedCameraYawDegrees = 0.0f;
        this.appliedCameraPitchDegrees = 0.0f;
        this.initialized = true;
    }

    private void renderAssetPreview(IModelAsset asset)
    {
        ModelPreviewSpec previewSpec = asset.getPreviewSpec();
        if (previewSpec == null)
        {
            return;
        }

        String assetId = asset.getId();
        SceneEntity previewEntity = this.entityCache.get(assetId);

        if (previewEntity == null)
        {
            previewEntity = previewSpec.createPreviewEntity();
            if (previewEntity == null)
            {
                return;
            }
            this.entityCache.put(assetId, previewEntity);
        }

        if (this.activePreviewEntityId == null || !this.activePreviewEntityId.equals(previewEntity.getId()))
        {
            if (this.activePreviewEntityId != null)
            {
                this.previewScene.removeEntity(this.activePreviewEntityId);
            }

            this.activePreviewEntityId = previewEntity.getId();
            this.previewScene.addEntity(previewEntity);
        }

        preparePreviewEntity(previewEntity, previewSpec);
        warmUpPreviewEntity(previewEntity);
        applyPreviewCameraRotation();
        this.previewScene.getCamera().update(1.0D);

        boolean previousNightMode = RenderSettings.NIGHT_MODE;
        boolean previousShadows = RenderSettings.SHADOWS_ENABLED;
        boolean previousFog = RenderSettings.FOG_ENABLED;
        
        RenderSettings.NIGHT_MODE = false;
        RenderSettings.SHADOWS_ENABLED = false;
        RenderSettings.FOG_ENABLED = false;

        try
        {
            this.renderer.resetState();
            this.renderer.renderToFramebuffer(this.previewScene, this.previewFramebuffer, THUMB_SIZE, THUMB_SIZE, false);
        }
        finally
        {
            RenderSettings.NIGHT_MODE = previousNightMode;
            RenderSettings.SHADOWS_ENABLED = previousShadows;
            RenderSettings.FOG_ENABLED = previousFog;
        }

        int targetTexture = this.textureCache.computeIfAbsent(assetId, key -> createPersistentTexture(THUMB_SIZE, THUMB_SIZE));
        copyToPersistentTexture(this.previewFramebuffer.getColorTexture(0).getHandle(), targetTexture, THUMB_SIZE, THUMB_SIZE);
        this.lastUsedFrame.put(assetId, this.frameCounter);
    }

    private static void warmUpPreviewEntity(SceneEntity previewEntity)
    {

        previewEntity.update(0.0);
        previewEntity.update(1.0 / 60.0);
    }

    private void preparePreviewEntity(SceneEntity previewEntity, ModelPreviewSpec previewSpec)
    {
        if (!(previewEntity instanceof Positionable positionable))
        {
            return;
        }

        ModelPreviewSpec.Bounds bounds = previewSpec.getBounds();
        if (bounds == null || !bounds.isValid())
        {
            return;
        }

        float baseRenderScale = previewSpec.getBaseRenderScale();
        float halfExtent = Math.max(bounds.getSizeX(), Math.max(bounds.getSizeY(), bounds.getSizeZ())) * 0.5f;
        if (halfExtent <= 0.0001f)
        {
            return;
        }

        float targetHalfExtent = 3.00f;
        float uniformScale = targetHalfExtent / Math.max(0.0001f, halfExtent * baseRenderScale);
        uniformScale = Math.max(0.05f, Math.min(128.0f, uniformScale));

        Vector3f centeredPosition = new Vector3f(-bounds.getCenterX() * uniformScale * baseRenderScale, -bounds.getCenterY() * uniformScale * baseRenderScale, -bounds.getCenterZ() * uniformScale * baseRenderScale);

        positionable.setScale(new Vector3f(uniformScale, uniformScale, uniformScale));
        positionable.setPosition(centeredPosition);
        this.previewScene.getCamera().getTarget().set(0f, 0f, 0f);
    }

    private void applyPreviewCameraRotation()
    {
        float yawDelta = this.animatedYawDegrees - this.appliedCameraYawDegrees;
        float pitchDelta = this.animatedPitchDegrees - this.appliedCameraPitchDegrees;
        if (Math.abs(yawDelta) < 0.001f && Math.abs(pitchDelta) < 0.001f)
        {
            return;
        }

        this.previewScene.getCamera().orbit(yawDelta, pitchDelta, 1.0f);
        this.appliedCameraYawDegrees = this.animatedYawDegrees;
        this.appliedCameraPitchDegrees = this.animatedPitchDegrees;
    }

    private boolean advancePreviewCameraAnimation()
    {
        float nextYaw = approach(this.animatedYawDegrees, this.previewYawDegrees, 0.22F);
        float nextPitch = approach(this.animatedPitchDegrees, this.previewPitchDegrees, 0.22F);
        if (Math.abs(nextYaw - this.animatedYawDegrees) < 0.001F
                && Math.abs(nextPitch - this.animatedPitchDegrees) < 0.001F)
        {
            return false;
        }

        this.animatedYawDegrees = nextYaw;
        this.animatedPitchDegrees = nextPitch;
        return true;
    }

    private void queueAllLiveAssetsForRefresh()
    {
        for (Map.Entry<String, IModelAsset> entry : this.liveAssets.entrySet())
        {
            String id = entry.getKey();
            if (this.queuedIds.add(id))
            {
                this.pendingAssets.put(id, entry.getValue());
                this.renderQueue.addLast(id);
            }
        }
    }

    private static boolean hasOpenGLContext()
    {
        if (GLFW.glfwGetCurrentContext() == 0L)
        {
            return false;
        }
        try
        {
            return GL.getCapabilities() != null;
        }
        catch (IllegalStateException ignored)
        {
            return false;
        }
    }

    private static float clamp(float value, float min, float max)
    {
        return Math.max(min, Math.min(max, value));
    }

    private static float approach(float current, float target, float factor)
    {
        float next = current + (target - current) * factor;
        if (Math.abs(target - next) < 0.01f)
        {
            return target;
        }
        return next;
    }

    private void trimCacheIfNeeded(Set<String> protectedIds)
    {
        if (this.textureCache.size() <= MAX_CACHE_SIZE)
        {
            return;
        }

        String candidate = null;
        int oldestFrame = Integer.MAX_VALUE;
        for (Map.Entry<String, Integer> entry : this.lastUsedFrame.entrySet())
        {
            if (protectedIds.contains(entry.getKey()))
            {
                continue;
            }
            if (entry.getValue() < oldestFrame)
            {
                oldestFrame = entry.getValue();
                candidate = entry.getKey();
            }
        }

        if (candidate != null)
        {
            Integer handle = this.textureCache.remove(candidate);
            this.lastUsedFrame.remove(candidate);
            this.entityCache.remove(candidate);
            if (handle != null)
            {
                glTexture.deleteTextures(handle);
            }
        }
    }

    private static int createPersistentTexture(int width, int height)
    {
        int handle = new GlTexture().createTextures(GlConstants.GL_TEXTURE_2D);

        new GlTexture().texture2D(handle, 1, GlConstants.GL_RGBA8, width, height);
        new GlTexture().bindTexture(GlConstants.GL_TEXTURE_2D, handle);
        new GlTexture().texParameteri(GlConstants.GL_TEXTURE_2D, GlConstants.GL_TEXTURE_MIN_FILTER, GlConstants.GL_LINEAR);
        new GlTexture().texParameteri(GlConstants.GL_TEXTURE_2D, GlConstants.GL_TEXTURE_MAG_FILTER, GlConstants.GL_LINEAR);
        new GlTexture().texParameteri(GlConstants.GL_TEXTURE_2D, GlConstants.GL_TEXTURE_WRAP_S, GlConstants.GL_CLAMP_TO_EDGE);
        new GlTexture().texParameteri(GlConstants.GL_TEXTURE_2D, GlConstants.GL_TEXTURE_WRAP_T, GlConstants.GL_CLAMP_TO_EDGE);
        new GlTexture().bindTexture(GlConstants.GL_TEXTURE_2D, 0);
        return handle;
    }

    private static void copyToPersistentTexture(int sourceTexture, int targetTexture, int width, int height)
    {
        new GlTexture().copyImageSubData(sourceTexture, GlConstants.GL_TEXTURE_2D, 0, 0, 0, 0, targetTexture, GlConstants.GL_TEXTURE_2D, 0, 0, 0, 0, width, height, 1);
    }
}
