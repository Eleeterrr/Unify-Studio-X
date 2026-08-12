package eleeter.unifystudiox.renderer.postprocess;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;

import java.util.Map;

import org.lwjgl.opengl.GL11C;

public class DepthOfFieldEffect implements PostEffect
{
    private static final String NAME = "DepthOfField";
    private static final float DEF_FOCUS_DIST = 10.0F;
    private static final float DEF_FOCUS_RANGE = 5.0F;
    private static final float DEF_BLUR_STRENGTH = 1.0F;
    private static final int DOF_BLUR_RADIUS = 6;

    private static final String VERT = """
            #version 330 core
            out vec2 vTexCoord;
            void main() {
                vec2 pos[3] = vec2[3](vec2(-1.0,-1.0), vec2(3.0,-1.0), vec2(-1.0,3.0));
                vTexCoord = pos[gl_VertexID] * 0.5 + 0.5;
                gl_Position = vec4(pos[gl_VertexID], 0.0, 1.0);
            }
            """;

    private static final String FRAG_BLUR = """
            #version 330 core
            in vec2 vTexCoord;
            out vec4 fragColor;
            uniform sampler2D uTex;
            uniform vec2 uDir;
            void main() {
                vec2 texel = 1.0 / vec2(textureSize(uTex, 0));
                vec4 result = vec4(0.0);
                float total = 0.0;
                for (int i = -6; i <= 6; i++) {
                    float w = exp(-float(i * i) / 18.0);
                    result += texture(uTex, vTexCoord + uDir * texel * float(i)) * w;
                    total += w;
                }
                fragColor = result / total;
            }
            """;

    private static final String FRAG_COMPOSITE = """
            #version 330 core
            in vec2 vTexCoord;
            out vec4 fragColor;
            uniform sampler2D uSharp;
            uniform sampler2D uBlurred;
            uniform sampler2D uDepth;
            uniform float uFocusDist;
            uniform float uFocusRange;
            uniform float uBlurStrength;
            float linearDepth(float d) {
                float near = 0.1;
                float far  = 1000.0;
                return (2.0 * near) / (far + near - d * (far - near));
            }
            void main() {
                float rawDepth = texture(uDepth, vTexCoord).r;
                float depth    = linearDepth(rawDepth) * 1000.0;
                float blur     = clamp(abs(depth - uFocusDist) / uFocusRange, 0.0, 1.0) * uBlurStrength;
                vec3 sharp     = texture(uSharp, vTexCoord).rgb;
                vec3 blurred   = texture(uBlurred, vTexCoord).rgb;
                fragColor      = vec4(mix(sharp, blurred, blur), 1.0);
            }
            """;

    private float focusDistance = DEF_FOCUS_DIST;
    private float focusRange = DEF_FOCUS_RANGE;
    private float blurStrength = DEF_BLUR_STRENGTH;
    private boolean enabled = true;

    private PostShader shaderBlur;
    private PostShader shaderComposite;
    private FullscreenQuad quad;
    private PingPongFBO blurPing;
    private int depthTexHandle = 0;

    @Override
    public void init()
    {
        this.shaderBlur = new PostShader(NAME + "_blur", VERT, FRAG_BLUR);
        this.shaderComposite = new PostShader(NAME + "_comp", VERT, FRAG_COMPOSITE);
        this.quad = new FullscreenQuad();
    }

    public void setDepthTexture(int handle)
    {
        this.depthTexHandle = handle;
    }

    @Override
    public void apply(int inputTexture, int outputFBO, int width, int height)
    {
        if (this.blurPing == null)
        {
            this.blurPing = new PingPongFBO(width, height);
        } else if (this.blurPing.getReadTexture() == 0)
        {
        }

        this.shaderBlur.bind();
        this.shaderBlur.setInt("uTex", 0);

        glBindFramebuffer(GL_FRAMEBUFFER, this.blurPing.getWriteFBO());
        GL11C.glViewport(0, 0, width, height);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, inputTexture);
        this.shaderBlur.setVec2("uDir", 1.0F, 0.0F);
        this.quad.draw();
        this.blurPing.swap();

        glBindFramebuffer(GL_FRAMEBUFFER, this.blurPing.getWriteFBO());
        glBindTexture(GL_TEXTURE_2D, this.blurPing.getReadTexture());
        this.shaderBlur.setVec2("uDir", 0.0F, 1.0F);
        this.quad.draw();
        this.blurPing.swap();

        this.shaderBlur.unbind();

        glBindFramebuffer(GL_FRAMEBUFFER, outputFBO);
        GL11C.glViewport(0, 0, width, height);

        this.shaderComposite.bind();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, inputTexture);
        glActiveTexture(GL_TEXTURE0 + 1);
        glBindTexture(GL_TEXTURE_2D, this.blurPing.getReadTexture());
        glActiveTexture(GL_TEXTURE0 + 2);
        glBindTexture(GL_TEXTURE_2D, this.depthTexHandle);

        this.shaderComposite.setInt("uSharp", 0);
        this.shaderComposite.setInt("uBlurred", 1);
        this.shaderComposite.setInt("uDepth", 2);
        this.shaderComposite.setFloat("uFocusDist", this.focusDistance);
        this.shaderComposite.setFloat("uFocusRange", this.focusRange);
        this.shaderComposite.setFloat("uBlurStrength", this.blurStrength);
        this.quad.draw();
        this.shaderComposite.unbind();

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void dispose()
    {
        if (this.shaderBlur != null) this.shaderBlur.dispose();
        if (this.shaderComposite != null) this.shaderComposite.dispose();
        if (this.quad != null) this.quad.dispose();
        if (this.blurPing != null) this.blurPing.dispose();
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

    public void setFocusDistance(float v)
    {
        this.focusDistance = v;
    }

    public void setFocusRange(float v)
    {
        this.focusRange = v;
    }

    public void setBlurStrength(float v)
    {
        this.blurStrength = v;
    }

    @Override
    public void writeState(Map<String, Object> out)
    {
        out.put("enabled", this.enabled);
        out.put("focusDistance", this.focusDistance);
        out.put("focusRange", this.focusRange);
        out.put("blurStrength", this.blurStrength);
    }

    @Override
    public void readState(Map<String, Object> in)
    {
        if (in.containsKey("enabled")) this.enabled = (boolean) in.get("enabled");
        if (in.containsKey("focusDistance")) this.focusDistance = ((Number) in.get("focusDistance")).floatValue();
        if (in.containsKey("focusRange")) this.focusRange = ((Number) in.get("focusRange")).floatValue();
        if (in.containsKey("blurStrength")) this.blurStrength = ((Number) in.get("blurStrength")).floatValue();
    }
}
