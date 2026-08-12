package eleeter.unifystudiox.renderer.postprocess;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;

import java.util.Map;

import org.lwjgl.opengl.GL11C;

public class ACESTonemapEffect implements PostEffect
{
    private static final String NAME = "ACESTonemap";
    private static final float DEF_EXPOSURE = 1.0F;
    private static final float DEF_GAMMA = 2.2F;

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
            uniform float uExposure;
            uniform float uGamma;
            
            vec3 aces(vec3 x) {
                float a = 2.51;
                float b = 0.03;
                float c = 2.43;
                float d = 0.59;
                float e = 0.14;
                return clamp((x * (a * x + b)) / (x * (c * x + d) + e), 0.0, 1.0);
            }
            
            void main() {
                vec3 col = texture(uScene, vTexCoord).rgb;
                col *= uExposure;
                col  = aces(col);
                col  = pow(col, vec3(1.0 / uGamma));
                fragColor = vec4(col, 1.0);
            }
            """;

    private float exposure = DEF_EXPOSURE;
    private float gamma = DEF_GAMMA;
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
        this.shader.setFloat("uExposure", this.exposure);
        this.shader.setFloat("uGamma", this.gamma);
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

    public void setExposure(float v)
    {
        this.exposure = v;
    }

    public void setGamma(float v)
    {
        this.gamma = v;
    }

    @Override
    public void writeState(Map<String, Object> out)
    {
        out.put("enabled", this.enabled);
        out.put("exposure", this.exposure);
        out.put("gamma", this.gamma);
    }

    @Override
    public void readState(Map<String, Object> in)
    {
        if (in.containsKey("enabled")) this.enabled = (boolean) in.get("enabled");
        if (in.containsKey("exposure")) this.exposure = ((Number) in.get("exposure")).floatValue();
        if (in.containsKey("gamma")) this.gamma = ((Number) in.get("gamma")).floatValue();
    }
}
