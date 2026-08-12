package eleeter.unifystudiox.renderer.postprocess;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;

import java.util.Map;

import org.lwjgl.opengl.GL11C;

import eleeter.unifystudiox.graphics.Framebuffer;
import eleeter.unifystudiox.graphics.TextureFormatBit;

public class BloomEffect implements PostEffect
{
    private static final String NAME = "Bloom";
    private static final float DEF_THRESHOLD = 0.8F;
    private static final float DEF_INTENSITY = 1.0F;
    private static final int DEF_BLUR_RADIUS = 4;
    private static final int BLUR_DOWNSCALE = 2;


    /* TODO: All these GLSL code will be transfer into the shaders folder. */
    private static final String VERT = """
            #version 330 core
            out vec2 vTexCoord;
            void main() {
                vec2 pos[3] = vec2[3](vec2(-1.0,-1.0), vec2(3.0,-1.0), vec2(-1.0,3.0));
                vTexCoord = pos[gl_VertexID] * 0.5 + 0.5;
                gl_Position = vec4(pos[gl_VertexID], 0.0, 1.0);
            }
            """;

    private static final String FRAG_BRIGHT = """
            #version 330 core
            in vec2 vTexCoord;
            out vec4 fragColor;
            uniform sampler2D uScene;
            uniform float uThreshold;
            void main() {
                vec3 col = texture(uScene, vTexCoord).rgb;
                float brightness = dot(col, vec3(0.2126, 0.7152, 0.0722));
                fragColor = brightness > uThreshold ? vec4(col, 1.0) : vec4(0.0);
            }
            """;

    private static final String FRAG_BLUR = """
            #version 330 core
            in vec2 vTexCoord;
            out vec4 fragColor;
            uniform sampler2D uTex;
            uniform vec2 uDir;
            uniform int uRadius;
            void main() {
                vec2 texelSize = 1.0 / vec2(textureSize(uTex, 0));
                vec4 result = vec4(0.0);
                float total = 0.0;
                for (int i = -uRadius; i <= uRadius; i++) {
                    float w = exp(-float(i * i) / (2.0 * float(uRadius * uRadius)));
                    result += texture(uTex, vTexCoord + uDir * texelSize * float(i)) * w;
                    total += w;
                }
                fragColor = result / total;
            }
            """;

    private static final String FRAG_COMPOSITE = """
            #version 330 core
            in vec2 vTexCoord;
            out vec4 fragColor;
            uniform sampler2D uScene;
            uniform sampler2D uBloom;
            uniform float uIntensity;
            void main() {
                vec3 scene = texture(uScene, vTexCoord).rgb;
                vec3 bloom = texture(uBloom, vTexCoord).rgb;
                fragColor = vec4(scene + bloom * uIntensity, 1.0);
            }
            """;

    private float threshold = DEF_THRESHOLD;
    private float intensity = DEF_INTENSITY;
    private int blurRadius = DEF_BLUR_RADIUS;
    private boolean enabled = true;

    private PostShader shaderBright;
    private PostShader shaderBlur;
    private PostShader shaderComposite;
    private FullscreenQuad quad;
    private Framebuffer brightFbo;
    private PingPongFBO blurPing;

    @Override
    public void init()
    {
        this.shaderBright = new PostShader(NAME, VERT, FRAG_BRIGHT);
        this.shaderBlur = new PostShader(NAME + "_blur", VERT, FRAG_BLUR);
        this.shaderComposite = new PostShader(NAME + "_comp", VERT, FRAG_COMPOSITE);
        this.quad = new FullscreenQuad();
    }

    @Override
    public void apply(int inputTexture, int outputFBO, int width, int height)
    {
        int bw = Math.max(1, width / BLUR_DOWNSCALE);
        int bh = Math.max(1, height / BLUR_DOWNSCALE);

        ensureBlurBuffers(bw, bh);

        glBindFramebuffer(GL_FRAMEBUFFER, this.brightFbo.getHandle());
        GL11C.glViewport(0, 0, bw, bh);
        this.shaderBright.bind();
        bindTex(0, inputTexture);
        this.shaderBright.setInt("uScene", 0);
        this.shaderBright.setFloat("uThreshold", this.threshold);
        this.quad.draw();
        this.shaderBright.unbind();

        int brightTex = this.brightFbo.getColorTexture(0).getHandle();

        this.shaderBlur.bind();
        this.shaderBlur.setInt("uTex", 0);
        this.shaderBlur.setInt("uRadius", this.blurRadius);

        glBindFramebuffer(GL_FRAMEBUFFER, this.blurPing.getWriteFBO());
        GL11C.glViewport(0, 0, bw, bh);
        bindTex(0, brightTex);
        this.shaderBlur.setVec2("uDir", 1.0F, 0.0F);
        this.quad.draw();
        this.blurPing.swap();

        glBindFramebuffer(GL_FRAMEBUFFER, this.blurPing.getWriteFBO());
        bindTex(0, this.blurPing.getReadTexture());
        this.shaderBlur.setVec2("uDir", 0.0F, 1.0F);
        this.quad.draw();
        this.blurPing.swap();

        this.shaderBlur.unbind();

        glBindFramebuffer(GL_FRAMEBUFFER, outputFBO);
        GL11C.glViewport(0, 0, width, height);
        this.shaderComposite.bind();
        bindTex(0, inputTexture);
        bindTex(1, this.blurPing.getReadTexture());
        this.shaderComposite.setInt("uScene", 0);
        this.shaderComposite.setInt("uBloom", 1);
        this.shaderComposite.setFloat("uIntensity", this.intensity);
        this.quad.draw();
        this.shaderComposite.unbind();

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void ensureBlurBuffers(int bw, int bh)
    {
        if (this.brightFbo == null)
        {
            this.brightFbo = Framebuffer.builder(bw, bh).addColorAttachment(TextureFormatBit.RGBA8).build();
            this.blurPing = new PingPongFBO(bw, bh);
        } else if (this.brightFbo.getWidth() != bw || this.brightFbo.getHeight() != bh)
        {
            this.brightFbo.resize(bw, bh);
            this.blurPing.resize(bw, bh);
        }
    }

    private static void bindTex(int unit, int handle)
    {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_2D, handle);
    }

    @Override
    public void dispose()
    {
        if (this.shaderBright != null) this.shaderBright.dispose();
        if (this.shaderBlur != null) this.shaderBlur.dispose();
        if (this.shaderComposite != null) this.shaderComposite.dispose();
        if (this.quad != null) this.quad.dispose();
        if (this.brightFbo != null) this.brightFbo.destroy();
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

    public float getThreshold()
    {
        return this.threshold;
    }

    public float getIntensity()
    {
        return this.intensity;
    }

    public int getBlurRadius()
    {
        return this.blurRadius;
    }

    public void setThreshold(float t)
    {
        this.threshold = t;
    }

    public void setIntensity(float i)
    {
        this.intensity = i;
    }

    public void setBlurRadius(int r)
    {
        this.blurRadius = r;
    }

    @Override
    public void writeState(Map<String, Object> out)
    {
        out.put("enabled", this.enabled);
        out.put("threshold", this.threshold);
        out.put("intensity", this.intensity);
        out.put("blurRadius", this.blurRadius);
    }

    @Override
    public void readState(Map<String, Object> in)
    {
        if (in.containsKey("enabled")) this.enabled = (boolean) in.get("enabled");
        if (in.containsKey("threshold")) this.threshold = ((Number) in.get("threshold")).floatValue();
        if (in.containsKey("intensity")) this.intensity = ((Number) in.get("intensity")).floatValue();
        if (in.containsKey("blurRadius")) this.blurRadius = ((Number) in.get("blurRadius")).intValue();
    }
}
