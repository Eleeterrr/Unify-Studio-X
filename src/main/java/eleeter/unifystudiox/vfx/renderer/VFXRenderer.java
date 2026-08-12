package eleeter.unifystudiox.vfx.renderer;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_DST_COLOR;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_COLOR;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_ZERO;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import eleeter.unifystudiox.vfx.core.VFXEffect;
import eleeter.unifystudiox.vfx.core.VFXEmitter;
import eleeter.unifystudiox.vfx.core.VFXManager;

/**
 * Top-level VFX renderer. Iterates all active effects from the {@link VFXManager},
 * applies the correct blend mode per emitter, and delegates to the appropriate
 * sub-renderer (sprite, ribbon, or ring).
 *
 * <p>This renderer must be called <em>after</em> all opaque geometry has been
 * rendered so that depth testing produces correct soft-particle fade results.
 * Depth writes are disabled during the entire VFX pass and restored afterward.</p>
 */
public class VFXRenderer
{

    private SpriteRenderer spriteRenderer;
    private RibbonRenderer ribbonRenderer;
    private RingRenderer ringRenderer;

    /**
     * Allocates all sub-renderers and their GPU resources.
     */
    public void init()
    {
        this.spriteRenderer = new SpriteRenderer();
        this.ribbonRenderer = new RibbonRenderer();
        this.ringRenderer = new RingRenderer();
        this.spriteRenderer.init();
        this.ribbonRenderer.init();
        this.ringRenderer.init();
    }

    /**
     * Renders all active VFX effects from the manager.
     * Must be called after opaque geometry and before the frame swap.
     *
     * @param manager the VFX manager containing all active effects
     * @param view    column-major float[16] view matrix
     * @param proj    column-major float[16] projection matrix
     */
    public void render(VFXManager manager, float[] view, float[] proj)
    {
        glDepthMask(false);
        glEnable(GL_DEPTH_TEST);

        for (VFXEffect effect : manager.getActiveEffects())
        {
            for (VFXEmitter emitter : effect.getEmitters())
            {
                if (!emitter.enabled || emitter.activeCount == 0)
                {
                    continue;
                }

                applyBlendMode(emitter.blendMode);

                switch (emitter.renderType)
                {
                    case SPRITE -> this.spriteRenderer.render(emitter, view, proj);
                    case RIBBON -> this.ribbonRenderer.render(emitter, view, proj);
                    case RING -> this.ringRenderer.render(emitter, view, proj);
                }
            }
        }

        glDepthMask(true);
        glDisable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL20.glUseProgram(0);
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    /**
     * Sets OpenGL blend state for the given blend mode.
     * Each mode is tuned for its primary visual use-case.
     *
     * @param mode the desired blend mode
     */
    private void applyBlendMode(VFXEmitter.BlendMode mode)
    {
        glEnable(GL_BLEND);
        switch (mode)
        {
            /* Additive: fire, lightning, magic, glow, sparks.
               Colors add together — looks like emitted light. */
            case ADDITIVE -> glBlendFunc(GL_SRC_ALPHA, GL_ONE);

            /* Normal: smoke, dust, clouds, fog, water splashes.
               Standard alpha transparency. */
            case NORMAL -> glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            /* Multiply: dark overlays, shadows, scorch marks.
               Darkens whatever is underneath. */
            case MULTIPLY -> glBlendFunc(GL_DST_COLOR, GL_ZERO);

            /* Subtract: dark voids, dark fire, inverse effects. */
            case SUBTRACT -> glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE,
                    GL_ZERO, GL_ONE_MINUS_SRC_ALPHA);

            /* Screen: bright sparkles, lens flares, soft glow.
               Lightens without blowing out. */
            case SCREEN -> glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_COLOR);
        }
    }

    /**
     * Releases all sub-renderer GPU resources.
     */
    public void destroy()
    {
        this.spriteRenderer.destroy();
        this.ribbonRenderer.destroy();
        this.ringRenderer.destroy();
    }
}
