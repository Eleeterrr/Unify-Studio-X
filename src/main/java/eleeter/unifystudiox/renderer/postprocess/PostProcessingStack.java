package eleeter.unifystudiox.renderer.postprocess;

import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_NEAREST;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;
import static org.lwjgl.opengl.GL45C.glBlitNamedFramebuffer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11C;

import eleeter.unifystudiox.util.log.AniLogger;


public class PostProcessingStack
{
    private static final String PASSTHROUGH_VERT = """
            #version 330 core
            out vec2 vTexCoord;
            void main() {
                vec2 pos[3] = vec2[3](vec2(-1.0,-1.0), vec2(3.0,-1.0), vec2(-1.0,3.0));
                vTexCoord = pos[gl_VertexID] * 0.5 + 0.5;
                gl_Position = vec4(pos[gl_VertexID], 0.0, 1.0);
            }
            """;

    private static final String PASSTHROUGH_FRAG = """
            #version 330 core
            in vec2 vTexCoord;
            out vec4 fragColor;
            uniform sampler2D uTex;
            void main() { fragColor = texture(uTex, vTexCoord); }
            """;

    private final List<PostEffect> effects = new ArrayList<>();

    private SceneFramebuffer sceneFbo;
    private PingPongFBO pingPong;
    private PostShader passthrough;
    private FullscreenQuad quad;
    private int currentWidth = 0;
    private int currentHeight = 0;

    public void init()
    {
        this.passthrough = new PostShader("Passthrough", PASSTHROUGH_VERT, PASSTHROUGH_FRAG);
        this.quad = new FullscreenQuad();

        for (PostEffect effect : this.effects)
        {
            effect.init();
        }

        AniLogger.info("PostProcessingStack", "Initialized with " + this.effects.size() + " effects.");
    }

    public void addEffect(PostEffect effect)
    {
        this.effects.add(effect);
    }

    public void removeEffect(String name)
    {
        this.effects.removeIf(e -> e.getName().equals(name));
    }

    public void setEffectEnabled(String name, boolean enabled)
    {
        for (PostEffect e : this.effects)
        {
            if (e.getName().equals(name))
            {
                e.setEnabled(enabled);
                return;
            }
        }
    }

    public void moveEffect(String name, int newIndex)
    {
        PostEffect target = null;

        for (PostEffect e : this.effects)
        {
            if (e.getName().equals(name))
            {
                target = e;
                break;
            }
        }

        if (target != null)
        {
            this.effects.remove(target);
            this.effects.add(Math.min(newIndex, this.effects.size()), target);
        }
    }


    public void render(int msaaFboHandle, int depthTexHandle, int fboW, int fboH, int screenW, int screenH, int targetFbo)
    {
        ensureBuffers(fboW, fboH);

        for (PostEffect e : this.effects)
        {
            if (e instanceof DepthOfFieldEffect dof)
            {
                dof.setDepthTexture(depthTexHandle);
            }
        }

        glBlitNamedFramebuffer(
                msaaFboHandle, this.sceneFbo.getFBOHandle(),
                0, 0, fboW, fboH,
                0, 0, fboW, fboH,
                GL_COLOR_BUFFER_BIT, GL_NEAREST
        );

        boolean depthTestWasEnabled = GL11C.glGetBoolean(GL11C.GL_DEPTH_TEST);
        GL11C.glDisable(GL11C.GL_DEPTH_TEST);

        int currentTex = this.sceneFbo.getColorTextureHandle();

        for (PostEffect effect : this.effects)
        {
            if (!effect.isEnabled()) continue;

            effect.apply(currentTex, this.pingPong.getWriteFBO(), fboW, fboH);
            this.pingPong.swap();
            currentTex = this.pingPong.getReadTexture();
        }

        /* Draw final result to target FBO at true screen resolution */
        glBindFramebuffer(GL_FRAMEBUFFER, targetFbo);
        GL11C.glViewport(0, 0, screenW, screenH);

        this.passthrough.bind();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, currentTex);
        this.passthrough.setInt("uTex", 0);
        this.quad.draw();
        this.passthrough.unbind();

        if (depthTestWasEnabled)
        {
            GL11C.glEnable(GL11C.GL_DEPTH_TEST);
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void ensureBuffers(int w, int h)
    {
        if (this.sceneFbo == null)
        {
            this.sceneFbo = new SceneFramebuffer(w, h);
            this.pingPong = new PingPongFBO(w, h);
            this.currentWidth = w;
            this.currentHeight = h;
        } else if (w != this.currentWidth || h != this.currentHeight)
        {
            resize(w, h);
        }
    }

    public void resize(int width, int height)
    {
        if (this.sceneFbo != null) this.sceneFbo.resize(width, height);
        if (this.pingPong != null) this.pingPong.resize(width, height);

        this.currentWidth = width;
        this.currentHeight = height;
    }

    public void dispose()
    {
        for (PostEffect e : this.effects)
        {
            e.dispose();
        }

        if (this.sceneFbo != null) this.sceneFbo.dispose();
        if (this.pingPong != null) this.pingPong.dispose();
        if (this.passthrough != null) this.passthrough.dispose();
        if (this.quad != null) this.quad.dispose();
    }

    public Map<String, Object> toJson()
    {
        Map<String, Object> root = new LinkedHashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();

        for (PostEffect e : this.effects)
        {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", e.getName());
            e.writeState(entry);
            list.add(entry);
        }

        root.put("effects", list);
        return root;
    }

    @SuppressWarnings("unchecked")
    public void fromJson(Map<String, Object> root)
    {
        if (!root.containsKey("effects")) return;

        List<Map<String, Object>> list = (List<Map<String, Object>>) root.get("effects");

        for (Map<String, Object> entry : list)
        {
            String name = (String) entry.get("name");

            for (PostEffect e : this.effects)
            {
                if (e.getName().equals(name))
                {
                    e.readState(entry);
                    break;
                }
            }
        }
    }

    public List<PostEffect> getEffects()
    {
        return this.effects;
    }
}
