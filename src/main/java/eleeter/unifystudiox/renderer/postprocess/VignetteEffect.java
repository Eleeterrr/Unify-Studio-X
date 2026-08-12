package eleeter.unifystudiox.renderer.postprocess;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;

import java.util.Map;

import org.lwjgl.opengl.GL11C;

public class VignetteEffect implements PostEffect
{
    private static final String NAME = "Vignette";
    private static final float DEF_INTENSITY = 0.5F;
    private static final float DEF_RADIUS = 0.75F;
    private static final float DEF_SOFTNESS = 0.45F;

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
            uniform float uIntensity;
            uniform float uRadius;
            uniform float uSoftness;
            void main() {
                vec3 col  = texture(uScene, vTexCoord).rgb;
                vec2 uv   = vTexCoord - 0.5;
                float dist = length(uv);
                float vign = smoothstep(uRadius, uRadius - uSoftness, dist);
                col *= mix(1.0 - uIntensity, 1.0, vign);
                fragColor = vec4(col, 1.0);
            }
            """;

    private float intensity = DEF_INTENSITY;
    private float radius = DEF_RADIUS;
    private float softness = DEF_SOFTNESS;
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
        this.shader.setFloat("uIntensity", this.intensity);
        this.shader.setFloat("uRadius", this.radius);
        this.shader.setFloat("uSoftness", this.softness);
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

    public void setIntensity(float v)
    {
        this.intensity = v;
    }

    public void setRadius(float v)
    {
        this.radius = v;
    }

    public void setSoftness(float v)
    {
        this.softness = v;
    }

    @Override
    public void writeState(Map<String, Object> out)
    {
        out.put("enabled", this.enabled);
        out.put("intensity", this.intensity);
        out.put("radius", this.radius);
        out.put("softness", this.softness);
    }

    @Override
    public void readState(Map<String, Object> in)
    {
        if (in.containsKey("enabled")) this.enabled = (boolean) in.get("enabled");
        if (in.containsKey("intensity")) this.intensity = ((Number) in.get("intensity")).floatValue();
        if (in.containsKey("radius")) this.radius = ((Number) in.get("radius")).floatValue();
        if (in.containsKey("softness")) this.softness = ((Number) in.get("softness")).floatValue();
    }
}
