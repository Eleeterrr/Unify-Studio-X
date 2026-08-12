package eleeter.unifystudiox.renderer.postprocess;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;

import java.util.Map;

import org.lwjgl.opengl.GL11C;

public class ChromaticAberrationEffect implements PostEffect
{
    private static final String NAME = "ChromaticAberration";
    private static final float DEF_STRENGTH = 0.005F;
    private static final float DEF_FALLOFF = 1.5F;

    private static final String VERT = """
            #version 330 core
            out vec2 vTexCoord;
            void main() {
                vec2 pos[3] = vec2[3](vec2(-1.0,-1.0), vec2(3.0,-1.0), vec2(-1.0,3.0));
                vTexCoord = pos[gl_VertexID] * 0.5 + 0.5;
                gl_Position = vec4(pos[gl_VertexID], 0.0, 1.0);
            }
            """;

    private static final String FRAG = """
            #version 330 core
            in vec2 vTexCoord;
            out vec4 fragColor;
            uniform sampler2D uScene;
            uniform float uStrength;
            uniform float uFalloff;
            void main() {
                vec2 dir    = (vTexCoord - 0.5) * 2.0;
                float dist  = pow(length(dir), uFalloff);
                vec2 offset = dir * uStrength * dist;
                float r = texture(uScene, vTexCoord + offset).r;
                float g = texture(uScene, vTexCoord).g;
                float b = texture(uScene, vTexCoord - offset).b;
                fragColor = vec4(r, g, b, 1.0);
            }
            """;

    private float strength = DEF_STRENGTH;
    private float falloff = DEF_FALLOFF;
    private boolean enabled = true;

    private PostShader shader;
    private FullscreenQuad quad;

    @Override
    public void init()
    {
        this.shader = new PostShader(NAME, VERT, FRAG);
        this.quad = new FullscreenQuad();
    }

    @Override
    public void apply(int inputTexture, int outputFBO, int width, int height)
    {
        glBindFramebuffer(GL_FRAMEBUFFER, outputFBO);
        GL11C.glViewport(0, 0, width, height);

        this.shader.bind();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, inputTexture);
        this.shader.setInt("uScene", 0);
        this.shader.setFloat("uStrength", this.strength);
        this.shader.setFloat("uFalloff", this.falloff);
        this.quad.draw();
        this.shader.unbind();

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void dispose()
    {
        if (this.shader != null) this.shader.dispose();
        if (this.quad != null) this.quad.dispose();
    }

    @Override
    public boolean isEnabled()
    {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean e)
    {
        this.enabled = e;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    public void setStrength(float v)
    {
        this.strength = v;
    }

    public void setFalloff(float v)
    {
        this.falloff = v;
    }

    @Override
    public void writeState(Map<String, Object> out)
    {
        out.put("enabled", this.enabled);
        out.put("strength", this.strength);
        out.put("falloff", this.falloff);
    }

    @Override
    public void readState(Map<String, Object> in)
    {
        if (in.containsKey("enabled")) this.enabled = (boolean) in.get("enabled");
        if (in.containsKey("strength")) this.strength = ((Number) in.get("strength")).floatValue();
        if (in.containsKey("falloff")) this.falloff = ((Number) in.get("falloff")).floatValue();
    }
}
